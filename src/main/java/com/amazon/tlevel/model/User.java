package com.amazon.tlevel.model;

/**
 * User.java
 * Amazon T-Level Portal – User Model
 * Developer: Princess Otobo
 *
 * Fields match the Data Dictionary exactly from the Design Documentation:
 * user_id, email, password_hash, role, first_name, last_name,
 * postcode, t_level_subject, college_id, gdpr_consent, consent_date,
 * email_verified, account_locked, created_at, deleted_at
 *
 * Also includes accessibility preferences from the Variable Table:
 * darkMode, fontSize, highContrast
 */
public class User {

    // --- Core identity fields (from Data Dictionary) ---
    private String userId;          // UUID primary key
    private String email;           // VARCHAR(254), UNIQUE
    private String passwordHash;    // CHAR(60), BCrypt hashed — plaintext NEVER stored
    private String role;            // ENUM: STUDENT / PARENT / COLLEGE_STAFF / AMAZON_STAFF / ADMIN
    private String firstName;       // VARCHAR(100)
    private String lastName;        // VARCHAR(100)
    private String location;        // postcode or town e.g. EX4 3QJ
    private String tLevelSubject;   // e.g. Digital Production
    private String collegeId;       // FK to college/institution

    // --- Security & GDPR fields (from Data Dictionary) ---
    private boolean gdprConsent;    // must be TRUE at sign-up
    private String consentDate;     // timestamp of when consent was given
    private boolean emailVerified;  // true once verification email is clicked
    private boolean accountLocked;  // true after 5 failed login attempts
    private int failedLoginAttempts;// tracks failed attempts before locking
    private String createdAt;       // timestamp of account creation
    private String deletedAt;       // null = active; not null = soft deleted

    // --- Accessibility preferences (from Variable Table) ---
    private boolean darkMode;       // default false
    private String fontSize;        // small / medium / large / x-large, default medium
    private boolean highContrast;   // default false

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------
    public User() {
        // Defaults matching the variable table and data dictionary
        this.emailVerified       = false;
        this.accountLocked       = false;
        this.failedLoginAttempts = 0;
        this.gdprConsent         = false;
        this.darkMode            = false;
        this.highContrast        = false;
        this.fontSize            = "medium";
    }

    // ---------------------------------------------------------------
    // Getters and Setters
    // ---------------------------------------------------------------

    public String getUserId()                    { return userId; }
    public void   setUserId(String userId)       { this.userId = userId; }

    public String getEmail()                     { return email; }
    public void   setEmail(String email)         { this.email = email; }

    public String getPasswordHash()              { return passwordHash; }
    public void   setPasswordHash(String hash)   { this.passwordHash = hash; }

    public String getRole()                      { return role; }
    public void   setRole(String role)           { this.role = role; }

    public String getFirstName()                 { return firstName; }
    public void   setFirstName(String fn)        { this.firstName = fn; }

    public String getLastName()                  { return lastName; }
    public void   setLastName(String ln)         { this.lastName = ln; }

    public String getLocation()                  { return location; }
    public void   setLocation(String location)   { this.location = location; }

    public String getTLevelSubject()             { return tLevelSubject; }
    public void   setTLevelSubject(String s)     { this.tLevelSubject = s; }

    public String getCollegeId()                 { return collegeId; }
    public void   setCollegeId(String id)        { this.collegeId = id; }

    public boolean isGdprConsent()               { return gdprConsent; }
    public void    setGdprConsent(boolean g)     { this.gdprConsent = g; }

    public String getConsentDate()               { return consentDate; }
    public void   setConsentDate(String d)       { this.consentDate = d; }

    public boolean isEmailVerified()             { return emailVerified; }
    public void    setEmailVerified(boolean v)   { this.emailVerified = v; }

    public boolean isAccountLocked()             { return accountLocked; }
    public void    setAccountLocked(boolean l)   { this.accountLocked = l; }

    public int  getFailedLoginAttempts()         { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int f)    { this.failedLoginAttempts = f; }

    public String getCreatedAt()                 { return createdAt; }
    public void   setCreatedAt(String d)         { this.createdAt = d; }

    public String getDeletedAt()                 { return deletedAt; }
    public void   setDeletedAt(String d)         { this.deletedAt = d; }

    public boolean isDarkMode()                  { return darkMode; }
    public void    setDarkMode(boolean d)        { this.darkMode = d; }

    public String getFontSize()                  { return fontSize; }
    public void   setFontSize(String f)          { this.fontSize = f; }

    public boolean isHighContrast()              { return highContrast; }
    public void    setHighContrast(boolean h)    { this.highContrast = h; }

    @Override
    public String toString() {
        return "User{id=" + userId + ", email=" + email +
               ", role=" + role + ", locked=" + accountLocked + "}";
    }
}
