#!/usr/bin/env python3
# train_fraud_model.py
"""
Pipeline completo (versión explicada) para entrenar un modelo de detección de fraude
con soporte de features espaciales (latitude/longitude) usando el dataset:
    transaction_data.csv

Diseñado para ejecución LOCAL (CPU). Comentarios pedagógicos y explicación por cada bloque.

Resumen de pasos:
1) Carga y EDA básico
2) Preprocesamiento (parsing geolocation, tratamiento nulos, normalización de nombres)
3) Ingeniería de features: temporales, por cuenta, transaccionales, dispositivo/red, espaciales
4) División temporal (time-based split) para evitar data leakage
5) Undersampling de negativos para prototipo (controlable)
6) Encoding (OneHot / TargetEncoder) y escalado (RobustScaler)
7) IsolationForest -> feature de anomalía
8) Entrenamiento LightGBM (sklearn API) en CPU con early stopping
9) Búsqueda de umbral óptimo (F1) + Youden (TPR-FPR) para comparativa
10) Calibración opcional (CalibratedClassifierCV, isotonic)
11) Evaluación (AUC, precision, recall, f1, FPR), plots y guardado de artefactos

IMPORTANTE:
- Ajusta DATA_PATH si tu CSV tiene otro nombre/ruta.
- Revisa el mapeo de nombres en 'find_col' si tus columnas usan nombres diferentes.
- Instalación recomendada:
    pip install pandas numpy scikit-learn lightgbm joblib matplotlib category_encoders
"""

import os
import time
import json
import logging
from functools import wraps
from typing import Tuple, Dict, Any, List

import numpy as np
import pandas as pd

from sklearn.model_selection import train_test_split
from sklearn.preprocessing import RobustScaler, LabelEncoder
from sklearn.metrics import (roc_auc_score, precision_score, recall_score, f1_score,
                             confusion_matrix, precision_recall_curve, roc_curve, auc)
from sklearn.calibration import CalibratedClassifierCV
from sklearn.ensemble import IsolationForest

import joblib
import matplotlib.pyplot as plt

# Optional: TargetEncoder from category_encoders (recommended for high-cardinality categoricals)
try:
    from category_encoders import TargetEncoder
except Exception:
    TargetEncoder = None

# LightGBM sklearn API (CPU)
try:
    import lightgbm as lgb
    LGB_AVAILABLE = True
except Exception:
    LGB_AVAILABLE = False

# -------------------------
# Configuration
# -------------------------
RANDOM_STATE = 42
DATA_PATH = "transaction_data.csv"        # Cambia si tu archivo tiene otro nombre
OUTPUT_DIR = "outputs_prototype"
MODEL_DIR = os.path.join(OUTPUT_DIR, "models")
PLOTS_DIR = os.path.join(OUTPUT_DIR, "plots")
os.makedirs(MODEL_DIR, exist_ok=True)
os.makedirs(PLOTS_DIR, exist_ok=True)

# Logging (console + file)
logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger(__name__)
fh = logging.FileHandler(os.path.join(OUTPUT_DIR, "train_fraud_model.log"))
fh.setLevel(logging.INFO)
logger.addHandler(fh)

# -------------------------
# Utility decorators & helpers
# -------------------------
def timed(func):
    """Decorador para medir tiempo de ejecución de funciones y loguearlo."""
    @wraps(func)
    def wrapper(*a, **kw):
        t0 = time.time()
        logger.info("START - %s", func.__name__)
        res = func(*a, **kw)
        logger.info("END   - %s (%.2fs)", func.__name__, time.time() - t0)
        return res
    return wrapper

def find_col(df: pd.DataFrame, candidates: List[str]) -> str:
    """
    Busca y devuelve el primer nombre de columna que coincida con 'candidates'
    (match case-insensitive). Si no encuentra ninguno devuelve None.
    Útil para adaptar script a datasets con nombres variables.
    """
    cols_lower = {c.lower(): c for c in df.columns}
    for cand in candidates:
        if cand in df.columns:
            return cand
        if cand.lower() in cols_lower:
            return cols_lower[cand.lower()]
    return None

def haversine_km(lat1, lon1, lat2, lon2):
    """
    Calcula distancia en km entre (lat1,lon1) y (lat2,lon2) con fórmula Haversine.
    Maneja nulos devolviendo np.nan.
    """
    try:
        if (pd.isna(lat1) or pd.isna(lon1) or pd.isna(lat2) or pd.isna(lon2)):
            return np.nan
        lat1, lon1, lat2, lon2 = map(np.radians, [lat1, lon1, lat2, lon2])
        dlat = lat2 - lat1
        dlon = lon2 - lon1
        a = np.sin(dlat/2.0)**2 + np.cos(lat1) * np.cos(lat2) * np.sin(dlon/2.0)**2
        c = 2 * np.arcsin(np.sqrt(a))
        R = 6371.0  # Earth radius in kilometers
        return R * c
    except Exception:
        return np.nan

