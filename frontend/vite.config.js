import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 80,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://gateway:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api/v3')
      }
    }
  }
})
