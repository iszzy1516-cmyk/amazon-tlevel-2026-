package com.amazon.tlevel.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    public void sendEmail(String to, String replyTo, String subject, String content) {
        System.out.println("=== EMAIL ===");
        System.out.println("To: " + to + " | Reply-To: " + replyTo);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + content.substring(0, Math.min(content.length(), 100)) + "...");
    }
}
