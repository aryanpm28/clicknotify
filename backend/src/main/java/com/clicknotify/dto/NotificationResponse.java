package com.clicknotify.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String userName;
    private String email;
    private String telegramChatId;
    private String productName;
    private String notificationStatus;
    private LocalDateTime createdAt;
    private String message;
}
