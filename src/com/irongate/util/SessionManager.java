package com.irongate.util;

import com.irongate.model.User;

public class SessionManager {

    private static User currentUser;
    private static String sessionKey; // AES key tied to this session

    public static void login(User user, String key) {
        currentUser = user;
        sessionKey  = key;
    }

    public static void logout() {
        currentUser = null;
        sessionKey  = null;
    }

    public static User getCurrentUser() { return currentUser; }
    public static String getSessionKey() { return sessionKey; }
    public static boolean isLoggedIn()   { return currentUser != null; }
}