# -------------------------
# 1. Load & basic EDA
# -------------------------
@timed
def load_data(path: str = DATA_PATH) -> pd.DataFrame:
    """Carga CSV, detecta target y timestamp, y realiza EDA sintetico."""
    if not os.path.exists(path):
        raise FileNotFoundError(f"File not found: {path}")

    df = pd.read_csv(path)
    # Normalize column names (no spaces)
    df.columns = [c.strip().replace(" ", "_") for c in df.columns]
    logger.info("Loaded data shape: %s", df.shape)

    # Detect target column (common names)
    target_candidates = [c for c in df.columns if c.lower() in ("fraud_flag", "fraudflag", "fraud", "isfraud")]
    if not target_candidates:
        raise ValueError("No target column found. Expected 'Fraud Flag' or similar.")
    target_col = target_candidates[0]
    df.rename(columns={target_col: "Fraud_Flag"}, inplace=True)
    df["Fraud_Flag"] = df["Fraud_Flag"].astype(int)

    # Attempt parse timestamp if any
    ts_candidates = [c for c in df.columns if c.lower() in ("timestamp", "transactiondt", "date", "datetime")]
    if ts_candidates:
        ts = ts_candidates[0]
        try:
            df["Timestamp"] = pd.to_datetime(df[ts])
        except Exception:
            try:
                df["Timestamp"] = pd.to_datetime(df[ts].astype(float), unit="s")
            except Exception:
                logger.warning("Could not parse timestamp column %s to datetime.", ts)
    else:
        logger.warning("No timestamp column found. Later pipeline will fallback to random split.")

    # Quick EDA logs
    logger.info("Top null counts:\n%s", df.isnull().sum().sort_values(ascending=False).head(20).to_string())
    logger.info("Target distribution: positives=%d, negatives=%d, pos_rate=%.6f",
                int(df["Fraud_Flag"].sum()), int(len(df) - df["Fraud_Flag"].sum()), df["Fraud_Flag"].mean())
    return df

# -------------------------
# 2. Preprocessing & Feature Engineering
# -------------------------
@timed
def preprocess_basic(df: pd.DataFrame) -> pd.DataFrame:
    """
    Limpieza básica:
    - Quitar duplicados exactos
    - Trim strings
    - Mantener el resto sin cambios; las conversiones ocurren en feature_engineering()
    """
    df = df.copy()
    before = len(df)
    df = df.drop_duplicates().reset_index(drop=True)
    logger.info("Dropped %d duplicates", before - len(df))
    # Strip strings for object columns
    for c in df.select_dtypes(include=["object"]).columns:
        df[c] = df[c].astype(str).str.strip()
    return df

@timed
def parse_geolocation(df: pd.DataFrame, geoloc_col_candidates: List[str] = None) -> pd.DataFrame:
    """
    Parsea la columna que contiene 'Geolocation (Latitude/Longitude)' y extrae latitude & longitude.
    En el dataset la columna suele tener strings del tipo "(lat, lon)".
    """
    df = df.copy()
    if geoloc_col_candidates is None:
        geoloc_col_candidates = ["Geolocation (Latitude/Longitude)", "Geolocation_(Latitude/Longitude)", "Geolocation", "Geolocation_Latitude_Longitude",
                                 "Geolocation_(Latitude/Longitude)"]
    col = find_col(df, geoloc_col_candidates)
    if col is None:
        logger.warning("No geolocation column found among candidates. Skipping geolocation parsing.")
        df["latitude"] = np.nan
        df["longitude"] = np.nan
        return df

    # Clean format like "(lat, lon)" or "lat,lon" and split
    s = df[col].astype(str).fillna("")
    # Remove parentheses and spaces then split by comma
    s_clean = s.str.replace(r"[()]", "", regex=True)
    parts = s_clean.str.split(",", expand=True)
    if parts.shape[1] < 2:
        logger.warning("Geolocation column found but could not split into lat/lon reliably.")
        df["latitude"] = np.nan
        df["longitude"] = np.nan
        return df
    df["latitude"] = pd.to_numeric(parts[0], errors="coerce")
    df["longitude"] = pd.to_numeric(parts[1], errors="coerce")
    logger.info("Parsed geolocation into latitude/longitude. Missing lat/lon: %d", df[["latitude", "longitude"]].isnull().any(axis=1).sum())
    return df

