import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import AuthProvider from "./auth/AuthProvider";
import App from "./App";

const rootElement =
  document.getElementById("root");

if (!rootElement) {
  throw new Error(
    "Root element was not found in index.html."
  );
}

createRoot(rootElement).render(
  <StrictMode>
    <AuthProvider>
      <App />
    </AuthProvider>
  </StrictMode>
);