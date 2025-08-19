// vite.config.js
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig(async ({ mode }) => {
  const plugins = [react()];

  // if (mode === "development") {
  //   // const { screenGraphPlugin } = await import(
  //   //   "@animaapp/vite-plugin-screen-graph"
  //   // );
  //   // plugins.push(screenGraphPlugin());
  // }

  return {
    plugins,
    extensions: ["js", "jsx"],

    publicDir: "./static",
    base: "/",

    server: {
      proxy: {
        "/api": {
          target: "https://localhost:8080",
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
