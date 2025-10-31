# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## React Compiler

The React Compiler is not enabled on this template. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.


Panel mínimo para visualizar transacciones y su estado (OK / SOSPECHOSA).

## Stack
- Frontend: React + Vite
- Mock backend: json-server (db.json)
- Datos de prueba: `public/alerts.json` 

## Cómo correr en local

### Opción 1 — Con mock de json-server
1. En una terminal (raíz del proyecto):  npx json-server --watch db.json --port 8001
Endpoint: `http://127.0.0.1:8000/alerts`

2. En otra terminal: npm install   npm run dev
3. Abrir el front: la URL que imprime Vite (p. ej. `http://localhost:5173`).
4. En `src/App.jsx` ajustar:
const ENDPOINT = "http://127.0.0.1:8000/alerts";