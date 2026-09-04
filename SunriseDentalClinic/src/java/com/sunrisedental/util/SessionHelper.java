package com.sunrisedental.util;

import com.sunrisedental.model.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessionHelper {

    public static final String SESSION_USER = "sunriseUser";

    private SessionHelper() {
    }

    public static UserSession getUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object obj = session.getAttribute(SESSION_USER);
        return obj instanceof UserSession ? (UserSession) obj : null;
    }

    public static void setUser(HttpServletRequest request, UserSession user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER, user);
    }

    public static void clearUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public static void requireRole(UserSession user, String... roles) {
        if (user == null) {
            throw new SecurityException("Not authenticated");
        }
        for (String role : roles) {
            if (role.equals(user.getRole())) {
                return;
            }
        }
        throw new SecurityException("Access denied for role: " + user.getRole());
    }
}
