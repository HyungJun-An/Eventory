// vite.config.js
import process from "node:process";
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
    build: {
      rollupOptions: {
        output: {
          // 모든 화면이 쓰는 React 계열만 앱 코드와 분리 → 앱을 다시 배포해도 브라우저 캐시 재사용.
          // 차트(recharts)·결제 SDK 는 lazy 페이지에서만 import 하므로 Rollup 이 해당 화면용 청크로 자동 분리한다.
          // (차트까지 수동 청크로 묶으면 CommonJS 변환 헬퍼가 차트 청크에 들어가
          //  React 청크가 차트 청크를 import → 메인 화면에서도 차트 97kB(gzip)를 받게 된다)
          manualChunks(id) {
            if (id.includes("commonjsHelpers")) return "react-vendor";
            if (/[\\/]node_modules[\\/](react|react-dom|react-router|react-router-dom|scheduler)[\\/]/.test(id)) return "react-vendor";
            return undefined;
          },
        },
      },
    },
  };
});
