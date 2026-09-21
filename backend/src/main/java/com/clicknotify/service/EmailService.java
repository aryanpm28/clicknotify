package com.clicknotify.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendNotificationEmail(String toEmail, String userName, String productName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Product Notification");

            String htmlContent = buildEmailBody(userName, productName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
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
                        <p style="font-size: 18px; font-weight: bold; color: #1e40af;">%s</p>
                        <p>Your notification request has been received successfully.</p>
                        <br>
                        <p>Regards,<br><strong>ClickNotify</strong></p>
                    </div>
                </body>
                </html>
                """.formatted(userName, productName);
    }
}
