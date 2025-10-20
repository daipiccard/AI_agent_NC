import "./myStyles/globals.css";
import React from "react";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css"; // index.css está dentro de src
import App from "./App.jsx"; // App.jsx dentro de src

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <App />
  </StrictMode>
);
