# ClickNotify

Portfolio project that demonstrates **third-party notification integration** using:

- **Email** through the Brevo API
- **Telegram Bot API** with one-click deep-link connection
- **MySQL** for notification records
- **Spring Boot REST API** with API-key authentication and rate limiting

Users can submit a notification request, connect their Telegram account through a one-time deep link, and receive notifications through email and Telegram.

## Live Demo

https://clicknotify.vercel.app

## Features

- React form for Name and Email
- One-click **Connect Telegram** flow
- Telegram deep-link connection using:
  `https://t.me/<bot>?start=<token>`
- Automatic Telegram chat linking without manually entering a Chat ID
- Sends email notifications through Brevo
- Sends Telegram notifications when Telegram is connected
- Saves notification records in MySQL
- API-key authentication
- Per-IP rate limiting for state-changing requests
- Global exception handling
- Domain-specific exception types
- Layered Spring Boot architecture
- Responsive React interface

## How it works

1. **Notification request** — the user enters their name and email
2. **Telegram connection** — the user can optionally connect Telegram using a one-time deep link
3. **Backend processing** — the Spring Boot API validates the request and stores it in MySQL
4. **Email notification** — the backend sends an email through Brevo
5. **Telegram notification** — if Telegram is connected, the backend sends a Telegram message through the Bot API
6. **Status update** — the notification record is updated with the processing result

Telegram linking works through a one-time token and polling flow, so the user never needs to manually enter a numeric Telegram Chat ID.

## Tech Stack

| Layer    | Technology |
| -------- | ---------- |
| Backend  | Java 21, Spring Boot 3.3.4, Spring Data JPA |
| Frontend | React, Vite, Tailwind CSS, Axios |
| Database | MySQL |
| Email    | Brevo API |
| Telegram | Telegram Bot API |
| Hosting  | Vercel + Render |

## Architecture

    User
     ↓
    React + Vite
     ↓
    Axios + X-API-Key
     ↓
    Spring Boot REST API
     ├── MySQL
     ├── Brevo Email API
     └── Telegram Bot API
     ↓
    Notification status
     ↓
    React UI

Production deployment:

    Vercel
      ↓
    React + Vite frontend
      ↓
    Render
      ↓
    Spring Boot backend
     ├── MySQL
     ├── Brevo
     └── Telegram

## Project Structure

    clicknotify/
    ├── backend/
    │   ├── src/
    │   │   ├── main/
    │   │   │   ├── java/
    │   │   │   └── resources/
    │   │   │       └── application.properties
    │   │   └── ...
    │   ├── Dockerfile
    │   └── pom.xml
    │
    ├── frontend/
    │   ├── src/
    │   │   ├── App.jsx
    │   │   ├── services/
    │   │   │   └── api.js
    │   │   └── ...
    │   ├── public/
    │   ├── package.json
    │   └── vite.config.js
    │
    └── docs/

## Setup

### Backend

    cd backend
    mvn spring-boot:run

The backend runs locally using the configured Spring Boot port.

### Frontend

    cd frontend
    npm install
    npm run dev

The frontend runs locally on:

http://localhost:5173

## Environment Variables

### Backend

| Variable                | Purpose                                   |
| ----------------------- | ----------------------------------------- |
| `DB_URL`                | MySQL database connection URL             |
| `DB_USERNAME`           | MySQL username                            |
| `DB_PASSWORD`           | MySQL password                            |
| `BREVO_API_KEY`         | Brevo API authentication                  |
| `BREVO_FROM_EMAIL`      | Email sender address                      |
| `TELEGRAM_BOT_TOKEN`    | Telegram Bot API token                    |
| `TELEGRAM_BOT_USERNAME` | Telegram bot username used for deep links |
| `API_KEY`               | API authentication key                    |
| `CORS_ALLOWED_ORIGINS`  | Allowed frontend origins                  |

### Frontend

| Variable            | Purpose                                     |
| ------------------- | ------------------------------------------- |
| `VITE_API_BASE_URL` | Backend API base URL                        |
| `VITE_API_KEY`      | API key sent through the `X-API-Key` header |

> `VITE_API_KEY` is exposed to the browser because Vite embeds `VITE_*` variables into the frontend build. It should therefore not be treated as a fully private secret.

## Telegram Connection

ClickNotify uses a deep-link flow instead of asking users to manually enter a Telegram Chat ID.

    User
     ↓
    Connect Telegram
     ↓
    Backend creates one-time token
     ↓
    Telegram deep link
     ↓
    User taps Start
     ↓
    Backend detects the Telegram chat
     ↓
    Chat becomes linked
     ↓
    Notifications can be sent

Example deep-link format:

