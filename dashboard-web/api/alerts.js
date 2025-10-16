const FALLBACK_ALERTS = [
  {
    id: "MVP-0001",
    monto: 250000,
    fecha: "2024-05-10",
    hora: "11:32:18",
    ubicacion: "Buenos Aires",
    bandera: "REVIEW",
  },
  {
    id: "MVP-0002",
    monto: 98000,
    fecha: "2024-05-09",
    hora: "19:41:03",
    ubicacion: "Córdoba",
    bandera: "OK",
  },
  {
    id: "MVP-0003",
    monto: 410000,
    fecha: "2024-05-09",
    hora: "08:07:55",
    ubicacion: "Mendoza",
    bandera: "SOSPECHOSA",
  },
];

function inferUpstreamUrl(req) {
  const configured =
    process.env.ALERTS_BACKEND_URL ||
    process.env.NEXT_PUBLIC_ALERTS_BACKEND_URL ||
    process.env.VITE_ALERTS_ENDPOINT ||
    "";

  if (!configured.trim()) {
    return null;
  }

  const proto = req.headers["x-forwarded-proto"] || "https";
  const host = req.headers.host;
  try {
    const parsed = new URL(configured);
    const selfOrigin = host ? `${proto}://${host}` : null;
    if (selfOrigin && parsed.origin === selfOrigin && parsed.pathname.startsWith("/api/alerts")) {
      return { loop: true, url: parsed.toString() };
    }
    return { url: parsed.toString() };
  } catch (err) {
    if (!host) {
      return { error: `Invalid ALERTS_BACKEND_URL value: ${configured}` };
    }
    try {
      const base = `${proto}://${host}`;
      const resolved = new URL(configured, base);
      if (resolved.origin === base && resolved.pathname.startsWith("/api/alerts")) {
        return { loop: true, url: resolved.toString() };
      }
      return { url: resolved.toString() };
    } catch (nestedErr) {
      return { error: `Invalid ALERTS_BACKEND_URL value: ${configured}` };
    }
  }
}

export default async function handler(req, res) {
  if (req.method !== "GET") {
    res.setHeader("Allow", "GET");
    res.status(405).json({ error: "Method not allowed" });
    return;
  }

  const upstream = inferUpstreamUrl(req);

  if (!upstream) {
    res
      .status(200)
      .json({ alerts: FALLBACK_ALERTS, source: "fallback", error: "Backend no configurado" });
    return;
  }

  if (upstream.error) {
    res
      .status(200)
      .json({ alerts: FALLBACK_ALERTS, source: "fallback", error: upstream.error });
    return;
  }

  if (upstream.loop) {
    res.status(200).json({ alerts: FALLBACK_ALERTS, source: "fallback", error: "Proxy configurado hacia sí mismo" });
    return;
  }

  try {
    const response = await fetch(upstream.url.replace(/\/$/, ""), {
      headers: { Accept: "application/json" },
    });

    const bodyText = await response.text();
    const contentType = response.headers.get("content-type") || "";

    res.status(response.status);
    if (contentType.includes("application/json")) {
      res.setHeader("Content-Type", "application/json");
      res.send(bodyText);
    } else {
      res.setHeader("Content-Type", contentType || "text/plain; charset=utf-8");
      res.send(bodyText);
    }
  } catch (err) {
    res
      .status(200)
      .json({ alerts: FALLBACK_ALERTS, source: "fallback", error: `Fallo al consultar backend: ${String(err)}` });
  }
}
