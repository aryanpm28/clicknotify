package com.clicknotify.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendNotificationEmail(
            String toEmail,
            String userName,
            String productName
    ) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Product Notification");

        message.setText(
                "Hello " + userName + ",\n\n" +
                "Thank you for showing interest in:\n\n" +
                productName + "\n\n" +
                "Your notification request has been received successfully.\n\n" +
                "Regards,\n" +
                "ClickNotify"
        );

        mailSender.send(message);
    }
}
