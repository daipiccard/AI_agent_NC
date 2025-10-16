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
- Backend de ingesta: Spring Boot + MySQL
- Datos de prueba: `public/alerts.json`

## Cómo correr en local

### Opción 1 — Backend real (Spring Boot)
1. Levantar MySQL en `localhost:3306` con la base `db_transacciones` y credenciales configuradas en `ingestion-java/src/main/resources/application.properties`.
2. Desde `ingestion-java/`: `./mvnw spring-boot:run`
   * El backend expone el endpoint `http://127.0.0.1:8000/alerts` y acepta peticiones CORS desde `http://localhost:5173` y `http://127.0.0.1:5173` por defecto.
   * Si necesitás exponerlo a otros orígenes, ajusta `app.alerts.allowed-origins` en `application.properties` o mediante variables de entorno.
3. En otra terminal, desde la raíz: `npm install` y luego `npm run dev`.
   * Por defecto el front usará `http://127.0.0.1:8000/alerts`. Para apuntar a otro backend define `VITE_ALERTS_ENDPOINT` antes de ejecutar `npm run dev`.
4. Abrir el front en la URL que imprime Vite (por ejemplo `http://localhost:5173`).

### Opción 2 — Mock con json-server
1. En la raíz: `npx json-server --watch db.json --port 8000`
2. En otra terminal: exporta `VITE_ALERTS_ENDPOINT=http://127.0.0.1:8000/alerts`, luego ejecuta `npm install` (una sola vez) y `npm run dev`.
3. Abrir el front en la URL de Vite.

## Deploy (Vercel u otros)

- Define la variable de entorno `ALERTS_BACKEND_URL` (o `NEXT_PUBLIC_ALERTS_BACKEND_URL`) con la URL pública del backend Spring Boot (por ejemplo `https://mi-backend.example.com/alerts`). La función serverless en `api/alerts.js` la usará para reenviar las consultas del dashboard.
- Opcionalmente, también puedes definir `VITE_ALERTS_ENDPOINT` para apuntar el front directamente al backend durante el build. Si no la indicas, el front consultará `https://<tu-dominio>/api/alerts`, que será atendido por la función anterior.
- Si el dominio del front es distinto al local, agrega ese dominio a `app.alerts.allowed-origins` (por ejemplo `https://mi-dashboard.vercel.app`).
- Si prefieres prescindir del proxy en Vercel, elimina `api/alerts.js` y define `VITE_ALERTS_ENDPOINT` con la URL final antes de desplegar.
- El proxy ya no sirve datos de ejemplo por defecto; si falta la variable de entorno o el backend es inaccesible, responderá con un error 502 para dejar claro que la configuración está incompleta. Solo se puede habilitar la muestra estática exportando `ALERTS_PROXY_FALLBACK=sample` (o valores equivalentes como `true`/`on`).

## Cómo guardar tus cambios en Git
1. Revisa qué archivos modificaste con `git status`.
2. Agrega los archivos relevantes al área de staging, por ejemplo `git add dashboard-web/src/App.jsx`.
3. Crea un commit descriptivo: `git commit -m "Descripción breve del cambio"`.
4. Opcional: sube el commit al repositorio remoto con `git push origin <nombre-de-tu-rama>`.

Con estos pasos tus cambios quedarán guardados localmente y listos para compartir.

### ¿Cómo sé si los cambios ya están aplicados?
- Tras hacer `git commit`, puedes verificar que el commit existe ejecutando `git log --oneline -1` para ver el último mensaje guardado.
- Si ves `nothing to commit, working tree clean` al ejecutar `git status`, significa que todos los cambios locales están registrados.
- Si ya hiciste `git push`, la rama remota también tendrá ese commit y podrás revisarlo en GitHub.

### Crear un Pull Request (PR)
1. Asegúrate de haber hecho `git push origin <nombre-de-tu-rama>` para subir los commits.
2. Ve a la página del repositorio en GitHub. Verás un botón que sugiere crear un PR para la rama recién subida; haz clic en **Compare & pull request**.
3. Completa el título y la descripción del PR (por ejemplo, resumiendo los cambios y las pruebas realizadas).
4. Haz clic en **Create pull request** para publicar la solicitud. Desde allí podrás seguir comentarios y aprobar los cambios.
