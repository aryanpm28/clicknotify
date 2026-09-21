package com.clicknotify.service;

import com.clicknotify.dto.NotificationRequest;
import com.clicknotify.dto.NotificationResponse;
import com.clicknotify.entity.Notification;
import com.clicknotify.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final TelegramService telegramService;
    private final TelegramLinkService telegramLinkService;

    public NotificationService(NotificationRepository notificationRepository,
                               EmailService emailService,
                               TelegramService telegramService,
                               TelegramLinkService telegramLinkService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.telegramService = telegramService;
        this.telegramLinkService = telegramLinkService;
    }

    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        // Resolve the real chat ID from the link token server-side — the
        // frontend/user never sees or handles the raw numeric chat ID at all.
        String resolvedChatId = telegramLinkService.resolveChatId(request.getTelegramLinkToken());

        Notification notification = new Notification();
        notification.setUserName(request.getUserName());
        notification.setEmail(request.getEmail());
        notification.setTelegramChatId(resolvedChatId);
        notification.setProductName(request.getProductName());
        notification.setNotificationStatus("PENDING");

        Notification saved = notificationRepository.save(notification);

        boolean emailSent = false;
        boolean telegramRequested = resolvedChatId != null && !resolvedChatId.isBlank();
        boolean telegramSent = false;

        try {
            emailService.sendNotificationEmail(
                    request.getEmail(),
                    request.getUserName(),
                    request.getProductName()
            );
            emailSent = true;
        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
        }

        try {
            telegramSent = telegramService.sendMessage(
                    resolvedChatId,
                    request.getUserName(),
                    request.getProductName()
            );
        } catch (Exception e) {
            // A chat ID WAS resolved but sending genuinely failed — this is
            // different from telegramRequested being false, where nothing
            // was attempted at all (no link established).
            System.err.println("Telegram failed: " + e.getMessage());
        }

        if (emailSent || telegramSent) {
            saved.setNotificationStatus("SENT");
        } else {
            saved.setNotificationStatus("FAILED");
        }
        notificationRepository.save(saved);

        String message;
        if (emailSent && telegramSent) {
            message = "Notification sent via Email and Telegram!";
        } else if (emailSent && telegramRequested) {
            // Telegram was requested but didn't go through — say so honestly
            // instead of pretending it worked.
            message = "Notification sent via Email (Telegram failed)";
        } else if (emailSent) {
            message = "Notification sent via Email";
        } else if (telegramSent) {
            message = "Notification sent via Telegram (Email failed)";
        } else if (telegramRequested) {
            message = "Failed to send both Email and Telegram";
        } else {
            message = "Failed to send notification";
        }

        return toResponse(saved, message);
    }

    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAll()
                .stream()
                .map(n -> toResponse(n, null))
                .collect(Collectors.toList());
    }

    private NotificationResponse toResponse(Notification n, String message) {
        NotificationResponse response = new NotificationResponse();
        response.setId(n.getId());
        response.setUserName(n.getUserName());
        response.setEmail(n.getEmail());
        response.setTelegramChatId(n.getTelegramChatId());
        response.setProductName(n.getProductName());
        response.setNotificationStatus(n.getNotificationStatus());
        response.setCreatedAt(n.getCreatedAt());
        response.setMessage(message);
        return response;
    }
}
