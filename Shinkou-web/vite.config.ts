import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, __dirname, "");
  const explicitApiBaseUrl = env.VITE_API_BASE_URL?.startsWith("http")
    ? env.VITE_API_BASE_URL
    : "";
  const apiProxyTarget =
    env.VITE_API_PROXY_TARGET || explicitApiBaseUrl || "http://127.0.0.1:8080";

  return {
    root: __dirname,
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    build: {
      rollupOptions: {
        input: resolve(__dirname, 'index.html')
      }
    },
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: apiProxyTarget,
          changeOrigin: true,
          secure: false,
        }
      }
    }
  };
});
