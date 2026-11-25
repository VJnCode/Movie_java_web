import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  return {
    plugins: [react()],
    server: {
      port: 5173,
      strictPort: true,
      // In development the SPA calls same-origin /api, proxied to the Spring Boot server (no CORS needed).
      proxy: {
        '/api': { target: env.DEV_API_PROXY || 'http://localhost:8000', changeOrigin: true },
      },
    },
    preview: { port: 4173, strictPort: true },
    build: {
      sourcemap: false,
      target: 'es2022',
      chunkSizeWarningLimit: 600,
      rollupOptions: {
        output: {
          manualChunks(id) {
            if (id.includes('node_modules')) {
              if (/react-bootstrap|@restart|@popperjs|dom-helpers|uncontrollable/.test(id)) return 'ui'
              return 'vendor'
            }
          },
        },
      },
    },
  }
})
