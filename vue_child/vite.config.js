import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    host: '0.0.0.0', // ✅ 외부에서도 접속 가능하게
    port: 5173,
    proxy: {
      '/geoserver': {
        target: 'https://api.child119.com',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path
      }
    }
  }
})