import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 你的后端基准路径
        changeOrigin: true,
        // 这里不需要重写路径，因为后端接口本身就是以 /api 开头
      }
    }
  }
})