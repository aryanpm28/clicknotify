package com.clicknotify.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class EmailService {

    private final RestClient restClient;
    private final String apiKey;
    private final String fromEmail;

    public EmailService(
            @Value("${brevo.api-key}") String apiKey,
            @Value("${brevo.from-email}") String fromEmail
    ) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("api-key", apiKey)
                .build();
    }

    public void sendNotificationEmail(
            String toEmail,
            String userName,
            String productName
    ) {
        String text =
                "Hello " + userName + ",\n\n" +
                "Thank you for showing interest in:\n\n" +
                productName + "\n\n" +
                "Your notification request has been received successfully.\n\n" +
                "Regards,\n" +
                "ClickNotify";

        Map<String, Object> body = Map.of(
                "sender", Map.of(
                        "name", "ClickNotify",
                        "email", fromEmail
                ),
                "to", new Object[]{
                        Map.of("email", toEmail)
                },
                "subject", "Product Notification",
                "textContent", text
        );

        restClient.post()
                .uri("/smtp/email")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