@timed
def feature_engineering(df: pd.DataFrame) -> pd.DataFrame:
    """
    Construcción de features:
    - Temporales: hour, dayofweek, is_weekend, time_since_last_txn_user, txn_count_last_5min (approx)
    - Cuenta/relación: sender_txn_count, sender_avg_amount, receiver_txn_count, receiver_avg_amount
    - Transaccionales: amount_zscore_user, is_high_value, same_receiver_amount_count
    - Dispositivo/red: device_freq, avg_latency_user, bandwidth_ratio
    - Espaciales: distance_from_last_location, avg_txn_distance, suspicious_travel
    """
    df = df.copy()

    # heuristics for key column names (adapta si tu CSV usa otros nombres)
    amt_col = find_col(df, ["Transaction Amount", "TransactionAmount", "Amount", "Transaction_Amount_(USD)"])
    sender_col = find_col(df, ["Sender Account ID", "SenderID", "from_account", "sender_id"])
    receiver_col = find_col(df, ["Receiver Account ID", "ReceiverID", "to_account", "receiver_id"])
    ts_col = find_col(df, ["Timestamp", "TransactionDT", "timestamp", "Date", "datetime"])
    device_col = find_col(df, ["Device Used", "Device", "device"])
    latency_col = find_col(df, ["Latency (ms)","Latency", "Latency_ms", "Latency_(ms)"])
    bandwidth_col = find_col(df, ["Slice Bandwidth (Mbps)", "Slice_Bandwidth", "Slice_Bandwidth_Mbps", "Bandwidth"])

    # TEMPORALES: convert timestamp and extract hour/day features
    if ts_col and ts_col in df.columns:
        df[ts_col] = pd.to_datetime(df[ts_col], errors="coerce")
        df["hour"] = df[ts_col].dt.hour.fillna(0).astype(int)
        df["dayofweek"] = df[ts_col].dt.dayofweek.fillna(0).astype(int)
        df["is_weekend"] = df["dayofweek"].isin([5, 6]).astype(int)
    else:
        df["hour"] = 0
        df["dayofweek"] = 0
        df["is_weekend"] = 0

    # TIME DELTAS per user (time since last transaction)
    if sender_col and sender_col in df.columns and ts_col and ts_col in df.columns:
        df = df.sort_values([sender_col, ts_col]).reset_index(drop=True)
        df["time_since_last_txn_user"] = df.groupby(sender_col)[ts_col].diff().dt.total_seconds().fillna(1e9)
        # txn_count_last_5min: attempt using rolling window if possible; can be heavy so fallback safe
        try:
            df["_tmp_ones"] = 1
            df["txn_count_last_5min"] = df.groupby(sender_col).rolling("5min", on=ts_col)["_tmp_ones"].count().values
            df.drop(columns=["_tmp_ones"], inplace=True)
        except Exception:
            # fallback: cumulative count as proxy
            df["txn_count_last_5min"] = df.groupby(sender_col).cumcount().clip(upper=20)
    else:
        df["time_since_last_txn_user"] = 1e9
        df["txn_count_last_5min"] = 0

    # PER-ACCOUNT AGGREGATES (sender & receiver)
    if sender_col and amt_col and sender_col in df.columns and amt_col in df.columns:
        grp_s = df.groupby(sender_col)[amt_col].agg(["count", "mean", "std"]).rename(columns={"count":"sender_txn_count","mean":"sender_avg_amount","std":"sender_std_amount"})
        df = df.merge(grp_s, how="left", left_on=sender_col, right_index=True)
    else:
        df["sender_txn_count"] = 0
        df["sender_avg_amount"] = 0.0
        df["sender_std_amount"] = 1.0

    if receiver_col and amt_col and receiver_col in df.columns and amt_col in df.columns:
        grp_r = df.groupby(receiver_col)[amt_col].agg(["count", "mean"]).rename(columns={"count":"receiver_txn_count","mean":"receiver_avg_amount"})
        df = df.merge(grp_r, how="left", left_on=receiver_col, right_index=True)
    else:
        df["receiver_txn_count"] = 0
        df["receiver_avg_amount"] = 0.0

    # TRANSACTIONAL: z-score, high value, repeated pattern
    if amt_col and sender_col and amt_col in df.columns and sender_col in df.columns:
        df["sender_std_amount"] = df["sender_std_amount"].replace(0, 1.0)
        df["amount_zscore_user"] = (df[amt_col] - df["sender_avg_amount"]) / (df["sender_std_amount"] + 1e-9)
        df["is_high_value"] = (df[amt_col] >= df.groupby(sender_col)[amt_col].transform(lambda x: x.quantile(0.95))).astype(int)
    else:
        df["amount_zscore_user"] = 0.0
        df["is_high_value"] = 0

    if sender_col and receiver_col and amt_col and all(c in df.columns for c in [sender_col, receiver_col, amt_col]):
        df["same_receiver_amount_count"] = df.groupby([sender_col, receiver_col, amt_col])[amt_col].transform("count")
    else:
        df["same_receiver_amount_count"] = 0

    # DEVICE / NETWORK features
    if device_col and device_col in df.columns:
        # device_freq: how many unique senders use the same device (proxy for suspicious shared hardware)
        df["device_freq"] = df.groupby(device_col)[sender_col if sender_col in df.columns else amt_col].transform("nunique")
    else:
        df["device_freq"] = 0

    if latency_col and latency_col in df.columns:
        df[latency_col] = pd.to_numeric(df[latency_col], errors="coerce").fillna(0)
        if sender_col in df.columns:
            df["avg_latency_user"] = df.groupby(sender_col)[latency_col].transform("mean").fillna(0)
        else:
            df["avg_latency_user"] = df[latency_col]
    else:
        df["avg_latency_user"] = 0.0

    if bandwidth_col and bandwidth_col in df.columns and latency_col and latency_col in df.columns:
        df[bandwidth_col] = pd.to_numeric(df[bandwidth_col], errors="coerce").fillna(0.0)
        df["bandwidth_ratio"] = df[bandwidth_col] / (df[latency_col] + 1e-6)
    else:
        df["bandwidth_ratio"] = 0.0

    # -------------------------
    # SPATIAL FEATURES: latitude/longitude parsing + distance calculations
    # -------------------------
    df = parse_geolocation(df)  # adds columns latitude, longitude (may be NaN)
    # If we have geolocation and sender/time, compute distance between consecutive transactions per sender
    if 'latitude' in df.columns and 'longitude' in df.columns and sender_col and sender_col in df.columns and ts_col and ts_col in df.columns:
        df = df.sort_values([sender_col, ts_col]).reset_index(drop=True)
        df["prev_lat"] = df.groupby(sender_col)["latitude"].shift(1)
        df["prev_lon"] = df.groupby(sender_col)["longitude"].shift(1)
        # compute haversine
        df["distance_from_last_location"] = df.apply(lambda r: haversine_km(r["latitude"], r["longitude"], r["prev_lat"], r["prev_lon"]), axis=1)
        # avg distance per sender
        df["avg_txn_distance"] = df.groupby(sender_col)["distance_from_last_location"].transform("mean").fillna(0.0)
        # suspicious travel: distance > 500km and time_since_last_txn_user < 1800s (30min) -> impossible travel
        df["suspicious_travel"] = ((df["distance_from_last_location"] > 500) & (df["time_since_last_txn_user"] < 1800)).astype(int)
        # Fill NaNs
        df["distance_from_last_location"] = df["distance_from_last_location"].fillna(0.0)
    else:
        # If not available, create default columns to keep pipeline stable
        df["distance_from_last_location"] = 0.0
        df["avg_txn_distance"] = 0.0
        df["suspicious_travel"] = 0

    # Safety: replace inf/nan numeric values
    num_cols = df.select_dtypes(include=[np.number]).columns
    df[num_cols] = df[num_cols].replace([np.inf, -np.inf], np.nan).fillna(0.0)

    # Save lookup of key column names as attributes (useful later)
    df.attrs["__amt_col__"] = amt_col
    df.attrs["__sender_col__"] = sender_col
    df.attrs["__receiver_col__"] = receiver_col
    df.attrs["__device_col__"] = device_col
    df.attrs["__latency_col__"] = latency_col
    df.attrs["__bandwidth_col__"] = bandwidth_col
    return df

