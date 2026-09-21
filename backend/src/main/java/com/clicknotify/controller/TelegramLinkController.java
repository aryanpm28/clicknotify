package com.clicknotify.controller;

import com.clicknotify.service.TelegramLinkService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/telegram")
public class TelegramLinkController {

    private final TelegramLinkService telegramLinkService;

    public TelegramLinkController(TelegramLinkService telegramLinkService) {
        this.telegramLinkService = telegramLinkService;
    }

    // Called when the user clicks "Connect Telegram" — returns a token and
    // the deep link to open. The token (not a chat ID) is what the frontend
    // holds onto and eventually sends with the notification request.
    @PostMapping("/link")
    public Map<String, String> startLink() {
        TelegramLinkService.LinkStartResult result = telegramLinkService.startLink();
        return Map.of(
                "token", result.token(),
                "deepLink", result.deepLink() != null ? result.deepLink() : ""
        );
    }

    // Polled by the frontend every couple of seconds after the deep link is
    // opened, until it reports linked = true.
    @GetMapping("/link/{token}/status")
    public Map<String, Boolean> checkStatus(@PathVariable String token) {
        return Map.of("linked", telegramLinkService.isLinked(token));
    }
}