https://t.me/<bot-username>?start=<token>

The numeric Telegram Chat ID is resolved by the backend and is never manually entered by the user.

## API

Every notification and Telegram request requires the API key:

    X-API-Key: <API_KEY>

| Method | Endpoint                        | Purpose                          | Authentication |
| ------ | ------------------------------- | -------------------------------- | -------------- |
| POST   | `/notifications`                | Create and send notification     | X-API-Key      |
| GET    | `/notifications`                | List notification records        | X-API-Key      |
| POST   | `/telegram/link`                | Start Telegram connection        | X-API-Key      |
| GET    | `/telegram/link/{token}/status` | Check Telegram connection status | X-API-Key      |

POST requests are also protected by per-IP rate limiting.

## Example Notification Request

    {
      "userName": "Aryan",
      "email": "aryan@example.com",
      "productName": "General Notification",
      "telegramLinkToken": "a1b2c3d4e5f6..."
    }

`telegramLinkToken` is optional and comes from:

    POST /telegram/link

The token represents the Telegram connection session and is not a manually entered Chat ID.

## Email Notifications

ClickNotify uses the **Brevo API** to send email notifications.

The backend sends the notification through Brevo's email API using:

    BREVO_API_KEY
    BREVO_FROM_EMAIL

No Gmail App Password is required for the current implementation.

## Database

MySQL is used to persist notification records.

The backend uses:

- Spring Data JPA
- JPA entities
- Repositories
- Service layer

Notification records contain information required to track notification requests and their processing status.

## Rate Limiting

State-changing API requests are protected by per-IP rate limiting.

This helps reduce:

- API abuse
- repeated notification requests
- unnecessary email/Telegram sends

The current rate limiter is in-memory and therefore resets when the backend instance restarts.

## Security

The application includes:

- API-key authentication
- Per-IP rate limiting
- Environment-based secrets
- Explicit CORS origins
- Spring service/repository separation
- Global exception handling
- No Brevo or Telegram secrets in frontend source code

> The browser currently sends `X-API-Key`, which means the frontend API key is visible to users. This is acceptable for the current portfolio architecture but should not be considered a fully private secret.

## Error Handling

The backend uses global exception handling with domain-specific exception types.

The API handles invalid requests and service failures through centralized error responses instead of placing all error handling directly inside controllers.

## Deployment

    GitHub
       ↓
    Vercel
       ↓
    React + Vite frontend
       ↓
    Render
       ↓
    Spring Boot backend
     ├── MySQL
     ├── Brevo
     └── Telegram Bot API

### Production Frontend

https://clicknotify.vercel.app

### Production Backend

https://clicknotify-backend.onrender.com

The frontend uses:

    VITE_API_BASE_URL

to communicate with the production backend.

For production, use the permanent Vercel domain rather than temporary Vercel preview URLs.

## Deployment Environment

### Vercel

Set:

    VITE_API_BASE_URL
    VITE_API_KEY

Then redeploy after changing either variable because Vite environment variables are included at build time.

### Render

Set:

    DB_URL
    DB_USERNAME
    DB_PASSWORD
    BREVO_API_KEY
    BREVO_FROM_EMAIL
    TELEGRAM_BOT_TOKEN
    TELEGRAM_BOT_USERNAME
    API_KEY
    CORS_ALLOWED_ORIGINS

For production CORS, use the permanent frontend origin:

https://clicknotify.vercel.app

## Important Limitation

Telegram link sessions are currently stored in server memory.

A Render restart or redeployment can clear pending Telegram link sessions, so a user may need to reconnect Telegram.

The current implementation is suitable for a small portfolio application.

## Portfolio Highlights

This project demonstrates:

- React frontend development
- Vite
- Tailwind CSS
- Axios
- Java 21
- Spring Boot
- Spring Data JPA
- REST API development
- MySQL
- Brevo API integration
- Telegram Bot API integration
- Deep-link authentication flow
- API-key authentication
- Rate limiting
- Global exception handling
- Layered backend architecture
- Vercel deployment
- Render deployment

## Production Status

The project is deployed with:

- React frontend on Vercel
- Spring Boot backend on Render
- MySQL persistence
- Brevo email notifications
- Telegram notifications
- Telegram deep-link connection
- API-key authentication
- Rate limiting

## Limitations

- The frontend API key is visible in the browser bundle
- Telegram pending-link state is stored in memory
- In-memory rate limiting is instance-specific
- Production preview URLs may require separate CORS configuration
- Notification delivery depends on external Brevo and Telegram services

## Author

**Aryan Patil**

GitHub:

https://github.com/aryanpm28

Live Demo:

https://clicknotify.vercel.app