# -------------------------
# 3. Split + sampling utilities
# -------------------------
@timed
def time_based_split(df: pd.DataFrame, test_size: float = 0.2, ts_col: str = "Timestamp") -> Tuple[pd.DataFrame, pd.DataFrame]:
    """
    Crea split temporal: train = earliest (1 - test_size) proporción, val = latest test_size proporción.
    Si no existe timestamp, cae a random stratified split.
    """
    if ts_col in df.columns:
        df_sorted = df.sort_values(ts_col).reset_index(drop=True)
        cutoff = int(len(df_sorted) * (1 - test_size))
        train_df = df_sorted.iloc[:cutoff].reset_index(drop=True)
        val_df = df_sorted.iloc[cutoff:].reset_index(drop=True)
        logger.info("Time split: train %d rows, val %d rows (cutoff index=%d)", len(train_df), len(val_df), cutoff)
    else:
        logger.warning("Timestamp missing: doing random stratified split (not ideal).")
        train_df, val_df = train_test_split(df, test_size=test_size, stratify=df["Fraud_Flag"], random_state=RANDOM_STATE)
        train_df = train_df.reset_index(drop=True)
        val_df = val_df.reset_index(drop=True)
    return train_df, val_df

@timed
def undersample_train(train_df: pd.DataFrame, target_col: str = "Fraud_Flag", neg_pos_ratio: int = 5) -> pd.DataFrame:
    """
    Para prototipo: mantenemos todos los positivos y muestreamos negativos
    para lograr una proporción más manejable para entrenamiento (reduce tiempo & estabiliza).
    neg_pos_ratio: número de negativos por cada positivo.
    """
    pos = train_df[train_df[target_col] == 1]
    neg = train_df[train_df[target_col] == 0]
    n_pos = len(pos)
    if n_pos == 0:
        raise ValueError("No positive examples in training split.")
    n_neg = min(len(neg), max(1, n_pos * neg_pos_ratio))
    neg_sampled = neg.sample(n=n_neg, random_state=RANDOM_STATE)
    train_bal = pd.concat([pos, neg_sampled]).sample(frac=1.0, random_state=RANDOM_STATE).reset_index(drop=True)
    logger.info("Undersampled train: pos=%d, neg_sampled=%d", n_pos, n_neg)
    return train_bal

