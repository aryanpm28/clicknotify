console.log(
  '[Vercel Build Check] VITE_API_BASE_URL:',
  process.env.VITE_API_BASE_URL ? 'RECEIVED' : 'MISSING'
)

import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      '/products': 'http://localhost:8080',
      '/notifications': 'http://localhost:8080'
    }
  }
})
