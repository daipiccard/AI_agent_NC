"""
Fraud Detection Model Training Pipeline

This script implements a complete pipeline for training a fraud detection model
using the banking transaction dataset. It includes data loading, preprocessing,
feature engineering, model training, evaluation, and saving results.
"""

import pandas as pd
import numpy as np
import logging
from datetime import datetime
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler, RobustScaler, LabelEncoder
from sklearn.metrics import (
    roc_auc_score, precision_score, recall_score, f1_score,
    confusion_matrix, classification_report, roc_curve, precision_recall_curve
)
from sklearn.calibration import CalibratedClassifierCV
import lightgbm as lgb
import joblib
import json
import warnings
warnings.filterwarnings('ignore')

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# Set random seed for reproducibility
RANDOM_STATE = 42
np.random.seed(RANDOM_STATE)

def load_data(file_path='data/transaction_data.csv'):
    """
    Load and perform initial exploration of the transaction dataset.

    Args:
        file_path (str): Path to the CSV file

    Returns:
        pd.DataFrame: Loaded dataframe with initial data types
    """
    logger.info("Loading dataset from %s", file_path)

    # Load data
    df = pd.read_csv(file_path)

    logger.info("Dataset shape: %s", df.shape)
    logger.info("Columns: %s", list(df.columns))

    # Basic info
    logger.info("Data types and non-null counts:")
    df.info()

    # Describe numerical columns
    logger.info("Numerical columns description:")
    print(df.describe())

    # Describe categorical columns
    logger.info("Categorical columns description:")
    categorical_cols = df.select_dtypes(include=['object']).columns
    for col in categorical_cols:
        logger.info("Unique values in %s: %d", col, df[col].nunique())
        logger.info("Top 5 values in %s:", col)
        print(df[col].value_counts().head())

    # Check for missing values
    logger.info("Missing values per column:")
    print(df.isnull().sum())

    # Fraud distribution
    fraud_counts = df['Fraud Flag'].value_counts()
    fraud_percentage = df['Fraud Flag'].value_counts(normalize=True) * 100
    logger.info("Fraud distribution:")
    for flag, count in fraud_counts.items():
        logger.info("  %s: %d (%.2f%%)", flag, count, fraud_percentage[flag])

    # Convert data types
    df['Timestamp'] = pd.to_datetime(df['Timestamp'])
    df['Fraud Flag'] = df['Fraud Flag'].astype(bool)
    df['Transaction Amount'] = df['Transaction Amount'].astype(float)
    df['Latency (ms)'] = df['Latency (ms)'].astype(int)
    df['Slice Bandwidth (Mbps)'] = df['Slice Bandwidth (Mbps)'].astype(int)
    df['PIN Code'] = df['PIN Code'].astype(str)  # Treat as categorical

    # Parse geolocation
    def parse_geolocation(geo_str):
        try:
            parts = geo_str.replace('N', '').replace('W', '').replace('S', '').replace('E', '').split(',')
            lat = float(parts[0].strip())
            lon = float(parts[1].strip())
            return lat, lon
        except:
            return np.nan, np.nan

    df['Latitude'], df['Longitude'] = zip(*df['Geolocation (Latitude/Longitude)'].apply(parse_geolocation))

    # Drop original geolocation column
    df = df.drop('Geolocation (Latitude/Longitude)', axis=1)

    logger.info("Data loading and initial exploration completed")
    return df

