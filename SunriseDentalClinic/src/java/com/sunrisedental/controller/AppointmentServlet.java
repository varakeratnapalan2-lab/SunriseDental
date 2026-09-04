package com.sunrisedental.controller;

import com.google.gson.JsonObject;
import com.sunrisedental.model.AppointmentDetail;
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
import java.util.List;
import java.util.Map;

@WebServlet(name = "AppointmentServlet", urlPatterns = {"/api/appointments", "/api/appointments/*"})
public class AppointmentServlet extends BaseServlet {

    private final ClinicFacade facade = ClinicFacade.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserSession user = SessionHelper.getUser(req);
        if (user == null) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
            return;
        }
        String path = req.getPathInfo();
        try {
            if ("/today".equals(path)) {
                List<AppointmentDetail> list = facade.getTodayAppointments();
                if ("DENTIST".equals(user.getRole()) && user.getDentistName() != null) {
                    list.removeIf(a -> !user.getDentistName().equals(a.getDentistName()));
                }
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, list);
            } else if (path != null && path.length() > 1) {
                String num = path.substring(1).toUpperCase();
                AppointmentDetail apt = facade.getAppointment(num);
                if (apt == null) {
                    JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Appointment not found");
                    return;
                }
                if ("DENTIST".equals(user.getRole()) && user.getDentistName() != null
                        && !user.getDentistName().equals(apt.getDentistName())) {
                    JsonUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, "This appointment is not assigned to you");
                    return;
                }
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, apt);
            } else {
                SessionHelper.requireRole(user, "ADMIN", "RECEPTIONIST", "DENTIST");
                String date = req.getParameter("date");
                String dentist = req.getParameter("dentist");
                // If logged in as DENTIST, restrict query to dentist's own name
                if ("DENTIST".equals(user.getRole()) && user.getDentistName() != null) {
                    dentist = user.getDentistName();
                }
                List<AppointmentDetail> list = facade.getAllAppointments(date, dentist);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, list);
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
        try {
            SessionHelper.requireRole(user, "RECEPTIONIST", "ADMIN");
            JsonObject body = JsonUtil.parseBody(readBody(req));
            Map<String, String> payload = new HashMap<>();
            payload.put("patientName", getStr(body, "patientName"));
            payload.put("address", getStr(body, "address"));
            payload.put("contactNumber", getStr(body, "contactNumber"));
            payload.put("dentistName", getStr(body, "dentistName"));
            payload.put("treatmentType", getStr(body, "treatmentType"));
            payload.put("appointmentDate", getStr(body, "appointmentDate"));
            payload.put("appointmentTime", getStr(body, "appointmentTime"));

            String apptNo = facade.registerAppointment(payload, user.getUserId());
            Map<String, Object> result = new HashMap<>();
            result.put("appointmentNumber", apptNo);
            result.put("success", true);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, result);
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
@Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserSession user = SessionHelper.getUser(req);
        if (user == null) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
            return;
        }
        String path = req.getPathInfo();
        if (path == null || !path.endsWith("/status")) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint");
            return;
        }
        String num = path.substring(1, path.length() - "/status".length());
        try {
            SessionHelper.requireRole(user, "DENTIST", "ADMIN");
            JsonObject body = JsonUtil.parseBody(readBody(req));
            String status = getStr(body, "status");
            facade.updateStatus(num, status, user);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, result);
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
private String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }
}
