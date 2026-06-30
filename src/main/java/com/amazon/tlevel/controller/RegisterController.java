package com.amazon.tlevel.controller;

import com.amazon.tlevel.model.User;
import com.amazon.tlevel.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public RegisterResult registerUser(@RequestParam String userFirstName,
                                       @RequestParam String userLastName,
                                       @RequestParam String userEmail,
                                       @RequestParam String userPassword,
                                       @RequestParam String userType,
                                       @RequestParam(required = false) String userLocation,
                                       @RequestParam boolean gdprConsent) {

        if (userEmail == null || !userEmail.contains("@") || !userEmail.contains(".")) {
            return new RegisterResult(false,
                    "ERROR: Invalid email format. Registration failed.");
        }

        if (userFirstName == null || userFirstName.trim().isEmpty()) {
            return new RegisterResult(false, "ERROR: First name is required.");
        }
        if (userLastName == null || userLastName.trim().isEmpty()) {
            return new RegisterResult(false, "ERROR: Last name is required.");
        }
        if (userPassword == null || userPassword.trim().isEmpty()) {
            return new RegisterResult(false, "ERROR: Password is required.");
        }
        if (userType == null || userType.trim().isEmpty()) {
            return new RegisterResult(false, "ERROR: Please select a user type (Student, School, or Parent/Guardian).");
        }

        if (!gdprConsent) {
            return new RegisterResult(false,
                    "ERROR: You must agree to the data processing terms to register (UK GDPR requirement).");
        }

        if (userService.emailExists(userEmail.trim().toLowerCase())) {
            return new RegisterResult(false,
                    "ERROR: This email is already registered. Please login instead.");
        }

        if (userPassword.length() < 8) {
            return new RegisterResult(false,
                    "ERROR: Password must be at least 8 characters long.");
        }

        if (!userPassword.matches(".*[A-Z].*")) {
            return new RegisterResult(false,
                    "ERROR: Password must contain at least one uppercase letter.");
        }
        if (!userPassword.matches(".*[0-9].*")) {
            return new RegisterResult(false,
                    "ERROR: Password must contain at least one number.");
        }

        String hashedPassword = BCrypt.hashpw(userPassword, BCrypt.gensalt(12));

        String newUserID = UUID.randomUUID().toString();

        User newUser = new User();
        newUser.setUserId(newUserID);
        newUser.setFirstName(userFirstName.trim());
        newUser.setLastName(userLastName.trim());
        newUser.setEmail(userEmail.trim().toLowerCase());
        newUser.setPasswordHash(hashedPassword);
        newUser.setRole(userType.trim());
        newUser.setLocation(userLocation != null ? userLocation.trim() : "");
        newUser.setGdprConsent(gdprConsent);
        newUser.setEmailVerified(false);
        newUser.setAccountLocked(false);

        userService.saveUser(newUser);

        return new RegisterResult(true,
                "Registration Successful! Welcome to the Amazon T Level platform, "
                + userFirstName + ".");
    }

    public static class RegisterResult {
        private final boolean success;
        private final String message;

        public RegisterResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage()  { return message; }

        @Override
        public String toString() {
            return "RegisterResult{success=" + success + ", message='" + message + "'}";
        }
    }
}
