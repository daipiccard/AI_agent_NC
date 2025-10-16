export default async function handler(req, res) {
  if (req.method !== "GET") {
    res.setHeader("Allow", "GET");
    res.status(405).json({ error: "Method not allowed" });
    return;
  }

  const upstreamBase = process.env.ALERTS_BACKEND_URL;
  if (!upstreamBase) {
    res
      .status(500)
      .json({ error: "Missing ALERTS_BACKEND_URL environment variable" });
    return;
  }

  const targetUrl = upstreamBase.replace(/\/$/, "");

  try {
    const response = await fetch(targetUrl, {
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
    res.status(502).json({ error: "Upstream request failed", detail: String(err) });
  }
}
