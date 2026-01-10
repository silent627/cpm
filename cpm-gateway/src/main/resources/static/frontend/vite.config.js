import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  // 构建输出目录：Gateway 的静态资源目录
  build: {
    outDir: path.resolve(__dirname, '..'),
    emptyOutDir: false, // 不清空目录，保留其他静态资源
    assetsDir: 'assets',
    rollupOptions: {
      input: {
        main: path.resolve(__dirname, 'index.html')
      }
    }
  },
  // 开发服务器配置
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path // 保持路径不变
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  },
  // 基础路径（生产环境）
  base: '/'
})