def preprocess_data(df):
    """
    Preprocess the data: handle missing values, encode categoricals, scale features.

    Args:
        df (pd.DataFrame): Input dataframe

    Returns:
        pd.DataFrame: Preprocessed dataframe
    """
    logger.info("Starting data preprocessing")

    # Handle missing values
    df = df.dropna()  # Simple drop for now, could be improved
    logger.info("After dropping missing values: %s", df.shape)

    # Remove duplicates
    df = df.drop_duplicates()
    logger.info("After dropping duplicates: %s", df.shape)

    # Outlier handling for Transaction Amount (simple IQR method)
    Q1 = df['Transaction Amount'].quantile(0.25)
    Q3 = df['Transaction Amount'].quantile(0.75)
    IQR = Q3 - Q1
    lower_bound = Q1 - 1.5 * IQR
    upper_bound = Q3 + 1.5 * IQR
    df = df[(df['Transaction Amount'] >= lower_bound) & (df['Transaction Amount'] <= upper_bound)]
    logger.info("After outlier removal: %s", df.shape)

    # Temporal features from Timestamp
    df['hour'] = df['Timestamp'].dt.hour
    df['dayofweek'] = df['Timestamp'].dt.dayofweek
    df['month'] = df['Timestamp'].dt.month
    df['is_weekend'] = df['dayofweek'].isin([5, 6]).astype(int)

    # Sort by timestamp for temporal split
    df = df.sort_values('Timestamp').reset_index(drop=True)

    # Temporal split: 80% train, 20% validation
    split_idx = int(len(df) * 0.8)
    train_df = df.iloc[:split_idx].copy()
    val_df = df.iloc[split_idx:].copy()

    logger.info("Train set: %s, Validation set: %s", train_df.shape, val_df.shape)

    # Categorical encoding
    categorical_cols = ['Transaction Type', 'Transaction Status', 'Device Used', 'Network Slice ID']

    # Target encoding for high cardinality
    high_cardinality_cols = ['Sender Account ID', 'Receiver Account ID']
    for col in high_cardinality_cols:
        # Simple target encoding
        target_mean = train_df.groupby(col)['Fraud Flag'].mean()
        train_df[f'{col}_encoded'] = train_df[col].map(target_mean).fillna(train_df['Fraud Flag'].mean())
        val_df[f'{col}_encoded'] = val_df[col].map(target_mean).fillna(train_df['Fraud Flag'].mean())

    # One-hot encoding for low cardinality
    for col in categorical_cols:
        dummies = pd.get_dummies(train_df[col], prefix=col, drop_first=True)
        train_df = pd.concat([train_df, dummies], axis=1)
        val_dummies = pd.get_dummies(val_df[col], prefix=col, drop_first=True)
        # Ensure same columns
        for dummy_col in dummies.columns:
            if dummy_col not in val_dummies.columns:
                val_dummies[dummy_col] = 0
        val_dummies = val_dummies[dummies.columns]  # Reorder
        val_df = pd.concat([val_df, val_dummies], axis=1)

    # Scaling numerical features
    numerical_cols = ['Transaction Amount', 'Latitude', 'Longitude', 'Latency (ms)', 'Slice Bandwidth (Mbps)']
    scaler = RobustScaler()
    train_df[numerical_cols] = scaler.fit_transform(train_df[numerical_cols])
    val_df[numerical_cols] = scaler.transform(val_df[numerical_cols])

    # Save scaler
    joblib.dump(scaler, 'scaler.pkl')

    logger.info("Data preprocessing completed")
    return train_df, val_df

