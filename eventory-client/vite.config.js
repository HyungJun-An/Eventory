// vite.config.js
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig(({ mode }) => {
  const isProd = mode === "production";
  return {
    plugins: [
      react(),
    ],

    publicDir: "./static",
    base: "/",

    server: {
      proxy: {
        "/api": {
          target: isProd
            ? "https://localhost:8080" // prod일 땐 https
            : "http://localhost:8080", // dev일 땐 http
          changeOrigin: true,
          secure: false,
        },
      },
    },
    optimizeDeps: {
      include: ["jwt-decode"],
    },
  };
});