# -------------------------
# 4. Encoding & scaling
# -------------------------
@timed
def encode_and_scale(train_df: pd.DataFrame, val_df: pd.DataFrame, target_col: str = "Fraud_Flag", onehot_thresh: int = 10):
    """
    - One-hot encode categorical cols with low cardinality (<= onehot_thresh)
    - Target encode high-cardinality categoricals (if TargetEncoder available)
    - Use RobustScaler on numeric features (fit on train)
    - Return X_train, X_val, y_train, y_val, metadata
    """
    metadata = {}
    # Identify categorical candidates
    cat_candidates = [c for c in train_df.columns if train_df[c].dtype == "object" or train_df[c].dtype.name == "category"]
    # Remove timestamp and target
    cat_candidates = [c for c in cat_candidates if c not in ("Timestamp", target_col)]
    # Split low/high cardinality
    low_card = [c for c in cat_candidates if train_df[c].nunique() <= onehot_thresh]
    high_card = [c for c in cat_candidates if train_df[c].nunique() > onehot_thresh]
    logger.info("Categoricals low_card: %s", low_card)
    logger.info("Categoricals high_card: %s", high_card)

    # One-hot low-card using combined to keep consistent columns
    train_tmp = train_df.copy()
    val_tmp = val_df.copy()
    if low_card:
        combined = pd.concat([train_tmp, val_tmp], axis=0, ignore_index=True)
        combined = pd.get_dummies(combined, columns=low_card, dummy_na=False, drop_first=False)
        train_tmp = combined.iloc[:len(train_tmp)].reset_index(drop=True)
        val_tmp = combined.iloc[len(train_tmp):].reset_index(drop=True)

    # Target-encode high-card: fit on train and transform both. If not available, fallback to LabelEncoder.
    encoders = {}
    if high_card:
        if TargetEncoder is None:
            # Fallback label encoding (not ideal): keep mapping for val transform but unknown classes -> -1
            for c in high_card:
                le = LabelEncoder()
                train_tmp[c] = train_tmp[c].astype(str).fillna("missing")
                val_tmp[c] = val_tmp[c].astype(str).fillna("missing")
                le.fit(pd.concat([train_tmp[c], val_tmp[c]], axis=0).unique())  # fit on combined classes to avoid unseen at transform
                train_tmp[c] = le.transform(train_tmp[c])
                val_tmp[c] = val_tmp[c].map(lambda x: le.transform([x])[0] if x in le.classes_ else -1)
                encoders[c] = ("label", le)
            logger.warning("category_encoders not installed; used LabelEncoder fallback for high-cardinality.")
        else:
            te = TargetEncoder(cols=high_card, smoothing=0.3)
            te.fit(train_tmp[high_card], train_tmp[target_col])
            train_tmp[high_card] = te.transform(train_tmp[high_card])
            val_tmp[high_card] = te.transform(val_tmp[high_card])
            encoders["target_encoder"] = te

    # Build final feature list (exclude target & timestamp & identifiers if present)
    drop_cols = [target_col, "Timestamp"]
    feature_cols = [c for c in train_tmp.columns if c not in drop_cols]
    # Remove obvious ID columns if present
    for id_cand in ["TransactionID", "Transaction_ID", "Transaction_Id", "ID"]:
        if id_cand in feature_cols:
            feature_cols.remove(id_cand)

    # Choose numeric features from feature_cols
    numeric_feat = [c for c in feature_cols if train_tmp[c].dtype != "object"]

    # Fit RobustScaler on train numeric features and transform both
    scaler = RobustScaler()
    scaler.fit(train_tmp[numeric_feat].fillna(0.0))
    train_tmp[numeric_feat] = scaler.transform(train_tmp[numeric_feat].fillna(0.0))
    val_tmp[numeric_feat] = scaler.transform(val_tmp[numeric_feat].fillna(0.0))

    X_train = train_tmp[feature_cols].copy()
    X_val = val_tmp[feature_cols].copy()
    y_train = train_tmp[target_col].astype(int).copy()
    y_val = val_tmp[target_col].astype(int).copy()

    metadata["feature_cols"] = feature_cols
    metadata["numeric_feat"] = numeric_feat
    metadata["scaler"] = scaler
    metadata["encoders"] = encoders
    return X_train, X_val, y_train, y_val, metadata

