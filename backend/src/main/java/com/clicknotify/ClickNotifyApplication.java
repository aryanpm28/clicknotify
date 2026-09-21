package com.clicknotify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling powers TelegramLinkService's polling loop, which is
// what makes the "Connect Telegram" button work without needing a public
// webhook URL (not realistic for a local/small project to expose).
@SpringBootApplication
@EnableScheduling
public class ClickNotifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClickNotifyApplication.class, args);
    }
}
