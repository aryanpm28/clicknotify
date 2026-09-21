import { useState, useRef, useEffect } from 'react'
import toast, { Toaster } from 'react-hot-toast'
import { createNotification, startTelegramLink, checkTelegramLinkStatus } from './services/api'

function App() {
  const [form, setForm] = useState({ userName: '', email: '' })
  const [loading, setLoading] = useState(false)

  // Telegram connect flow state
  const [telegramStatus, setTelegramStatus] = useState('idle') // idle | connecting | connected | unavailable
  const [telegramToken, setTelegramToken] = useState(null)
  const pollRef = useRef(null)

  useEffect(() => {
    // Stop polling if the component unmounts mid-connect
    return () => clearInterval(pollRef.current)
  }, [])

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleConnectTelegram = async () => {
    setTelegramStatus('connecting')
    try {
      const { token, deepLink } = await startTelegramLink()
      setTelegramToken(token)

      if (!deepLink) {
        // Bot isn't configured on the backend — nothing to open
        setTelegramStatus('unavailable')
        return
      }

      window.open(deepLink, '_blank', 'noopener,noreferrer')

      // Poll until the user actually taps Start in Telegram, or give up after 2 minutes
      let attempts = 0
      pollRef.current = setInterval(async () => {
        attempts += 1
        try {
          const { linked } = await checkTelegramLinkStatus(token)
          if (linked) {
            clearInterval(pollRef.current)
            setTelegramStatus('connected')
            toast.success('Telegram connected!')
          } else if (attempts >= 40) { // ~2 minutes at 3s intervals
            clearInterval(pollRef.current)
            setTelegramStatus('idle')
            setTelegramToken(null)
            toast.error('Telegram connection timed out — try again')
          }
        } catch {
          // transient poll failure — just try again on the next tick
        }
      }, 3000)
    } catch {
      setTelegramStatus('idle')
      toast.error('Could not start Telegram connection')
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)

    try {
      const result = await createNotification({
        ...form,
        telegramLinkToken: telegramStatus === 'connected' ? telegramToken : null,
        productName: 'General Notification'
      })
      toast.success(result.message || 'Notification sent successfully!')
      setForm({ userName: '', email: '' })
      setTelegramStatus('idle')
      setTelegramToken(null)
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to send notification'
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center p-4">
      <Toaster position="top-right" />

      <div className="bg-white rounded-xl shadow-lg p-8 w-full max-w-md">
        <h1 className="text-2xl font-bold text-center text-gray-800 mb-2">
          ClickNotify
        </h1>
        <p className="text-center text-gray-500 text-sm mb-6">
          Get notified by Email — Telegram optional
        </p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Name
            </label>
            <input
              type="text"
              name="userName"
              value={form.userName}
              onChange={handleChange}
              required
              placeholder="Your name"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Email
            </label>
            <input
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              required
              placeholder="you@example.com"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Telegram <span className="text-gray-400 font-normal">(optional)</span>
            </label>

            {telegramStatus === 'connected' ? (
              <div className="flex items-center gap-2 text-sm text-green-700 bg-green-50 border border-green-200 rounded-lg px-3 py-2">
                ✅ Telegram connected — you'll get notified there too
              </div>
            ) : (
              <button
                type="button"
                onClick={handleConnectTelegram}
                disabled={telegramStatus === 'connecting'}
                className="w-full border border-gray-300 hover:bg-gray-50 disabled:opacity-60 text-gray-700 text-sm font-medium py-2 rounded-lg transition"
              >
                {telegramStatus === 'connecting'
                  ? 'Waiting for you to tap Start in Telegram…'
                  : 'Connect Telegram'}
              </button>
            )}

            {telegramStatus === 'unavailable' && (
              <p className="text-xs text-amber-600 mt-1">
                Telegram isn't configured on this deployment right now — email will still work.
              </p>
            )}
            {telegramStatus === 'connecting' && (
              <p className="text-xs text-gray-400 mt-1">
                A Telegram tab just opened — tap <strong>Start</strong> there.
                Telegram requires this one-time step itself; this app can't skip it.
              </p>
            )}
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white font-medium py-2.5 rounded-lg transition"
          >
            {loading ? 'Sending...' : 'Send Notification'}
          </button>
        </form>
      </div>
    </div>
  )
}

export default App
