# Amazon T-Level Portal — System Documentation

## Overview

The **Amazon T-Level Portal** is a full-stack Java web application built with **Spring Boot 3.2.5** and vanilla HTML/CSS/JavaScript. It connects students, parents, college staff, and Amazon administrators to T-Level course information across the UK. The system features role-based authentication, GDPR-compliant user management, course search/filtering, and a responsive Amazon-themed UI.

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                         │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐  │
│  │  index   │ │  login   │ │  signup  │ │  dashboard-* │  │
│  │ (public) │ │ (public) │ │ (public) │ │ (role-based) │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────────┘  │
│                         ↕ HTTP (port 8089)                 │
├─────────────────────────────────────────────────────────────┤
│                      SPRING BOOT LAYER                       │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  TLevelApplication.java  (@SpringBootApplication)   │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │
│  │ Controllers │ │  Services   │ │   Security Layer    │   │
│  │  (REST API) │ │(Business Logic)│  (Session/Auth)    │   │
│  │             │ │             │ │                     │   │
│  │ • Login     │ │ • UserService│ │ • SessionSecurity   │   │
│  │ • Register  │ │ • CourseSvc  │ │   Manager           │   │
│  │ • Home      │ │ • EmailSvc   │ │ • Account Locking   │   │
│  │ • Content   │ │             │ │ • GDPR Consent      │   │
│  │ • Update    │ │             │ │                     │   │
│  │ • Contact   │ │             │ │                     │   │
│  └─────────────┘ └─────────────┘ └─────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              In-Memory Data Store                  │   │
│  │         (HashMap — resets on restart)              │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## How the System Works

### 1. Registration Flow
1. User fills `signup.html` → JavaScript validates passwords match and meet complexity rules
2. Form submits `POST /api/register` with `userFirstName`, `userLastName`, `userEmail`, `userPassword`, `userType`, `userLocation`, `gdprConsent`
3. `RegisterController` validates:
   - Email contains `@` and `.`
   - All required fields present
   - GDPR consent is `true`
   - Email doesn't already exist
   - Password ≥ 8 chars, 1 uppercase, 1 number
4. Password hashed with **BCrypt** (`gensalt(12)`) — plaintext never stored
5. `UserService` saves user to in-memory `HashMap`
6. Returns JSON → frontend redirects to login page

### 2. Login & Role-Based Routing
1. User fills `student-login.html` → submits `POST /api/login`
2. `LoginController`:
   - Looks up user by email
   - Checks if account is locked (after 5 failed attempts)
   - Compares BCrypt hash of input password vs stored hash
   - On success: resets failed attempts, creates session variables (`isAuthenticated`, `currentUserRole`, `currentUserID`, `sessionToken`, accessibility prefs)
   - On failure: increments failed attempts, locks account at 5
3. Frontend receives JSON with `userRole` field
4. JavaScript redirects:
   - `STUDENT` → `dashboard-student.html`
   - `PARENT` → `dashboard-parent.html`
   - `COLLEGE_STAFF` → `dashboard-staff.html`
   - `AMAZON_STAFF` / `ADMIN` → `dashboard-admin.html`

### 3. Session Security
- `SessionSecurityManager` generates UUID session tokens
- Sessions expire after **30 minutes** of inactivity
- Every protected request checks `sessionToken`, `isAuthenticated`, and `currentUserID`
- `clearSession()` invalidates on logout

### 4. Course Discovery
- `HomePageController` serves `GET /api/courses` (all courses)
- `GET /api/courses/search?searchQuery=...` filters by name
- `GET /api/courses/filter?subject=...&location=...` filters by subject and location
- `index.html` uses JavaScript `fetch()` to load and render course cards dynamically

### 5. Contact Form
- `POST /api/contact/submit` validates inputs (name, email, message length 10–2000)
- `EmailService` logs the email to console (stub — replace with SMTP for production)
- Returns success/error message to frontend

