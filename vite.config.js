import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  },
  root: resolve(__dirname, 'src/main/webapp'),
  build: {
    outDir: resolve(__dirname, 'src/main/webapp/dist'),  // 确保输出到正确的dist目录
    emptyOutDir: true
  }
})