# -------------------------
# 5. Model training utilities
# -------------------------
@timed
def train_lightgbm(X_train, y_train, X_val, y_val, scale_pos_weight: float, early_stopping_rounds: int = 100):
    """
    Entrena LGBMClassifier (sklearn API) en CPU.
    `scale_pos_weight` es la proporción n_neg / n_pos para compensar el desbalance.
    """
    if not LGB_AVAILABLE:
        raise RuntimeError("LightGBM no está instalado en el entorno. Instálalo con pip install lightgbm")

    clf = lgb.LGBMClassifier(
        objective="binary",
        boosting_type="gbdt",
        learning_rate=0.02,
        num_leaves=96,
        n_estimators=4000,
        subsample=0.8,
        colsample_bytree=0.8,
        reg_alpha=0.15,
        reg_lambda=0.15,
        min_child_samples=50,
        random_state=RANDOM_STATE,
        n_jobs=-1,
        verbosity=-1,
        scale_pos_weight=scale_pos_weight,
        early_stopping_rounds=early_stopping_rounds 
    )

    # Fit with early stopping on validation set
    clf.fit(
        X_train, y_train,
        eval_set=[(X_val, y_val)],
        eval_metric="auc",
    )
    return clf

# -------------------------
# 6. Threshold selection & evaluation functions
# -------------------------
def youden_threshold(y_true: np.ndarray, y_scores: np.ndarray) -> float:
    """Devuelve el threshold que maximiza Youden's J = TPR - FPR (balance TPR-FPR)."""
    fpr, tpr, thresholds = roc_curve(y_true, y_scores)
    J = tpr - fpr
    idx = int(np.nanargmax(J))
    return float(thresholds[idx])

def best_threshold_by_f1(y_true: np.ndarray, y_scores: np.ndarray) -> Tuple[float, float]:
    """Encuentra threshold que maximiza F1 usando precision_recall_curve."""
    p, r, thr = precision_recall_curve(y_true, y_scores)
    f1 = 2 * (p * r) / (p + r + 1e-12)
    if len(f1) == 0:
        return 0.5, 0.0
    idx = int(np.nanargmax(f1))
    # thr has length len(p)-1, manejar borde
    if idx >= len(thr):
        best_thr = float(thr[-1]) if len(thr) > 0 else 0.5
    else:
        best_thr = float(thr[idx])
    return best_thr, float(f1[idx])

def evaluate_at_threshold(y_true: np.ndarray, y_scores: np.ndarray, threshold: float) -> Dict[str, Any]:
    """Calcula AUC, precision, recall, f1, fpr y matriz de confusión para un threshold dado."""
    y_pred = (y_scores >= threshold).astype(int)
    auc_score = roc_auc_score(y_true, y_scores) if len(np.unique(y_true)) > 1 else float("nan")
    prec = precision_score(y_true, y_pred, zero_division=0)
    rec = recall_score(y_true, y_pred, zero_division=0)
    f1v = f1_score(y_true, y_pred, zero_division=0)
    tn, fp, fn, tp = confusion_matrix(y_true, y_pred).ravel()
    fpr = fp / (fp + tn + 1e-12)
    return {"auc": float(auc_score), "precision": float(prec), "recall": float(rec),
            "f1": float(f1v), "fpr": float(fpr), "tp": int(tp), "fp": int(fp), "tn": int(tn), "fn": int(fn)}

