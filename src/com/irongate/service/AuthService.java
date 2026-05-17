package com.irongate.service;

import com.irongate.dao.ActivityLogDAO;
import com.irongate.dao.UserDAO;
import com.irongate.model.User;
import com.irongate.security.AESUtil;
import com.irongate.security.BCryptUtil;
import com.irongate.security.OTPService;
import com.irongate.util.SessionManager;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private final UserDAO         userDAO  = new UserDAO();
    private final ActivityLogDAO  logDAO   = new ActivityLogDAO();

    // In-memory OTP store: username → {otp, expiry epoch second}
    private static final Map<String, String[]> OTP_STORE = new HashMap<>();
    private static final long OTP_TTL_SECONDS = 300; // 5 minutes

    // ── Registration ────────────────────────────────────────────

    public enum RegisterResult { SUCCESS, USERNAME_TAKEN, EMAIL_TAKEN, ERROR }

    public RegisterResult register(String username, String email, String password) {
        if (userDAO.usernameExists(username)) return RegisterResult.USERNAME_TAKEN;
        if (userDAO.emailExists(email))       return RegisterResult.EMAIL_TAKEN;
        String hash = BCryptUtil.hash(password);
        int id = userDAO.registerUser(username, email, hash);
        if (id == -1) return RegisterResult.ERROR;
        userDAO.createUserSecurity(id);
        logDAO.log(id, null, "REGISTER");
        return RegisterResult.SUCCESS;
    }

    // ── Login Step 1: verify credentials ────────────────────────

    public enum LoginResult { SUCCESS, NEED_OTP, BAD_CREDENTIALS, ERROR }

    /**
     * Validates username + password.
     * If 2FA is off → establishes session and returns SUCCESS.
     * If 2FA is on  → sends OTP email and returns NEED_OTP.
     */
    public LoginResult login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            return LoginResult.BAD_CREDENTIALS;
        }
        if (!BCryptUtil.verify(password, user.getPasswordHash())) {
            logDAO.log(user.getUserId(), null, "LOGIN_FAIL");
            return LoginResult.BAD_CREDENTIALS;
        }

        if (userDAO.is2FAEnabled(user.getUserId())) {
            // Send OTP
            try {
                String otp    = OTPService.generateOTP();
                long   expiry = Instant.now().getEpochSecond() + OTP_TTL_SECONDS;
                OTP_STORE.put(username, new String[]{otp, String.valueOf(expiry)});
                OTPService.sendOTP(user.getEmail(), otp);
                logDAO.log(user.getUserId(), null, "OTP_SENT");
                return LoginResult.NEED_OTP;
            } catch (Exception e) {
                e.printStackTrace();
                return LoginResult.ERROR;
            }
        }

        // 2FA disabled — create session
        establishSession(user);
        return LoginResult.SUCCESS;
    }

    // ── Login Step 2: verify OTP ─────────────────────────────────

    public enum OTPResult { SUCCESS, INVALID, EXPIRED }

    public OTPResult verifyOTP(String username, String enteredOtp) {
        String[] stored = OTP_STORE.get(username);
        if (stored == null) return OTPResult.INVALID;

        long expiry = Long.parseLong(stored[1]);
        if (Instant.now().getEpochSecond() > expiry) {
            OTP_STORE.remove(username);
            return OTPResult.EXPIRED;
        }
        if (!stored[0].equals(enteredOtp.trim())) return OTPResult.INVALID;

        OTP_STORE.remove(username);
        User user = userDAO.findByUsername(username);
        if (user == null) return OTPResult.INVALID;
        establishSession(user);
        return OTPResult.SUCCESS;
    }

    // ── Session ──────────────────────────────────────────────────

    private void establishSession(User user) {
        try {
            String key = AESUtil.generateKey();
            SessionManager.login(user, key);
            logDAO.log(user.getUserId(), null, "LOGIN");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        if (SessionManager.isLoggedIn()) {
            logDAO.log(SessionManager.getCurrentUser().getUserId(), null, "LOGOUT");
        }
        SessionManager.logout();
    }

    // ── 2FA toggle ───────────────────────────────────────────────

    public void toggle2FA(boolean enable) {
        if (!SessionManager.isLoggedIn()) return;
        int uid = SessionManager.getCurrentUser().getUserId();
        userDAO.set2FA(uid, enable);
        logDAO.log(uid, null, enable ? "2FA_ENABLED" : "2FA_DISABLED");
    }

    public boolean is2FAEnabled() {
        if (!SessionManager.isLoggedIn()) return false;
        return userDAO.is2FAEnabled(SessionManager.getCurrentUser().getUserId());
    }
}
