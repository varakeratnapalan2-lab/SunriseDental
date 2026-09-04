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

@WebServlet(name = "AdminServlet", urlPatterns = {"/api/admin", "/api/admin/*"})
public class AdminServlet extends BaseServlet {

    private final ClinicFacade facade = ClinicFacade.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserSession user = SessionHelper.getUser(req);
        String path = req.getPathInfo();
        try {
            if ("/users".equals(path)) {
                SessionHelper.requireRole(user, "ADMIN");
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, facade.listStaff());
            } else if ("/dentists".equals(path)) {
                SessionHelper.requireRole(user, "ADMIN", "RECEPTIONIST");
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, facade.listDentists());
            } else if ("/treatments".equals(path)) {
                SessionHelper.requireRole(user, "ADMIN", "RECEPTIONIST");
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, facade.listTreatments());
            } else if ("/consultation-fee".equals(path)) {
                SessionHelper.requireRole(user, "ADMIN", "RECEPTIONIST");
                Map<String, Object> result = new HashMap<>();
                result.put("value", facade.getConsultationFee());
                result.put("consultationFee", facade.getConsultationFee());
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, result);
            } else {
                JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown admin endpoint");
            }
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserSession user = SessionHelper.getUser(req);
        String path = req.getPathInfo();
        try {
            SessionHelper.requireRole(user, "ADMIN");
            JsonObject body = JsonUtil.parseBody(readBody(req));
            if ("/users".equals(path)) {
                facade.createStaff(
                    body.get("fullName").getAsString(),
                    body.get("username").getAsString(),
                    body.get("password").getAsString(),
                    body.get("role").getAsString()
                );
            } else if ("/dentists".equals(path)) {
                facade.addDentist(body.get("name").getAsString(),
                    body.has("specialization") ? body.get("specialization").getAsString() : "General Dentistry");
            } else if ("/treatments".equals(path)) {
                facade.addTreatment(body.get("name").getAsString(), body.get("price").getAsDouble());
            } else {
                JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown admin endpoint");
                return;
            }
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, result);
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserSession user = SessionHelper.getUser(req);
        String path = req.getPathInfo();
        try {
            SessionHelper.requireRole(user, "ADMIN");
            JsonObject body = JsonUtil.parseBody(readBody(req));
            if ("/consultation-fee".equals(path)) {
                double fee = body.has("value") ? body.get("value").getAsDouble() : body.get("consultationFee").getAsDouble();
                facade.updateConsultationFee(fee);
            } else if (path != null && path.startsWith("/users/")) {
                // PUT /api/admin/users/{id} — Update staff user
                int userId = Integer.parseInt(path.substring("/users/".length()));
                String fullName = body.get("fullName").getAsString();
                String username = body.get("username").getAsString();
                String password = body.has("password") && !body.get("password").getAsString().isEmpty()
                                  ? body.get("password").getAsString() : null;
                String role = body.get("role").getAsString();
                facade.updateStaff(userId, fullName, username, password, role);
            } else {
                JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown admin endpoint");
                return;
            }
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, result);
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserSession user = SessionHelper.getUser(req);
        String path = req.getPathInfo();
        try {
            SessionHelper.requireRole(user, "ADMIN");
            if (path != null && path.startsWith("/users/")) {
                // DELETE /api/admin/users/{id} — Deactivate staff user
                int userId = Integer.parseInt(path.substring("/users/".length()));
                facade.deleteStaff(userId);
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, result);
            } else {
                JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown admin endpoint");
            }
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