# -------------------------
# 7. Plot helpers (ROC, PR, calibration, feature importance)
# -------------------------
@timed
def plot_curves(y_val, probs, output_prefix=PLOTS_DIR):
    """Dibuja y guarda ROC y Precision-Recall plots."""
    os.makedirs(output_prefix, exist_ok=True)
    # ROC
    fpr, tpr, _ = roc_curve(y_val, probs)
    roc_auc = auc(fpr, tpr)
    plt.figure(figsize=(6,5))
    plt.plot(fpr, tpr, label=f"ROC (AUC={roc_auc:.4f})")
    plt.plot([0,1],[0,1],"--", color="gray")
    plt.xlabel("FPR")
    plt.ylabel("TPR")
    plt.title("ROC Curve")
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(os.path.join(output_prefix, "roc_curve.png"))
    plt.close()

    # Precision-Recall
    precision, recall, _ = precision_recall_curve(y_val, probs)
    pr_auc = auc(recall, precision)
    plt.figure(figsize=(6,5))
    plt.plot(recall, precision, label=f"PR (AUC={pr_auc:.4f})")
    plt.xlabel("Recall")
    plt.ylabel("Precision")
    plt.title("Precision-Recall Curve")
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(os.path.join(output_prefix, "pr_curve.png"))
    plt.close()

@timed
def plot_feature_importance(model, feature_names, output_path=os.path.join(PLOTS_DIR, "feature_importance.png"), top_n=40):
    """Guarda gráfica de importancia de features según LightGBM (si disponible)."""
    try:
        importances = model.feature_importances_
        idx = np.argsort(importances)[::-1][:top_n]
        names = [feature_names[i] for i in idx]
        vals = importances[idx]
        plt.figure(figsize=(8, min(12, top_n/2)))
        plt.barh(range(len(vals))[::-1], vals[::-1], align="center")
        plt.yticks(range(len(vals))[::-1], names[::-1])
        plt.xlabel("Feature importance")
        plt.title("Top feature importances")
        plt.tight_layout()
        plt.savefig(output_path)
        plt.close()
    except Exception as e:
        logger.warning("Could not plot feature importance: %s", e)

