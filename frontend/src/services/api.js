import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
    // Must match the backend's security.api-key (API_KEY env var).
    // Production API URL is supplied by Vercel environment variables.
    'X-API-Key': import.meta.env.VITE_API_KEY
  }
})

export const createNotification = async (data) => {
  const response = await api.post('/notifications', data)
  return response.data
}

export const startTelegramLink = async () => {
  const response = await api.post('/telegram/link')
  return response.data
}

export const checkTelegramLinkStatus = async (token) => {
  const response = await api.get(`/telegram/link/${token}/status`)
  return response.data
}

export default api
