package com.amazon.tlevel.security;

import com.amazon.tlevel.model.User;
import com.amazon.tlevel.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * SessionSecurityManager.java
 * Amazon T-Level Portal – Session & Security Backend
 * Developer: Princess Otobo
 *
 * Handles all security and session management for the portal.
 *
 * Covers three areas from the data dictionary and design doc:
 *
 *  1. SESSION TOKEN MANAGEMENT
 *     - Generates a unique token when user logs in
 *     - Validates the token on each protected request
 *     - Clears the token on logout
 *
 *  2. ACCOUNT LOCKING
 *     - Tracks failed login attempts per user
 *     - Locks account after 5 failed attempts (data dictionary requirement)
 *     - Provides unlock method for admin/support use
 *
 *  3. GDPR CONSENT TRACKING
 *     - Records when consent was given
 *     - Checks consent is still valid before processing personal data
 *     - Handles consent withdrawal (right to erasure — UK GDPR Article 17)
 */
public class SessionSecurityManager {

    // Maximum failed login attempts before account locks
    // Matches data dictionary: account_locked field
    private static final int MAX_FAILED_ATTEMPTS = 5;

    // Session timeout in minutes (30 mins of inactivity)
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    private final UserService userService;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public SessionSecurityManager(UserService userService) {
        this.userService = userService;
    }

    // ==============================================================
    // SECTION 1: SESSION TOKEN MANAGEMENT
    // ==============================================================

    /**
     * Generates a new session token and stores it in the session.
     * Called immediately after successful login.
     * Matches: sessionToken = String (Global, held in memory)
     */
    public String generateSessionToken(HttpSession session, String userId) {
        String token = UUID.randomUUID().toString();

        // Store token in session
        session.setAttribute("sessionToken", token);
        session.setAttribute("currentUserID", userId);
        session.setAttribute("tokenCreatedAt", LocalDateTime.now().toString());

        // Set session timeout — 30 minutes of inactivity
        session.setMaxInactiveInterval(SESSION_TIMEOUT_MINUTES * 60);

        return token;
    }

    /**
     * Validates that the session token is present and the session is active.
     * Called at the start of any protected page or action.
     * Returns true if the session is valid, false if expired or tampered with.
     */
    public boolean validateSession(HttpSession session) {
        try {
            String token   = (String) session.getAttribute("sessionToken");
            Boolean isAuth = (Boolean) session.getAttribute("isAuthenticated");
            String userId  = (String) session.getAttribute("currentUserID");

            // All three must be present and valid
            if (token == null || token.isEmpty()) return false;
            if (!Boolean.TRUE.equals(isAuth))     return false;
            if (userId == null || userId.isEmpty()) return false;

            return true;

        } catch (IllegalStateException e) {
            // Session has already been invalidated
            return false;
        }
    }

    /**
     * Clears the session completely on logout.
     * Matches: isAuthenticated = FALSE on logout
     */
    public void clearSession(HttpSession session) {
        try {
            session.invalidate();
        } catch (IllegalStateException e) {
            // Session already invalidated — that's fine
        }
    }

    /**
     * Returns the current user's role from the session.
     * Used to control access to admin/staff only pages.
     * Matches: currentUserRole ENUM from the variable table
     */
    public String getSessionUserRole(HttpSession session) {
        try {
            return (String) session.getAttribute("currentUserRole");
        } catch (IllegalStateException e) {
            return null;
        }
    }

    // ==============================================================
    // SECTION 2: ACCOUNT LOCKING
    // ==============================================================

    /**
     * Records a failed login attempt for a user.
     * Automatically locks the account after MAX_FAILED_ATTEMPTS.
     * Matches data dictionary: account_locked, failed_login_attempts
     * Returns the updated number of failed attempts.
     */
    public int recordFailedLoginAttempt(String userId) {
        int attempts = userService.incrementFailedAttempts(userId);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            userService.lockAccount(userId);
            System.out.println("Account locked for user: " + userId +
                               " after " + attempts + " failed attempts.");
        }

        return attempts;
    }

    /**
     * Resets failed login attempts after a successful login.
     * Called immediately when login succeeds.
     */
    public void resetFailedAttempts(String userId) {
        userService.resetFailedAttempts(userId);
    }

    /**
     * Checks whether a user's account is currently locked.
     * Called at the start of the login process.
     */
    public boolean isAccountLocked(String userId) {
        User user = userService.findById(userId);
        return user != null && user.isAccountLocked();
    }

    /**
     * Unlocks a user's account — for use by admin or support staff only.
     * Resets the failed attempts counter too.
     */
    public SecurityResult unlockAccount(HttpSession session, String targetUserId) {

        // Only ADMIN or AMAZON_STAFF can unlock accounts
        String role = getSessionUserRole(session);
        if (!"ADMIN".equals(role) && !"AMAZON_STAFF".equals(role)) {
            return new SecurityResult(false,
                    "ERROR: You do not have permission to unlock accounts.");
        }

        userService.unlockAccount(targetUserId);
        userService.resetFailedAttempts(targetUserId);

        return new SecurityResult(true,
                "Account unlocked successfully. The user can now log in again.");
    }

    // ==============================================================
    // SECTION 3: GDPR CONSENT TRACKING
    // ==============================================================

    /**
     * Records that a user has given GDPR consent.
     * Called at registration when user ticks the consent checkbox.
     * Matches data dictionary: gdpr_consent BOOLEAN, consent_date TIMESTAMP
     */
    public SecurityResult recordGdprConsent(String userId) {
        String consentDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        userService.recordGdprConsent(userId, consentDate);

        return new SecurityResult(true,
                "GDPR consent recorded at " + consentDate + " (UK GDPR Article 7).");
    }

    /**
     * Checks that a user has given valid GDPR consent.
     * Called before processing or displaying any personal data.
     * Returns false if consent was never given or has been withdrawn.
     */
    public boolean hasValidGdprConsent(String userId) {
        User user = userService.findById(userId);
        return user != null && user.isGdprConsent();
    }

    /**
     * Handles a user's request to withdraw GDPR consent and delete their data.
     * This is the Right to Erasure under UK GDPR Article 17.
     * Soft deletes the account and clears the session.
     */
    public SecurityResult withdrawConsentAndDeleteData(HttpSession session) {

        if (!validateSession(session)) {
            return new SecurityResult(false,
                    "ERROR: You must be logged in to withdraw consent.");
        }

        String userId = (String) session.getAttribute("currentUserID");

        // Soft delete — sets deleted_at timestamp, keeps record for legal purposes
        // Data dictionary: deleted_at TIMESTAMP (null = active)
        userService.softDeleteUser(userId);

        // Clear consent flag
        userService.revokeGdprConsent(userId);

        // End the session
        clearSession(session);

        return new SecurityResult(true,
                "Your consent has been withdrawn and your account data has been " +
                "scheduled for deletion in line with UK GDPR Article 17 " +
                "(Right to Erasure). This will be completed within 30 days.");
    }

    // ---------------------------------------------------------------
    // Result class
    // ---------------------------------------------------------------
    public static class SecurityResult {
        private final boolean success;
        private final String message;

        public SecurityResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage()  { return message; }

        @Override
        public String toString() {
            return "SecurityResult{success=" + success + ", message='" + message + "'}";
        }
    }
}
