# ClickNotify

Portfolio project that demonstrates **third-party notifications** using:

- **Email** (Spring Mail + Gmail)
- **Telegram Bot** (Bot API via @BotFather) — connected with a one-click deep link, not a manually typed Chat ID

No WhatsApp. No SMS. No paid Twilio number.

## Features

- React form (Name, Email) + a "Connect Telegram" button
- Telegram is linked via a deep link (`https://t.me/<bot>?start=<token>`) —
  the user taps Start in Telegram once, and the backend detects the link
  automatically by polling for it. No chat ID is ever typed or pasted.
- Saves request in MySQL
- Sends HTML email
- Sends Telegram message via bot, once connected
- API key + per-IP rate limiting (on state-changing requests only)
- Global exception handling with domain-specific exception types
- Layered Spring Boot architecture

## Tech Stack

| Layer    | Technology |
|----------|------------|
| Backend  | Java 21, Spring Boot, Spring Data JPA, Spring Mail |
| Frontend | React, Vite, Tailwind CSS, Axios |
| Database | MySQL |
| Telegram | Bot API (free) — deep link + polling, no webhook needed |

## Project Structure

clicknotify/
├── backend/
│ └── src/main/java/com/clicknotify/
│ ├── controller/ (Notification, TelegramLink)
│ ├── service/ (Notification, Email, Telegram, TelegramLink)
│ ├── repository/
│ ├── entity/
│ ├── dto/
│ ├── config/
│ ├── security/ (API key + rate limit interceptor)
│ └── exception/
└── frontend/
└── src/
├── App.jsx
└── services/api.js


## Setup

### 1. MySQL
Create/use local MySQL. App auto-creates DB `clicknotify`.

### 2. Gmail App Password
1. Enable 2-Step Verification
2. https://myaccount.google.com/apppasswords
3. Generate a password — set it as an environment variable (see step 4), never paste it directly into `application.properties`

### 3. Telegram Bot
1. Message **@BotFather** → `/newbot` → get your bot token
2. Note your bot's `@username` too — the app needs it to build the connect link
3. That's it — no manual Chat ID lookup needed. The app's "Connect Telegram" button handles linking a user's chat automatically via a deep link.

### 4. Environment variables

`application.properties` reads secrets from environment variables with safe placeholder defaults — real values are never committed. Set these before running:

```bash
export DB_PASSWORD=your_real_mysql_password
export MAIL_USERNAME=you@gmail.com
export MAIL_APP_PASSWORD=your_gmail_app_password
export TELEGRAM_BOT_TOKEN=your_bot_token
export TELEGRAM_BOT_USERNAME=your_bot_username
export API_KEY=$(openssl rand -hex 24)
```

Frontend: copy `frontend/.env.example` to `.env` and set `VITE_API_KEY` to match `API_KEY` above.

### 5. Run

```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm run dev
```

Open http://localhost:5173

## API

Every `/notifications/**` and `/telegram/**` request requires an
`X-API-Key` header matching `API_KEY`. POST requests are also rate-limited
per IP (10/minute by default).

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /notifications | Send a notification (email, + Telegram if connected) |
| GET | /notifications | List notification logs |
| POST | /telegram/link | Start a Telegram connect attempt — returns `{ token, deepLink }` |
| GET | /telegram/link/{token}/status | Poll whether that link has been completed — `{ linked }` |

### Example: send a notification

```json
{
  "userName": "Aryan",
  "email": "aryan@example.com",
  "telegramLinkToken": "a1b2c3d4e5f6...",
  "productName": "General Notification"
}
```

`telegramLinkToken` is optional and comes from the `/telegram/link`
response after the user completes the connect flow — it's never a raw
chat ID entered by hand.

## Why no phone number?

Telegram identifies chats by a numeric **Chat ID**, not a phone number —
and this app never even asks the user for that ID directly. The
"Connect Telegram" button generates a one-time link; the backend resolves
the real chat ID itself once the user taps Start. Phone number was only
ever relevant for SMS/WhatsApp, both dropped from this project since they
require a paid, rented number.