### 6. GDPR Compliance
- `recordGdprConsent()` timestamps consent at registration
- `hasValidGdprConsent()` checks before processing personal data
- `withdrawConsentAndDeleteData()` soft-deletes account (sets `deletedAt`) and clears session
- All data dictionary fields match UK GDPR Article 7 (consent) and Article 17 (right to erasure)

---

## Features

| Feature | Implementation |
|---------|---------------|
| **Role-Based Access** | 4 distinct dashboards (Student, Parent, Staff, Admin) |
| **Account Security** | BCrypt hashing, account lockout after 5 fails, session tokens |
| **Course Search** | Real-time search + filter by subject/location |
| **GDPR Compliance** | Consent tracking, soft-delete, data minimization |
| **Responsive Design** | Mobile hamburger menu, CSS Grid/Flexbox |
| **Accessibility** | Dark mode, high contrast, font size preferences (stored in session) |
| **Form Validation** | Client-side + server-side validation with error messages |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2.5, Spring Web |
| Security | BCrypt (jbcrypt 0.4), HttpSession |
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Build Tool | Maven 3.9+ |
| Server | Embedded Apache Tomcat 10.1 |

---

## Prerequisites

- **Java 17 or higher** (`java -version`)
- **Maven 3.9+** (`mvn -version`)
- **Git** (for cloning)

---

## Installation & Running

### Step 1: Clone the Repository
```bash
git clone https://github.com/iszzy1516-cmyk/amazon-tlevel-2026-.git
cd amazon-tlevel-2026-
```

### Step 2: Build & Run
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8089"
```

### Step 3: Open in Browser
| Page | URL |
|------|-----|
| Home (Course Discovery) | http://localhost:8089/ |
| Choose Login | http://localhost:8089/choose-login.html |
| Sign In | http://localhost:8089/student-login.html |
| Register | http://localhost:8089/signup.html |
| Contact | http://localhost:8089/contact.html |
| About | http://localhost:8089/about.html |

### Step 4: Test the API (Terminal)
```bash
# List all courses
curl http://localhost:8089/api/courses

# Register a student
curl -X POST http://localhost:8089/api/register \
  -d "userFirstName=Israel&userLastName=Johnson&userEmail=student@test.com&userPassword=Test1234!&userType=STUDENT&gdprConsent=true"

# Register a parent
curl -X POST http://localhost:8089/api/register \
  -d "userFirstName=John&userLastName=Parent&userEmail=parent@test.com&userPassword=Test1234!&userType=PARENT&gdprConsent=true"

# Register staff
curl -X POST http://localhost:8089/api/register \
  -d "userFirstName=Jane&userLastName=Staff&userEmail=staff@test.com&userPassword=Test1234!&userType=COLLEGE_STAFF&gdprConsent=true"

# Login (returns role for redirect)
curl -X POST http://localhost:8089/api/login \
  -d "inputEmail=student@test.com&inputPassword=Test1234!"
```

---

## API Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET` | `/api/courses` | List all courses | No |
| `GET` | `/api/courses/search?searchQuery=...` | Search by name | No |
| `GET` | `/api/courses/filter?subject=...&location=...` | Filter courses | No |
| `GET` | `/api/course/{courseId}` | Get single course details | No |
| `POST` | `/api/register` | Create new account | No |
| `POST` | `/api/login` | Authenticate user | No |
| `POST` | `/api/logout` | Destroy session | Yes |
| `POST` | `/api/account/update` | Update profile | Yes |
| `POST` | `/api/account/delete` | Soft-delete account | Yes |
| `POST` | `/api/contact/submit` | Send contact form | No |

---

## Role-Based Dashboards

After login, users are automatically redirected to their role-specific dashboard:

