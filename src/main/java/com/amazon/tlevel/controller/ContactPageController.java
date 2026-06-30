package com.amazon.tlevel.controller;

import com.amazon.tlevel.model.ContactMessage;
import com.amazon.tlevel.service.EmailService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ContactPageController {

    private static final String SUPPORT_EMAIL = "support@amazon-tlevel.co.uk";
    private final EmailService emailService;

    public ContactPageController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/contact/submit")
    public ContactResult submitContactForm(@RequestParam String firstName,
                                           @RequestParam String lastName,
                                           @RequestParam String userEmail,
                                           @RequestParam String messageBody) {

        ContactResult validation = validateInputs(firstName, lastName, userEmail, messageBody);
        if (!validation.isSuccess()) {
            return validation;
        }

        ContactMessage message = new ContactMessage();
        message.setFirstName(firstName.trim());
        message.setLastName(lastName.trim());
        message.setUserEmail(userEmail.trim().toLowerCase());
        message.setMessageBody(messageBody.trim());

        try {
            String subject = "New Contact Form Submission from " + firstName + " " + lastName;
            String emailContent = buildEmailContent(message);

            emailService.sendEmail(
                    SUPPORT_EMAIL,
                    message.getUserEmail(),
                    subject,
                    emailContent
            );

            return new ContactResult(true,
                    "Thank you, " + firstName + "! Your message has been sent. " +
                    "We'll get back to you at " + userEmail + " as soon as possible.");

        } catch (Exception e) {
            System.err.println("Email sending failed: " + e.getMessage());
            return new ContactResult(false,
                    "Sorry, there was a problem sending your message. Please try again later.");
        }
    }

    private ContactResult validateInputs(String firstName, String lastName,
                                          String email, String message) {

        if (firstName == null || firstName.trim().isEmpty()) {
            return new ContactResult(false, "ERROR: First name is required.");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            return new ContactResult(false, "ERROR: Last name is required.");
        }
        if (email == null || email.trim().isEmpty()) {
            return new ContactResult(false, "ERROR: Email address is required.");
        }
        if (message == null || message.trim().isEmpty()) {
            return new ContactResult(false, "ERROR: Message cannot be empty.");
        }

        if (!email.contains("@") || !email.contains(".")) {
            return new ContactResult(false,
                    "ERROR: Invalid email format. Please enter a valid email address.");
        }

        if (message.trim().length() < 10) {
            return new ContactResult(false,
                    "ERROR: Message is too short. Please provide more detail.");
        }
        if (message.trim().length() > 2000) {
            return new ContactResult(false,
                    "ERROR: Message is too long. Please keep it under 2000 characters.");
        }

        return new ContactResult(true, "Validation passed.");
    }

    private String buildEmailContent(ContactMessage msg) {
        return  "New message from the Amazon T-Level Portal contact form:\n\n"
              + "Name:    " + msg.getFirstName() + " " + msg.getLastName() + "\n"
              + "Email:   " + msg.getUserEmail() + "\n"
              + "Message:\n" + msg.getMessageBody() + "\n\n"
              + "---\nPlease reply directly to the user's email address above.";
    }

    public static class ContactResult {
        private final boolean success;
        private final String message;

        public ContactResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage()  { return message; }

        @Override
        public String toString() {
            return "ContactResult{success=" + success + ", message='" + message + "'}";
        }
    }
}
