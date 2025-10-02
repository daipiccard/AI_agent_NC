import { useEffect, useState } from "react";
import "./styles.css"; // 👈 importa las clases limpias

// Cambiá esto por el endpoint real cuando lo tengas:
const ENDPOINT = "http://127.0.0.1:8000/alerts";

export default function App() {
  const [items, setItems] = useState([]);
  const [status, setStatus] = useState("Listo");
  const [error, setError] = useState("");

  async function fetchAlerts() {
    setStatus("Cargando…");
    setError("");
    try {
      const res = await fetch(ENDPOINT, { headers: { Accept: "application/json" } });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      const arr = Array.isArray(data) ? data : data.items || data.alerts || [];
      setItems(arr);
      setStatus("Actualizado");
    } catch (e) {
      setError(`Error al consultar ${ENDPOINT}: ${e.message}`);
      setStatus("Error");
    }
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