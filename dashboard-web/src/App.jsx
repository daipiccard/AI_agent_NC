import React from "react";

import { useEffect, useState } from "react";
import "./styles.css"; // 👈 importa las clases limpias

// Usa una variable de entorno para apuntar al backend en deploys (por ejemplo, Vercel)
// y hace fallback al backend local solo durante el desarrollo en localhost. Si no se
// cumple ninguna de las condiciones anteriores, utiliza un archivo estático incluido
// en la aplicación para evitar errores 404 en producción.
const CONFIGURED_BASE = (import.meta.env.VITE_ALERTS_BASE_URL || "").trim().replace(/\/$/, "");

function resolveBackendEndpoint() {
  if (CONFIGURED_BASE) {
    return `${CONFIGURED_BASE}/alerts`;
  }

  if (typeof window !== "undefined" && window.location.hostname === "localhost") {
    return "http://127.0.0.1:8001/alerts";
  }

  return null;
}

const BACKEND_ENDPOINT = resolveBackendEndpoint();
const FALLBACK_ENDPOINT = "/alerts.json";

export default function App() {
  const [items, setItems] = useState([]);
  const [status, setStatus] = useState("Listo");
  const [error, setError] = useState("");
  const [source, setSource] = useState(BACKEND_ENDPOINT || FALLBACK_ENDPOINT);

  async function fetchAlerts() {
    setStatus("Cargando…");
    setError("");
    const tried = [];
    let lastError = null;

    const candidates = [];
    if (BACKEND_ENDPOINT) {
      candidates.push(BACKEND_ENDPOINT);
    }
    if (source && !candidates.includes(source)) {
      candidates.push(source);
    }
    if (!candidates.includes(FALLBACK_ENDPOINT)) {
      candidates.push(FALLBACK_ENDPOINT);
    }

    for (const url of candidates) {
      tried.push(url);
      try {
        const res = await fetch(url, { headers: { Accept: "application/json" } });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        const arr = Array.isArray(data) ? data : data.items || data.alerts || [];
        setItems(arr);
        setSource(url);
        setStatus(url === FALLBACK_ENDPOINT ? "Datos de ejemplo" : "Actualizado (API)");
        return;
      } catch (e) {
        lastError = e;
      }
    }

    const target = tried.at(-1) ?? BACKEND_ENDPOINT ?? FALLBACK_ENDPOINT;
    setError(`Error al consultar ${target}: ${lastError?.message ?? "sin detalles"}`);
    setStatus("Error");
  }

  useEffect(() => {
    fetchAlerts();
    const t = setInterval(fetchAlerts, 5000);
    return () => clearInterval(t);
  }, []);

  const fmtMoney = (v) =>
    new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 0 })
      .format(Number(v) || 0);

  // Mapea “bandera” a clases CSS de estado
  function estadoClass(bandera, sospechosa) {
    const s = (bandera ?? (sospechosa ? "sospechoso" : "ok")).toString().toLowerCase();
    if (s.includes("sosp")) return "pill estado-sospechosa";
    if (s.includes("review")) return "pill estado-review";
    return "pill estado-ok";
  }

  function EstadoPill({ bandera, sospechosa }) {
    const cls = estadoClass(bandera, sospechosa);
    const label = cls.includes("sospechosa") ? "SOSPECHOSA" : cls.includes("review") ? "REVIEW" : "OK";
    return <span className={cls} data-test="estado-pill">{label}</span>;
  }

  return (
    <div className="wrap">
      <header className="header">
        <h1 className="title">MVP Fraude — Dashboard</h1>
        <div className="row">
          <button className="btn" onClick={fetchAlerts}>Refrescar</button>
          <span className="status">{status}</span>
        </div>
        {error && <div className="msg-error">{error}</div>}
      </header>

      <table className="tabla">
        <thead>
          <tr>
            <th>ID</th><th>Monto</th><th>Fecha</th><th>Hora</th><th>Ubicación</th><th>Estado</th>
          </tr>
        </thead>
        <tbody>
          {items.length === 0 ? (
            <tr><td colSpan="6" className="muted">Sin datos…</td></tr>
          ) : (
            items.map((it, i) => {
              const id = it.id || it.tx_id || "-";
              const monto = fmtMoney(it.monto);
              const fecha = it.fecha || "-";
              const hora = it.hora || "-";
              const ubic = it.ubicacion || it.pais || "-";
              return (
                <tr key={i}>
                  <td>{id}</td>
                  <td>{monto}</td>
                  <td>{fecha}</td>
                  <td>{hora}</td>
                  <td>{ubic}</td>
                  <td><EstadoPill bandera={it.bandera} sospechosa={it.sospechosa} /></td>
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
}