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

@WebServlet(name = "NotificationServlet", urlPatterns = {"/api/notifications", "/api/notifications/*"})
public class NotificationServlet extends BaseServlet {

    private final ClinicFacade facade = ClinicFacade.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserSession user = SessionHelper.getUser(req);
        try {
            SessionHelper.requireRole(user, "RECEPTIONIST", "ADMIN");
            if (!"/send".equals(req.getPathInfo())) {
                JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown notification endpoint");
                return;
            }
            JsonObject body = JsonUtil.parseBody(readBody(req));
            String num = body.has("appointmentNumber") ? body.get("appointmentNumber").getAsString().trim() : "";
            String type = body.has("type") ? body.get("type").getAsString() : "sms";
            if (num.isEmpty()) {
                JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Appointment number required");
                return;
            }
            String message = facade.sendReminder(num, type);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", message);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, result);
        } catch (IllegalArgumentException | SecurityException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
