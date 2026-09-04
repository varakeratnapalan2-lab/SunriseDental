package com.sunrisedental.controller;

import com.google.gson.JsonObject;
import com.sunrisedental.model.UserSession;
import com.sunrisedental.service.ClinicFacade;
import com.sunrisedental.util.JsonUtil;
import com.sunrisedental.util.SessionHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * MVC Pattern — Controller layer for authentication (View = HTML, Model = UserSession).
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/api/auth", "/api/auth/*"})
public class AuthServlet extends BaseServlet {

    private final ClinicFacade facade = ClinicFacade.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) {
            path = "/";
        }
        try {
            if ("/login".equals(path)) {
                handleLogin(req, resp);
            } else if ("/logout".equals(path)) {
                handleLogout(req, resp);
            } else {
                JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown auth endpoint");
            }
        } catch (SQLException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        JsonObject body = JsonUtil.parseBody(readBody(req));
        String username = body.has("username") ? body.get("username").getAsString().trim() : "";
        String password = body.has("password") ? body.get("password").getAsString() : "";

        if (username.isEmpty() || password.length() < 4) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid credentials");
            return;
        }

        UserSession user = facade.login(username, password);
        if (user == null) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
            return;
        }

        SessionHelper.setUser(req, user);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("fullName", user.getFullName());
        result.put("role", user.getRole());
        if (user.getDentistName() != null) {
            result.put("dentistName", user.getDentistName());
        }
        JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, result);
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionHelper.clearUser(req);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, result);
    }
}
