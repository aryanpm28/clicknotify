package com.clicknotify.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${resend.from-email:onboarding@resend.dev}")
    private String fromEmail;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public EmailService(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    public void sendNotificationEmail(
            String toEmail,
            String userName,
            String productName
    ) {
        try {
            String htmlContent = buildEmailBody(userName, productName);

            Map<String, Object> emailData = Map.of(
                    "from", fromEmail,
                    "to", new String[]{toEmail},
                    "subject", "Product Notification",
                    "html", htmlContent
            );

            String jsonBody = objectMapper.writeValueAsString(emailData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException(
                        "Resend email failed. HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to send email through Resend: " + e.getMessage(),
                    e
            );
        }
    }

    private String buildEmailBody(String userName, String productName) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #2563eb;">ClickNotify</h2>

                        <p>Hello <strong>%s</strong>,</p>

                        <p>Thank you for showing interest in:</p>

                        <p style="font-size: 18px; font-weight: bold; color: #1e40af;">
                            %s
                        </p>

                        <p>Your notification request has been received successfully.</p>

                        <br>

                        <p>
                            Regards,<br>
                            <strong>ClickNotify</strong>
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(userName, productName);
    }
}

