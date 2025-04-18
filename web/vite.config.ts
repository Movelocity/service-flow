import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, './src')
    }
  },
  base: '/',
  build: {
    // 输出到Spring Boot的static目录
    outDir: resolve(__dirname, '../src/main/resources/static'),
    // 将资源放在assets子目录
    assetsDir: 'assets',
    // 生成sourcemap以便调试
    sourcemap: true,
    // 清空输出目录前排除已有文件
    emptyOutDir: false,
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
