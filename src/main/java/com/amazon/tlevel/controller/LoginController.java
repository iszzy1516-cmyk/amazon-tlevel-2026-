package com.amazon.tlevel.controller;

import com.amazon.tlevel.model.User;
import com.amazon.tlevel.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class LoginController {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginResult loginUser(@RequestParam String inputEmail,
                                  @RequestParam String inputPassword,
                                  HttpSession session) {

        if (inputEmail == null || inputEmail.trim().isEmpty()) {
            return new LoginResult(false, "ERROR: Please enter your email address.", null);
        }
        if (inputPassword == null || inputPassword.trim().isEmpty()) {
            return new LoginResult(false, "ERROR: Please enter your password.", null);
        }

        User user = userService.findByEmail(inputEmail.trim().toLowerCase());

        if (user == null) {
            return new LoginResult(false,
                    "ERROR: Invalid email or password.", null);
        }

        if (user.isAccountLocked()) {
            return new LoginResult(false,
                    "ERROR: Your account has been locked after too many failed attempts. " +
                    "Please contact support to unlock it.", null);
        }

        boolean passwordMatches = BCrypt.checkpw(inputPassword, user.getPasswordHash());

        if (passwordMatches) {
            userService.resetFailedAttempts(user.getUserId());

            session.setAttribute("isAuthenticated", true);
            session.setAttribute("currentUserRole", user.getRole());
            session.setAttribute("currentUserID", user.getUserId());
            session.setAttribute("darkMode", user.isDarkMode());
            session.setAttribute("highContrast", user.isHighContrast());
            session.setAttribute("fontSize", user.getFontSize());

            String sessionToken = java.util.UUID.randomUUID().toString();
            session.setAttribute("sessionToken", sessionToken);

            return new LoginResult(true,
                    "Login Successful! Redirecting to dashboard...",
                    user.getRole());

        } else {
            int failedAttempts = userService.incrementFailedAttempts(user.getUserId());

            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                userService.lockAccount(user.getUserId());
                return new LoginResult(false,
                        "ERROR: Invalid email or password. " +
                        "Your account has now been locked after " + MAX_FAILED_ATTEMPTS +
                        " failed attempts. Please contact support.", null);
            }

            int attemptsLeft = MAX_FAILED_ATTEMPTS - failedAttempts;

            return new LoginResult(false,
                    "ERROR: Invalid email or password. " +
                    "You have " + attemptsLeft + " attempt(s) remaining before your account is locked.",
                    null);
        }
    }

    @PostMapping("/logout")
    public void logoutUser(HttpSession session) {
        session.invalidate();
    }

    public static boolean isAuthenticated(HttpSession session) {
        Boolean auth = (Boolean) session.getAttribute("isAuthenticated");
        return Boolean.TRUE.equals(auth);
    }

    public static String getCurrentUserRole(HttpSession session) {
        return (String) session.getAttribute("currentUserRole");
    }

    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final String userRole;

        public LoginResult(boolean success, String message, String userRole) {
            this.success  = success;
            this.message  = message;
            this.userRole = userRole;
        }

        public boolean isSuccess()   { return success; }
        public String getMessage()   { return message; }
        public String getUserRole()  { return userRole; }

        @Override
        public String toString() {
            return "LoginResult{success=" + success + ", role=" + userRole + "}";
        }
    }
}
