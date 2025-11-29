package com.devbuild.notificationservice.controller;

import com.devbuild.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;

    /**
     * Endpoint to test email configuration manually.
     * Usage: POST http://localhost:8084/api/notifications/test
     */
    @PostMapping("/test")
    public ResponseEntity<String> sendTestEmail(@RequestParam String to) {
        emailService.sendEmail(
                to,
                "Test Notification System",
                "<h1>It Works!</h1><p>Your Notification Service is correctly configured to send emails.</p>"
        );
        return ResponseEntity.ok("Email sent to " + to);
    }
}