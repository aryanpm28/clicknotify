import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
    // Must match the backend's security.api-key (API_KEY env var) or every
    // request gets a 401. Set VITE_API_KEY in your .env — see .env.example.
    'X-API-Key': import.meta.env.VITE_API_KEY || 'dev-only-placeholder-key'
  }
})

export const createNotification = async (data) => {
  const response = await api.post('/notifications', data)
  return response.data
}

export const startTelegramLink = async () => {
  const response = await api.post('/telegram/link')
  return response.data // { token, deepLink }
}

export const checkTelegramLinkStatus = async (token) => {
  const response = await api.get(`/telegram/link/${token}/status`)
  return response.data // { linked }
}

export default api