def feature_engineering(train_df, val_df):
    """
    Create advanced features for fraud detection.

    Args:
        train_df (pd.DataFrame): Training dataframe
        val_df (pd.DataFrame): Validation dataframe

    Returns:
        tuple: (train_df, val_df) with new features
    """
    logger.info("Starting feature engineering")

    # Combine for feature calculation, then split back
    combined_df = pd.concat([train_df, val_df], ignore_index=True)

    # Temporal features
    combined_df = combined_df.sort_values(['Sender Account ID', 'Timestamp'])
    combined_df['time_since_last_txn_sender'] = combined_df.groupby('Sender Account ID')['Timestamp'].diff().dt.total_seconds().fillna(0)

    # Simplified rolling count - count transactions in last 5 minutes per sender
    # Using a simpler approach to avoid pandas rolling issues
    combined_df['txn_count_last_5min_sender'] = 0  # Initialize
    for sender in combined_df['Sender Account ID'].unique():
        mask = combined_df['Sender Account ID'] == sender
        sender_data = combined_df[mask].copy()
        sender_data = sender_data.set_index('Timestamp')
        rolling_counts = sender_data.rolling('5min').count()['Transaction Amount']
        combined_df.loc[mask, 'txn_count_last_5min_sender'] = rolling_counts.values

    # Account-based features
    sender_stats = combined_df.groupby('Sender Account ID')['Transaction Amount'].agg(['count', 'mean', 'std']).fillna(0)
    sender_stats.columns = ['sender_txn_count', 'sender_avg_amount', 'sender_std_amount']
    combined_df = combined_df.merge(sender_stats, left_on='Sender Account ID', right_index=True, how='left')

    receiver_stats = combined_df.groupby('Receiver Account ID')['Transaction Amount'].agg(['count', 'mean']).fillna(0)
    receiver_stats.columns = ['receiver_txn_count', 'receiver_avg_amount']
    combined_df = combined_df.merge(receiver_stats, left_on='Receiver Account ID', right_index=True, how='left')

    # Unique receivers per sender
    unique_receivers = combined_df.groupby('Sender Account ID')['Receiver Account ID'].nunique()
    combined_df['unique_receivers'] = combined_df['Sender Account ID'].map(unique_receivers).fillna(0)

    # Geographical features
    combined_df['distance_from_last_location'] = combined_df.groupby('Sender Account ID')[['Latitude', 'Longitude']].diff().pow(2).sum(axis=1).pow(0.5).fillna(0)
    combined_df['avg_txn_distance'] = combined_df.groupby('Sender Account ID')['distance_from_last_location'].expanding().mean().reset_index(0, drop=True).fillna(0)

    # Device and network features
    combined_df['bandwidth_ratio'] = combined_df['Slice Bandwidth (Mbps)'] / (combined_df['Latency (ms)'] + 1)  # Avoid division by zero

    # Transaction features
    combined_df['amount_zscore_sender'] = combined_df.groupby('Sender Account ID')['Transaction Amount'].transform(lambda x: (x - x.mean()) / (x.std() + 1e-8))
    combined_df['is_high_value'] = (combined_df['Transaction Amount'] > combined_df['Transaction Amount'].quantile(0.95)).astype(int)

    # PIN features
    combined_df['pin_entropy'] = combined_df['PIN Code'].apply(lambda x: len(set(str(x))) / len(str(x)) if len(str(x)) > 0 else 0)
    pin_counts = combined_df.groupby('Sender Account ID')['PIN Code'].value_counts()
    combined_df['pin_reuse_count'] = combined_df.apply(lambda row: pin_counts.get((row['Sender Account ID'], row['PIN Code']), 0), axis=1)

    # Anomaly score using Isolation Forest (simplified)
    from sklearn.ensemble import IsolationForest
    iso_features = ['Transaction Amount', 'Latency (ms)', 'Slice Bandwidth (Mbps)']
    iso = IsolationForest(random_state=RANDOM_STATE, contamination=0.1)
    combined_df['anomaly_score'] = iso.fit_predict(combined_df[iso_features])
    combined_df['anomaly_score'] = (combined_df['anomaly_score'] == -1).astype(int)  # Convert to binary

    # Split back
    train_df = combined_df.iloc[:len(train_df)].copy()
    val_df = combined_df.iloc[len(train_df):].copy()

    logger.info("Feature engineering completed")
    return train_df, val_df

def train_model(train_df, val_df):
    """
    Train the LightGBM model with hyperparameter tuning.

    Args:
        train_df (pd.DataFrame): Training dataframe
        val_df (pd.DataFrame): Validation dataframe

    Returns:
        tuple: (model, feature_names)
    """
    logger.info("Starting model training")

    # Define features (exclude non-feature columns)
    exclude_cols = ['Transaction ID', 'Sender Account ID', 'Receiver Account ID', 'Timestamp', 'Fraud Flag', 'PIN Code',
                   'Transaction Type', 'Transaction Status', 'Device Used', 'Network Slice ID']  # Exclude original categorical columns
    feature_cols = [col for col in train_df.columns if col not in exclude_cols]

    X_train = train_df[feature_cols]
    y_train = train_df['Fraud Flag']
    X_val = val_df[feature_cols]
    y_val = val_df['Fraud Flag']

    # Calculate class weights
    class_weights = len(y_train) / (2 * np.bincount(y_train))
    scale_pos_weight = class_weights[1] / class_weights[0]

    # LightGBM parameters
    params = {
        'objective': 'binary',
        'metric': 'auc',
        'boosting_type': 'gbdt',
        'num_leaves': 31,
        'learning_rate': 0.05,
        'feature_fraction': 0.9,
        'lambda_l1': 0.1,
        'lambda_l2': 0.1,
        'scale_pos_weight': scale_pos_weight,
        'random_state': RANDOM_STATE,
        'verbosity': -1
    }

    # Create datasets
    train_data = lgb.Dataset(X_train, label=y_train)
    val_data = lgb.Dataset(X_val, label=y_val, reference=train_data)

    # Train model with callback for early stopping
    from lightgbm import early_stopping, log_evaluation
    model = lgb.train(
        params,
        train_data,
        num_boost_round=1000,
        valid_sets=[train_data, val_data],
        callbacks=[early_stopping(50), log_evaluation(0)]
    )

    # Skip calibration for now - return the model directly
    # Calibration can be added later with proper sklearn wrapper
    calibrated_model = model

    logger.info("Model training completed")
    return calibrated_model, feature_cols

