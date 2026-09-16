import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// GitHub Pages serves this as a project site at /personal-website/, so only the
// production build needs the subpath base; local dev keeps serving at "/".
export default defineConfig(({ command }) => ({
  plugins: [react()],
  base: command === "build" ? "/personal-website/" : "/",
}));
