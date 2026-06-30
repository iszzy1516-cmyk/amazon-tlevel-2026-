package com.amazon.tlevel.service;

import com.amazon.tlevel.model.User;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    private final Map<String, User> byId = new HashMap<>();
    private final Map<String, User> byEmail = new HashMap<>();

    public User findById(String userId) { return byId.get(userId); }
    public User findByEmail(String email) { return byEmail.get(email.toLowerCase().trim()); }
    public boolean emailExists(String email) { return byEmail.containsKey(email.toLowerCase().trim()); }

    public void saveUser(User user) {
        byId.put(user.getUserId(), user);
        byEmail.put(user.getEmail().toLowerCase(), user);
    }

    public void updateUser(User user) { saveUser(user); }

    public int incrementFailedAttempts(String userId) {
        User u = byId.get(userId);
        if (u == null) return 0;
        int n = u.getFailedLoginAttempts() + 1;
        u.setFailedLoginAttempts(n);
        return n;
    }

    public void resetFailedAttempts(String userId) {
        User u = byId.get(userId);
        if (u != null) u.setFailedLoginAttempts(0);
    }

    public void lockAccount(String userId) {
        User u = byId.get(userId);
        if (u != null) u.setAccountLocked(true);
    }

    public void unlockAccount(String userId) {
        User u = byId.get(userId);
        if (u != null) u.setAccountLocked(false);
    }

    public void softDeleteUser(String userId) {
        User u = byId.get(userId);
        if (u != null) u.setDeletedAt(LocalDateTime.now().toString());
    }

    public void recordGdprConsent(String userId, String date) {
        User u = byId.get(userId);
        if (u != null) { u.setGdprConsent(true); u.setConsentDate(date); }
    }

    public void revokeGdprConsent(String userId) {
        User u = byId.get(userId);
        if (u != null) u.setGdprConsent(false);
    }
}