def evaluate_model(model, val_df, feature_cols):
    """
    Evaluate the trained model and generate metrics.

    Args:
        model: Trained model
        val_df (pd.DataFrame): Validation dataframe
        feature_cols (list): Feature column names

    Returns:
        dict: Evaluation metrics
    """
    logger.info("Starting model evaluation")

    X_val = val_df[feature_cols]
    y_val = val_df['Fraud Flag']

    # Predictions
    y_pred_proba = model.predict(X_val)
    y_pred = (y_pred_proba >= 0.5).astype(int)

    # Find optimal threshold
    fpr, tpr, thresholds = roc_curve(y_val, y_pred_proba)
    optimal_idx = np.argmax(tpr - fpr)
    optimal_threshold = thresholds[optimal_idx]

    # Predictions with optimal threshold
    y_pred_opt = (y_pred_proba >= optimal_threshold).astype(int)

    # Metrics
    metrics = {
        'auc': roc_auc_score(y_val, y_pred_proba),
        'precision': precision_score(y_val, y_pred_opt),
        'recall': recall_score(y_val, y_pred_opt),
        'f1': f1_score(y_val, y_pred_opt),
        'optimal_threshold': optimal_threshold
    }

    # Confusion matrix
    cm = confusion_matrix(y_val, y_pred_opt)
    metrics['confusion_matrix'] = cm.tolist()

    # Classification report
    report = classification_report(y_val, y_pred_opt, output_dict=True)
    metrics['classification_report'] = report

    # Feature importance
    feature_importance = dict(zip(feature_cols, model.feature_importance()))
    metrics['feature_importance'] = feature_importance

    logger.info("AUC: %.4f", metrics['auc'])
    logger.info("F1 Score: %.4f", metrics['f1'])
    logger.info("Optimal Threshold: %.4f", metrics['optimal_threshold'])

    return metrics

def save_results(model, metrics, feature_cols):
    """
    Save the trained model and evaluation metrics.

    Args:
        model: Trained model
        metrics (dict): Evaluation metrics
        feature_cols (list): Feature column names
    """
    logger.info("Saving model and results")

    # Save model
    joblib.dump(model, 'fraud_model_lgbm.pkl')

    # Convert numpy types to Python types for JSON serialization
    def convert_numpy_types(obj):
        if isinstance(obj, np.integer):
            return int(obj)
        elif isinstance(obj, np.floating):
            return float(obj)
        elif isinstance(obj, np.ndarray):
            return obj.tolist()
        elif isinstance(obj, dict):
            return {key: convert_numpy_types(value) for key, value in obj.items()}
        elif isinstance(obj, list):
            return [convert_numpy_types(item) for item in obj]
        else:
            return obj

    # Save metrics
    with open('model_metrics.json', 'w') as f:
        json.dump(convert_numpy_types(metrics), f, indent=4)

    # Save feature list
    with open('feature_list.json', 'w') as f:
        json.dump(feature_cols, f, indent=4)

    logger.info("Model and results saved successfully")

def predict_new_transaction(model, transaction_data, feature_cols):
    """
    Predict fraud probability for a new transaction.

    Args:
        model: Trained model
        transaction_data (dict): Transaction data
        feature_cols (list): Feature column names

    Returns:
        float: Fraud probability
    """
    # This is a placeholder - would need full preprocessing pipeline
    # For demonstration purposes only
    logger.info("Predicting fraud for new transaction")
    # In a real implementation, this would preprocess the transaction_data
    # to match the training features
    return 0.5  # Placeholder

if __name__ == "__main__":
    # Main execution
    logger.info("Starting fraud detection model training pipeline")

    # Load data
    df = load_data()

    # Preprocess
    train_df, val_df = preprocess_data(df)

    # Feature engineering
    train_df, val_df = feature_engineering(train_df, val_df)

    # Train model
    model, feature_cols = train_model(train_df, val_df)

    # Evaluate
    metrics = evaluate_model(model, val_df, feature_cols)

    # Save results
    save_results(model, metrics, feature_cols)

    logger.info("Pipeline completed successfully")