# -------------------------
# 8. Main pipeline
# -------------------------
@timed
def run_pipeline(data_path: str = DATA_PATH,
                 neg_pos_ratio: int = 5,
                 calibrate: bool = True,
                 iso_contamination: float = 0.01,
                 ensemble_alpha: float = 0.85):
    """
    Pipeline orquestador.
    - neg_pos_ratio: negativos por positivo para undersampling training
    - calibrate: si se aplica CalibratedClassifierCV (isotonic)
    - iso_contamination: estimación de proporción de outliers para IsolationForest
    - ensemble_alpha: peso de LGBM en el ensemble (vs iso score)
    """
    # 1) Load
    df = load_data(data_path)

    # 2) Basic preprocess
    df = preprocess_basic(df)

    # 3) Feature engineering (incluye parse geolocation + spatial features)
    df = feature_engineering(df)

    # 4) Time-based split (importante para evitar leakage)
    train_full, val = time_based_split(df, test_size=0.2, ts_col="Timestamp")

    # 5) Undersample training for prototyping (keep all positives)
    train = undersample_train(train_full, target_col="Fraud_Flag", neg_pos_ratio=neg_pos_ratio)

    # 6) Encode & scale (fit on train only)
    X_train, X_val, y_train, y_val, metadata = encode_and_scale(train, val, target_col="Fraud_Flag", onehot_thresh=10)
    feature_cols = metadata["feature_cols"]
    numeric_feat = metadata["numeric_feat"]
    logger.info("Using %d features for training.", len(feature_cols))

    # 7) Train IsolationForest on train numeric features and produce anomaly score on val
    iso = IsolationForest(n_estimators=200, contamination=iso_contamination, random_state=RANDOM_STATE, n_jobs=-1)
    iso.fit(X_train[numeric_feat].fillna(-1))
    joblib.dump(iso, os.path.join(MODEL_DIR, "isoforest.joblib"))
    iso_scores_val = -iso.decision_function(X_val[numeric_feat].fillna(-1))  # invert so higher = more anomalous
    # Scale iso scores to [0,1] for ensembling
    iso_min, iso_max = np.min(iso_scores_val), np.max(iso_scores_val)
    if iso_max - iso_min > 1e-9:
        iso_scaled = (iso_scores_val - iso_min) / (iso_max - iso_min)
    else:
        iso_scaled = np.zeros_like(iso_scores_val)

    # 8) Compute scale_pos_weight for LGBM from undersampled train
    pos = int(y_train.sum())
    neg = int((y_train == 0).sum())
    scale_pos_weight = float(neg / max(1, pos))
    logger.info("scale_pos_weight for LGBM: %.3f (pos=%d, neg=%d)", scale_pos_weight, pos, neg)

    # 9) Train LightGBM on undersampled training (sklearn API)
    clf = train_lightgbm(X_train[feature_cols], y_train, X_val[feature_cols], y_val, scale_pos_weight=scale_pos_weight, early_stopping_rounds=100)
    joblib.dump(clf, os.path.join(MODEL_DIR, "lgbm_model.joblib"))

    # 10) Predict probabilities on validation
    if hasattr(clf, "predict_proba"):
        yval_proba = clf.predict_proba(X_val[feature_cols])[:, 1]
    else:
        yval_proba = clf.predict(X_val[feature_cols])

    # 11) Ensemble: weighted average LGBM_prob and Iso anomaly (iso_scaled)
    ensembled_prob = ensemble_alpha * yval_proba + (1.0 - ensemble_alpha) * iso_scaled

    # 12) Threshold selection: Youden & best-F1
    thr_youden = youden_threshold(y_val.values, ensembled_prob)
    thr_f1, best_f1 = best_threshold_by_f1(y_val.values, ensembled_prob)
    metrics_youden = evaluate_at_threshold(y_val.values, ensembled_prob, thr_youden)
    metrics_f1 = evaluate_at_threshold(y_val.values, ensembled_prob, thr_f1)
    logger.info("Youden threshold %.4f -> metrics: %s", thr_youden, json.dumps(metrics_youden))
    logger.info("F1 threshold %.4f (best_f1=%.4f) -> metrics: %s", thr_f1, best_f1, json.dumps(metrics_f1))

    # 13) Calibration (optional)
    calibrated_info = {}
    if calibrate:
        try:
            calibrator = CalibratedClassifierCV(base_estimator=clf, method="isotonic", cv=3)
            calibrator.fit(X_train[feature_cols], y_train)
            cal_probs = calibrator.predict_proba(X_val[feature_cols])[:, 1]
            cal_ensembled = ensemble_alpha * cal_probs + (1 - ensemble_alpha) * iso_scaled
            thr_cal = youden_threshold(y_val.values, cal_ensembled)
            metrics_cal = evaluate_at_threshold(y_val.values, cal_ensembled, thr_cal)
            calibrated_info = {"calibrator_saved": True, "thr_cal": thr_cal, "metrics_cal": metrics_cal}
            joblib.dump(calibrator, os.path.join(MODEL_DIR, "calibrator_isotonic.joblib"))
            logger.info("Calibration succeeded: thr_cal %.4f -> %s", thr_cal, json.dumps(metrics_cal))
        except Exception as e:
            logger.exception("Calibration failed: %s", e)
            calibrated_info = {"calibrator_saved": False, "error": str(e)}

    # 14) Save artifacts & metrics
    joblib.dump(feature_cols, os.path.join(MODEL_DIR, "feature_cols.joblib"))
    joblib.dump(metadata.get("scaler"), os.path.join(MODEL_DIR, "scaler.joblib"))
    joblib.dump(metadata.get("encoders"), os.path.join(MODEL_DIR, "encoders.joblib"))

    out = {
        "train_full_rows": int(len(train_full)),
        "train_used_rows": int(len(train)),
        "val_rows": int(len(val)),
        "scale_pos_weight": scale_pos_weight,
        "ensemble_alpha": ensemble_alpha,
        "youden_threshold": thr_youden,
        "youden_metrics": metrics_youden,
        "f1_threshold": thr_f1,
        "f1_metrics": metrics_f1,
        "best_f1_value": best_f1,
        "calibrated_info": calibrated_info,
        "raw_auc_lgbm": float(roc_auc_score(y_val.values, yval_proba)) if len(np.unique(y_val.values))>1 else None
    }
    with open(os.path.join(OUTPUT_DIR, "model_metrics.json"), "w") as f:
        json.dump(out, f, indent=2)

    # 15) Visualizations
    plot_curves(y_val.values, ensembled_prob, output_prefix=PLOTS_DIR)
    plot_feature_importance(clf, feature_cols, output_path=os.path.join(PLOTS_DIR, "feature_importance.png"))

    logger.info("Saved artifacts in %s", OUTPUT_DIR)
    return out

# -------------------------
# 9. Run if invoked directly
# -------------------------
if __name__ == "__main__":
    start_time = time.time()
    logger.info("Starting training pipeline (local CPU) with geolocation-enhanced features.")
    metrics = run_pipeline(data_path=DATA_PATH, neg_pos_ratio=5, calibrate=True, iso_contamination=0.01, ensemble_alpha=0.85)
    logger.info("Pipeline finished in %.2fs. Metrics summary:\n%s", time.time() - start_time, json.dumps(metrics, indent=2))
    print(json.dumps(metrics, indent=2))
