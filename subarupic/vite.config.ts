
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

// https://vitejs.dev/config/
const config = {
  base: './',
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  test: {
    pool: 'vmThreads',
    poolOptions: {
      vmThreads: {
        singleThread: true,
        maxThreads: 1,
      },
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8123',
        changeOrigin: true,
        secure: false,
        ws: true,
      },
      '/ws': {
        target: 'http://127.0.0.1:8123',
        changeOrigin: true,
        secure: false,
        ws: true,
      },
    },
  },
} as any;

export default defineConfig(config);
