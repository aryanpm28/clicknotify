package com.clicknotify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements a "Connect Telegram" flow using a deep link
 * (https://t.me/<bot>?start=<token>) instead of asking the user to look up
 * and paste their own numeric chat ID. The user still has to tap Start in
 * Telegram themselves — that's the real, unremovable consent step the
 * platform requires — but everything after that is automatic.
 *
 * No webhook/public URL needed: this polls Telegram's getUpdates endpoint
 * on a short interval instead, which is the right tradeoff for a small
 * project that isn't (yet) deployed behind a public HTTPS address.
 */
@Service
public class TelegramLinkService {

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.bot.username:}")
    private String botUsername;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // token -> session. In-memory and short-lived by design — this is just
    // a handshake, not something that needs to survive a restart.
    private final Map<String, LinkSession> sessions = new ConcurrentHashMap<>();
    private volatile long lastUpdateId = 0;

    private static final long SESSION_TTL_MINUTES = 10;

    private record LinkSession(Instant createdAt, String chatId) {}

    /** Starts a new link attempt and returns the deep link the frontend should open. */
    public LinkStartResult startLink() {
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        sessions.put(token, new LinkSession(Instant.now(), null));
        String deepLink = (botUsername == null || botUsername.isBlank() || botUsername.startsWith("your_"))
                ? null
                : "https://t.me/" + botUsername + "?start=" + token;
        return new LinkStartResult(token, deepLink);
    }

    public boolean isLinked(String token) {
        LinkSession session = sessions.get(token);
        return session != null && session.chatId() != null;
    }

    /** Resolves a token to the chat ID it linked to, used server-side when actually sending. */
    public String resolveChatId(String token) {
        if (token == null || token.isBlank()) return null;
        LinkSession session = sessions.get(token);
        return session != null ? session.chatId() : null;
    }

    /**
     * Polls Telegram for new messages every few seconds looking for
     * "/start <token>" commands, and marks the matching session linked.
     * A short interval keeps the "connect" experience feeling close to
     * instant without needing a public webhook endpoint.
     */
    @Scheduled(fixedDelay = 3000)
    public void pollForLinks() {
        if (botToken == null || botToken.isBlank() || botToken.startsWith("your_")) {
            return; // not configured — nothing to poll
        }
        if (sessions.isEmpty()) {
            return; // nothing waiting to be linked, skip the API call
        }

        try {
            String url = "https://api.telegram.org/bot" + botToken
                    + "/getUpdates?offset=" + (lastUpdateId + 1) + "&timeout=0";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (!root.path("ok").asBoolean(false)) return;

            for (JsonNode update : root.path("result")) {
                lastUpdateId = Math.max(lastUpdateId, update.path("update_id").asLong());

                JsonNode message = update.path("message");
                String text = message.path("text").asText("");
                if (!text.startsWith("/start ")) continue;

                String token = text.substring("/start ".length()).trim();
                LinkSession session = sessions.get(token);
                if (session == null) continue; // unknown/expired token — ignore

                String chatId = message.path("chat").path("id").asText(null);
                if (chatId == null) continue;

                sessions.put(token, new LinkSession(session.createdAt(), chatId));
                sendConfirmation(chatId);
            }
        } catch (Exception e) {
            // A transient failure here shouldn't crash the app — just try
            // again on the next scheduled tick.
            System.err.println("Telegram polling error: " + e.getMessage());
        }

        evictExpiredSessions();
    }

    private void sendConfirmation(String chatId) {
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            Map<String, Object> body = Map.of(
                    "chat_id", chatId,
                    "text", "✅ Connected! You'll get your ClickNotify notifications here."
            );
            restTemplate.postForObject(url, body, String.class);
        } catch (Exception e) {
            // Not fatal — the link itself already succeeded, this is just a courtesy message.
            System.err.println("Failed to send Telegram confirmation: " + e.getMessage());
        }
    }

    private void evictExpiredSessions() {
        Instant cutoff = Instant.now().minusSeconds(SESSION_TTL_MINUTES * 60);
        sessions.entrySet().removeIf(e -> e.getValue().createdAt().isBefore(cutoff) && e.getValue().chatId() == null);
    }

    public record LinkStartResult(String token, String deepLink) {}
}
