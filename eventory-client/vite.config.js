// vite.config.js
import react from "@vitejs/plugin-react";
import { defineConfig, loadEnv } from "vite";

export default defineConfig(({ mode }) => {
  // 개발 서버 프록시 대상: .env 또는 쉘 환경변수 API_PROXY_TARGET으로 변경 가능
  // 기본값은 docker compose로 띄운 백엔드(HTTPS, self-signed 인증서)
  const env = loadEnv(mode, process.cwd(), "");
  const apiTarget = env.API_PROXY_TARGET || "https://localhost:8080";
  return {
    plugins: [
      react(),
    ],

    publicDir: "./static",
    base: "/",

    server: {
      proxy: {
        "/api": {
          target: apiTarget,
          changeOrigin: true,
          secure: false, // self-signed 인증서 허용
        },
      },
    },
    optimizeDeps: {
      include: ["jwt-decode"],
    },
  };
});
