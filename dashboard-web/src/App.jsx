import React, { useState, useEffect } from "react";
import "./index.css";          // tu CSS principal dentro de src
import "./myStyles/globals.css"; // mi CSS del Figma, ruta correcta
const ENDPOINT = "/alerts.json"; // o "http://127.0.0.1:8000/alerts

// Componentes Figma
import { Button } from "./components/ui/button";
import { Badge } from "./components/ui/badge";
import { FraudStats } from "./components/FraudStats";
import { FraudChart } from "./components/FraudChart";
import { AlertsPanel } from "./components/AlertsPanel";
import { TransactionsTable } from "./components/TransactionsTable";
import { ConfigPanel } from "./components/ConfigPanel";
import { RiskDistribution } from "./components/RiskDistribution";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger } from "./components/ui/sheet";


// Iconos
import { Shield, Menu, Bell, Download, LayoutDashboard, AlertTriangle, ListFilter, Settings, Home } from "lucide-react";
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
  // ✅ Lógica del equipo
  const [items, setItems] = useState([]);
  const [status, setStatus] = useState("Listo");
  const [error, setError] = useState("");

  async function fetchAlerts() {
    setStatus("Cargando…");
    setError("");
    try {
      const res = await fetch("/alerts.json");
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      const arr = Array.isArray(data) ? data : data.items || data.alerts || [];
      setItems(arr);
      setStatus("Actualizado");
    } catch (e) {
      setError(`Error al consultar: ${e.message}`);
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

  // ✅ Lógica del menú Figma
  const [menuOpen, setMenuOpen] = useState(false);
  const scrollToSection = (id) => {
    const el = document.getElementById(id);
    if (el) {
      el.scrollIntoView({ behavior: "smooth", block: "start" });
      setMenuOpen(false);
    }
  };

  const menuItems = [
    { id: "dashboard", label: "Panel de Monitoreo", icon: <Home className="h-5 w-5" /> },
    { id: "analytics", label: "Análisis y Gráficos", icon: <LayoutDashboard className="h-5 w-5" /> },
    { id: "alerts", label: "Alertas Activas", icon: <AlertTriangle className="h-5 w-5" /> },
    { id: "transactions", label: "Transacciones", icon: <ListFilter className="h-5 w-5" /> },
    { id: "config", label: "Configuración", icon: <Settings className="h-5 w-5" /> },
  ];

  // ✅ JSX Figma + tabla de alertas del equipo integrada
  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container flex h-16 items-center justify-between gap-3 px-4">
          <div className="flex items-center gap-2 md:gap-3 min-w-0">
            <div className="flex items-center justify-center h-9 w-9 md:h-10 md:w-10 rounded-lg bg-primary text-primary-foreground flex-shrink-0">
              <Shield className="h-5 w-5 md:h-6 md:w-6" />
            </div>
            <div className="min-w-0">
              <h1 className="text-base md:text-lg truncate">FraudGuard AI</h1>
              <p className="text-xs text-muted-foreground hidden sm:block">Sistema de Detección de Fraude</p>
            </div>
          </div>
          
          <div className="flex items-center gap-2 md:gap-4 flex-shrink-0">
          <Badge variant="outline" className="gap-2 hidden lg:flex items-center">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-500 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
          </span>
          Sistema Activo
          </Badge>
            <Button variant="ghost" size="icon" className="relative h-9 w-9 md:h-10 md:w-10">
              <Bell className="h-4 w-4 md:h-5 md:w-5" />
              <span className="absolute top-1 right-1 h-2 w-2 rounded-full bg-destructive"></span>
            </Button>
            
            <Sheet open={menuOpen} onOpenChange={setMenuOpen}>
              <SheetTrigger asChild>
                <Button variant="outline" size="icon" className="h-9 w-9 md:h-10 md:w-10">
                  <Menu className="h-4 w-4 md:h-5 md:w-5" />
                </Button>
              </SheetTrigger>
              <SheetContent>
                <SheetHeader>
                  <SheetTitle className="flex items-center gap-2">
                    <Shield className="h-5 w-5 text-primary" />
                    FraudGuard AI
                  </SheetTitle>
                  <SheetDescription>
                    Navegación del sistema
                  </SheetDescription>
                </SheetHeader>
                
                <nav className="mt-8 space-y-2">
                  {menuItems.map((item) => (
                    <button
                      key={item.id}
                      onClick={() => scrollToSection(item.id)}
                      className="w-full flex items-center gap-3 rounded-lg px-3 py-3 text-left transition-colors hover:bg-accent hover:text-accent-foreground"
                    >
                      <span className="text-muted-foreground">{item.icon}</span>
                      <span>{item.label}</span>
                    </button>
                  ))}
                </nav>
              </SheetContent>
            </Sheet>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container mx-auto p-6 space-y-6">
        {/* Stats Overview */}
        <div id="dashboard" className="scroll-mt-20">
          <div className="flex items-center justify-between">
            <div>
              <h2>Panel de Monitoreo</h2>
              <p className="text-sm text-muted-foreground mt-1">
                Visualización en tiempo real de actividad y detecciones
              </p>
            </div>
            <Button variant="outline" className="gap-2 hidden sm:flex">
              <Download className="h-4 w-4" />
              Exportar Reporte
            </Button>
          </div>

          <div className="mt-6">
            <FraudStats />
          </div>
        </div>

        {/* Charts Section */}
        <div id="analytics" className="grid gap-6 md:grid-cols-7 scroll-mt-20">
          <FraudChart />
          <RiskDistribution />
        </div>

        {/* Alerts Section (tabla del equipo) */}
        <div id="alerts" className="grid gap-6 md:grid-cols-7 scroll-mt-20">
          <AlertsPanel />
          <div id="transactions" className="col-span-3 scroll-mt-20">
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
        </div>

        {/* Config Panel */}
        <div id="config" className="grid gap-6 md:grid-cols-7 scroll-mt-20">
          <ConfigPanel />
        </div>
      </main>
    </div>
  );
}


