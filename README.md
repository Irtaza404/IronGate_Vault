# IronGate Vault — Setup Guide
**Secure File Management System**
Author: Muhammad Irtaza | 2025-ARID-0151 | BSCS-3B | BIIT

---

## Project Structure

```
IronGate Vault/
├── src/
│   └── com/irongate/
│       ├── util/
│       │   ├── DBConnection.java        ← SQL Server connection string
│       │   └── SessionManager.java      ← In-memory session (user + AES key)
│       ├── model/
│       │   ├── User.java
│       │   ├── VaultFile.java
│       │   └── ActivityLog.java
│       ├── security/
│       │   ├── AESUtil.java             ← AES-256-GCM encrypt / decrypt
│       │   ├── BCryptUtil.java          ← PBKDF2-HMAC-SHA256 password hashing
│       │   ├── HashUtil.java            ← SHA-256 file fingerprinting
│       │   └── OTPService.java          ← javax.mail SMTP OTP sender
│       ├── dao/
│       │   ├── UserDAO.java
│       │   ├── FileDAO.java             ← Duplicate detection (O(1) hash index)
│       │   └── ActivityLogDAO.java
│       ├── service/
│       │   ├── AuthService.java         ← Login, Register, OTP, 2FA
│       │   ├── FileService.java         ← Upload (encrypt), Download (decrypt)
│       │   └── PasswordCrackerService.java ← Priority Queue DSA simulation
│       └── ui/
│           ├── MainApp.java             ← JavaFX entry point
│           ├── StyleUtil.java           ← Shared design tokens
│           ├── LoginScreen.java
│           ├── RegisterScreen.java
│           ├── OTPScreen.java
│           ├── DashboardScreen.java     ← Sidebar shell
│           ├── MyFilesPanel.java
│           ├── DuplicatesPanel.java
│           ├── ActivityLogPanel.java
│           ├── SecurityPanel.java
│           └── PasswordCrackerPanel.java
├── lib/                                 ← Put your JARs here
│   ├── mssql-jdbc-12.x.jre11.jar
│   └── javax.mail.jar
├── vault_storage/                       ← Encrypted files written here at runtime
├── logo.jpg                             ← App icon (copy yours here)
├── run.bat                              ← Windows: compile + run
└── run.sh                              ← Linux/macOS: compile + run
```

---

## Step 1 — Database

Run `IronGate_Vault_Database_Schema.sql` in SSMS against your
`Muhammad-Irtaza\SQLEXPRESS` instance. The connection string in
`DBConnection.java` uses Windows Integrated Security — no password needed.

## Step 2 — JARs in lib/

| JAR | Download from |
|-----|---------------|
| `mssql-jdbc-12.x.jre11.jar` | https://learn.microsoft.com/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server |
| `javax.mail.jar` | https://javaee.github.io/javamail/ (or jakarta.mail) |

Place both inside the `lib/` folder.

## Step 3 — JavaFX SDK

Download JavaFX 21 SDK from https://gluonhq.com/products/javafx/
and extract it. Note the path to the `lib/` folder inside it.

## Step 4 — Configure Email (for OTP/2FA)

Open `src/com/irongate/security/OTPService.java` and set:

```java
private static final String SMTP_USER = "your.gmail@gmail.com";
private static final String SMTP_PASS = "your-app-password";   // Gmail App Password
```

Generate a Gmail App Password at: https://myaccount.google.com/apppasswords

## Step 5 — Build & Run (Windows)

Edit `run.bat` and set:
```bat
set JAVAFX_PATH=C:\path\to\javafx-sdk-21\lib
set JAVA_HOME=C:\Program Files\Java\jdk-21
```
Then double-click `run.bat` or run it in CMD.

---

## DSA Components

| Data Structure | Location | Purpose |
|---|---|---|
| HashMap (O(1) lookup) | `FileDAO.isDuplicate()` + DB index on `file_hash` | Duplicate file detection |
| Priority Queue (min-heap) | `PasswordCrackerService` | Attack strategy ordering |
| Directed Graph (conceptual) | `ActivityLog` table | Audit trail / event graph |
| BST (DB B-Tree index) | `IX_Files_UserId`, `IX_ActivityLog_Timestamp` | Fast file retrieval |

## Security Architecture

- **AES-256-GCM** — every file encrypted before disk write; IV prepended to ciphertext
- **PBKDF2-HMAC-SHA256** — 120,000 iterations; passwords never stored plaintext
- **SHA-256 hashing** — content-based fingerprint for duplicate detection and integrity
- **Session key** — derived fresh on each login; cleared on logout; never persisted
- **OTP** — 6-digit, 5-minute TTL, sent via javax.mail SMTP