| Role | Dashboard URL | Features |
|------|--------------|----------|
| `STUDENT` | `dashboard-student.html` | My Courses, Progress Tracking, Account Settings |
| `PARENT` | `dashboard-parent.html` | Child's Progress, Applications, Messages |
| `COLLEGE_STAFF` | `dashboard-staff.html` | Student Records, Applications, Placements |
| `AMAZON_STAFF` / `ADMIN` | `dashboard-admin.html` | User Management, Analytics, GDPR Audit |

Every dashboard includes a **"Browse More Courses"** button linking back to `index.html`.

---

## Seeded Course Data

The system boots with **10 pre-loaded courses** across the UK:

| Course | Subject | Location |
|--------|---------|----------|
| Digital Production, Design and Development | Digital | London |
| Health and Science | Health | Manchester |
| Engineering, Manufacturing, Processing & Control | Engineering | Birmingham |
| Business Management and Administration | Business | Bristol |
| Legal Services | Legal | Leeds |
| Agriculture, Land Management and Production | Agriculture | Exeter |
| Media, Broadcast and Production | Media | Glasgow |
| Education and Early Years | Education | Liverpool |
| Construction: Design, Surveying and Planning | Construction | Sheffield |
| Science | Science | Cardiff |

---

## Project Structure

```
amazon-tlevel-2026-/
├── pom.xml                              # Maven build config
├── src/
│   ├── main/
│   │   ├── java/com/amazon/tlevel/
│   │   │   ├── TLevelApplication.java   # Spring Boot entry point
│   │   │   ├── controller/
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── RegisterController.java
│   │   │   │   ├── HomePageController.java
│   │   │   │   ├── ContentPageController.java
│   │   │   │   ├── UpdateAccountController.java
│   │   │   │   └── ContactPageController.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Course.java
│   │   │   │   └── ContactMessage.java
│   │   │   ├── security/
│   │   │   │   └── SessionSecurityManager.java
│   │   │   └── service/
│   │   │       ├── UserService.java      # In-memory user store
│   │   │       ├── CourseService.java    # In-memory course store
│   │   │       └── EmailService.java     # Email stub (console logging)
│   │   └── resources/
│   │       └── static/                   # Frontend files
│   │           ├── index.html             # Home / course discovery
│   │           ├── choose-login.html      # Role selection
│   │           ├── student-login.html     # Sign in form
│   │           ├── signup.html            # Registration form
│   │           ├── contact.html           # Contact form
│   │           ├── about.html             # About T-Levels
│   │           ├── dashboard-student.html
│   │           ├── dashboard-parent.html
│   │           ├── dashboard-staff.html
│   │           ├── dashboard-admin.html
│   │           └── styles/
│   │               └── main.css           # Amazon-themed stylesheet
│   │           └── assets/
│   │               └── images/
│   │                   └── amazon-logo.png
└── README.md
```

---

## Important Notes

- **Data is ephemeral** — the in-memory `HashMap` database resets every server restart. For production, replace `UserService` and `CourseService` with JPA repositories + PostgreSQL/MySQL.
- **Email is stubbed** — `EmailService` prints to console. Integrate JavaMailSender or SendGrid for real delivery.
- **Port 8089** is configured via command-line argument. To make it permanent, add `server.port=8089` to `src/main/resources/application.properties`.
- **No HTTPS** — this is a development build. Use a reverse proxy (Nginx/Caddy) or Spring Boot SSL for production.

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `Port 8080 was already in use` | Use `-Dspring-boot.run.arguments="--server.port=8089"` |
| `mvn: command not found` | `sudo apt install maven -y` |
| 404 on `/api/courses` | Ensure controllers have `@RestController` and `@GetMapping` annotations |
| Static files not loading | Verify files are in `src/main/resources/static/` |
| CSS not applying | Check browser DevTools → Network tab for 404s on `styles/main.css` |

---

## Author

**Developer:** Princess Otobo  
**System:** Amazon T-Level Portal — Session & Security Backend  
**Institution:** UK T-Level Technical Qualification Program