// import React from "react";

// import { useEffect, useState } from "react";
// import "./styles.css"; // 👈 importa las clases limpias

// // Usa una variable de entorno para apuntar al backend en deploys (por ejemplo, Vercel)
// // y hace fallback al backend local solo durante el desarrollo en localhost. Si no se
// // cumple ninguna de las condiciones anteriores, utiliza un archivo estático incluido
// // en la aplicación para evitar errores 404 en producción.
// const CONFIGURED_BASE = (import.meta.env.VITE_ALERTS_BASE_URL || "").trim().replace(/\/$/, "");

// function resolveBackendEndpoint() {
//   if (CONFIGURED_BASE) {
//     return `${CONFIGURED_BASE}/alerts`;
//   }

//   if (typeof window !== "undefined" && window.location.hostname === "localhost") {
//     return "http://127.0.0.1:8001/alerts";
//   }

//   return null;
// }

// const BACKEND_ENDPOINT = resolveBackendEndpoint();
// const FALLBACK_ENDPOINT = "/alerts.json";

// export default function App() {
//   const [items, setItems] = useState([]);
//   const [status, setStatus] = useState("Listo");
//   const [error, setError] = useState("");
//   const [source, setSource] = useState(BACKEND_ENDPOINT || FALLBACK_ENDPOINT);

//   async function fetchAlerts() {
//     setStatus("Cargando…");
//     setError("");
//     const tried = [];
//     let lastError = null;

//     const candidates = [];
//     if (BACKEND_ENDPOINT) {
//       candidates.push(BACKEND_ENDPOINT);
//     }
//     if (source && !candidates.includes(source)) {
//       candidates.push(source);
//     }
//     if (!candidates.includes(FALLBACK_ENDPOINT)) {
//       candidates.push(FALLBACK_ENDPOINT);
//     }

//     for (const url of candidates) {
//       tried.push(url);
//       try {
//         const res = await fetch(url, { headers: { Accept: "application/json" } });
//         if (!res.ok) throw new Error(`HTTP ${res.status}`);
//         const data = await res.json();
//         const arr = Array.isArray(data) ? data : data.items || data.alerts || [];
//         setItems(arr);
//         setSource(url);
//         setStatus(url === FALLBACK_ENDPOINT ? "Datos de ejemplo" : "Actualizado (API)");
//         return;
//       } catch (e) {
//         lastError = e;
//       }
//     }

//     const target = tried.at(-1) ?? BACKEND_ENDPOINT ?? FALLBACK_ENDPOINT;
//     setError(`Error al consultar ${target}: ${lastError?.message ?? "sin detalles"}`);
//     setStatus("Error");
//   }

//   useEffect(() => {
//     fetchAlerts();
//     const t = setInterval(fetchAlerts, 5000);
//     return () => clearInterval(t);
//   }, []);

//   const fmtMoney = (v) =>
//     new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 0 })
//       .format(Number(v) || 0);

//   // Mapea “bandera” a clases CSS de estado
//   function estadoClass(bandera, sospechosa) {
//     const s = (bandera ?? (sospechosa ? "sospechoso" : "ok")).toString().toLowerCase();
//     if (s.includes("sosp")) return "pill estado-sospechosa";
//     if (s.includes("review")) return "pill estado-review";
//     return "pill estado-ok";
//   }

//   function EstadoPill({ bandera, sospechosa }) {
//     const cls = estadoClass(bandera, sospechosa);
//     const label = cls.includes("sospechosa") ? "SOSPECHOSA" : cls.includes("review") ? "REVIEW" : "OK";
//     return <span className={cls} data-test="estado-pill">{label}</span>;
//   }

//   return (
//     <div className="wrap">
//       <header className="header">
//         <h1 className="title">MVP Fraude — Dashboard</h1>
//         <div className="row">
//           <button className="btn" onClick={fetchAlerts}>Refrescar</button>
//           <span className="status">{status}</span>
//         </div>
//         {error && <div className="msg-error">{error}</div>}
//       </header>

//       <table className="tabla">
//         <thead>
//           <tr>
//             <th>ID</th><th>Monto</th><th>Fecha</th><th>Hora</th><th>Ubicación</th><th>Estado</th>
//           </tr>
//         </thead>
//         <tbody>
//           {items.length === 0 ? (
//             <tr><td colSpan="6" className="muted">Sin datos…</td></tr>
//           ) : (
//             items.map((it, i) => {
//               const id = it.id || it.tx_id || "-";
//               const monto = fmtMoney(it.monto);
//               const fecha = it.fecha || "-";
//               const hora = it.hora || "-";
//               const ubic = it.ubicacion || it.pais || "-";
//               return (
//                 <tr key={i}>
//                   <td>{id}</td>
//                   <td>{monto}</td>
//                   <td>{fecha}</td>
//                   <td>{hora}</td>
//                   <td>{ubic}</td>
//                   <td><EstadoPill bandera={it.bandera} sospechosa={it.sospechosa} /></td>
//                 </tr>
//               );
//             })
//           )}
//         </tbody>
//       </table>
//     </div>
//   );
// }