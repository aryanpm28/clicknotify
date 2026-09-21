package com.clicknotify.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramService {

    @Value("${telegram.bot.token:}")
    private String botToken;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Returns true if a message was actually sent, false if Telegram was
     * intentionally skipped (no chat ID provided, or bot not configured).
     * Throws only for a genuine failure — a chat ID was given but sending
     * it actually failed — so the caller can tell "not requested" apart
     * from "requested but failed" instead of treating both the same way.
     */
    public boolean sendMessage(String chatId, String userName, String productName) {
        if (botToken == null || botToken.isBlank() || botToken.startsWith("your_")) {
            System.out.println("Telegram skipped: bot token not configured");
            return false;
        }

        if (chatId == null || chatId.isBlank()) {
            // Not an error — this user simply didn't provide a Telegram
            // chat ID, which is allowed now that the field is optional.
            return false;
        }

        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        String text = "🔔 ClickNotify\n\n"
                + "Hello " + userName + "!\n\n"
                + "Thank you for showing interest in:\n"
                + productName + "\n\n"
                + "Your notification request has been received successfully.\n\n"
                + "— ClickNotify";

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId.trim());
        body.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Telegram API error: " + response.getStatusCode() + " - " + response.getBody());
        }

        return true;
    }
}
