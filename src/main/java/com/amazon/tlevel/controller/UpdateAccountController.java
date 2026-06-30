package com.amazon.tlevel.controller;

import com.amazon.tlevel.model.User;
import com.amazon.tlevel.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UpdateAccountController {

    private final UserService userService;

    public UpdateAccountController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/account/update")
    public UpdateResult updateAccount(HttpSession session,
                                       @RequestParam(required = false) String newEmail,
                                       @RequestParam(required = false) String newLocation,
                                       @RequestParam(required = false) Boolean darkMode,
                                       @RequestParam(required = false) Boolean highContrast,
                                       @RequestParam(required = false) String fontSize) {

        if (!LoginController.isAuthenticated(session)) {
            return new UpdateResult(false,
                    "ERROR: You must be logged in to update your account.");
        }

        String currentUserID = (String) session.getAttribute("currentUserID");
        if (currentUserID == null) {
            return new UpdateResult(false,
                    "ERROR: Session expired. Please log in again.");
        }

        User user = userService.findById(currentUserID);
        if (user == null) {
            return new UpdateResult(false,
                    "ERROR: User account not found.");
        }

        if (newEmail != null && !newEmail.trim().isEmpty()) {

            if (!newEmail.contains("@") || !newEmail.contains(".")) {
                return new UpdateResult(false,
                        "ERROR: Invalid email format. Please enter a valid email address.");
            }

            if (userService.emailExists(newEmail.trim().toLowerCase()) &&
                !newEmail.trim().toLowerCase().equals(user.getEmail())) {
                return new UpdateResult(false,
                        "ERROR: This email address is already in use by another account.");
            }

            user.setEmail(newEmail.trim().toLowerCase());
        }

        if (newLocation != null && !newLocation.trim().isEmpty()) {
            user.setLocation(newLocation.trim());
        }

        if (darkMode != null) {
            user.setDarkMode(darkMode);
            session.setAttribute("darkMode", darkMode);
        }

        if (highContrast != null) {
            user.setHighContrast(highContrast);
            session.setAttribute("highContrast", highContrast);
        }

        if (fontSize != null && !fontSize.trim().isEmpty()) {
            if (!fontSize.equals("small") && !fontSize.equals("medium") &&
                !fontSize.equals("large") && !fontSize.equals("x-large")) {
                return new UpdateResult(false,
                        "ERROR: Invalid font size. Choose small, medium, large, or x-large.");
            }
            user.setFontSize(fontSize);
            session.setAttribute("fontSize", fontSize);
        }

        userService.updateUser(user);

        return new UpdateResult(true, "Account updated successfully.");
    }

    @PostMapping("/account/delete")
    public UpdateResult deleteAccount(HttpSession session) {

        if (!LoginController.isAuthenticated(session)) {
            return new UpdateResult(false,
                    "ERROR: You must be logged in to delete your account.");
        }

        String currentUserID = (String) session.getAttribute("currentUserID");

        userService.softDeleteUser(currentUserID);

        session.invalidate();

        return new UpdateResult(true,
                "Your account has been successfully deleted. " +
                "Your data will be removed in line with our UK GDPR policy.");
    }

    public static class UpdateResult {
        private final boolean success;
        private final String message;

        public UpdateResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage()  { return message; }

        @Override
        public String toString() {
            return "UpdateResult{success=" + success + ", message='" + message + "'}";
        }
    }
}
