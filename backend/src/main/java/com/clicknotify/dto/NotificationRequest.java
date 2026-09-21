package com.clicknotify.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotBlank(message = "Name is required")
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    // Optional — set automatically once the user completes the "Connect
    // Telegram" flow (see TelegramLinkController). Not a raw chat ID typed
    // by the user; the backend resolves the real chat ID from this token
    // itself, so the numeric ID never has to be looked up or copy-pasted.
    private String telegramLinkToken;

    @NotBlank(message = "Product name is required")
    private String productName;
}
