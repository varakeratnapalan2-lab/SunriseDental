package com.sunrisedental.controller;

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
import java.util.Map;

@WebServlet(name = "ReportServlet", urlPatterns = {"/api/reports", "/api/reports/*"})
public class ReportServlet extends BaseServlet {

    private final ClinicFacade facade = ClinicFacade.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserSession user = SessionHelper.getUser(req);
        String path = req.getPathInfo();
        try {
            SessionHelper.requireRole(user, "ADMIN");
            Map<String, Object> report;
            if ("/daily".equals(path)) {
                String date = req.getParameter("date");
                if (date == null || date.isBlank()) {
                    JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Date parameter required");
                    return;
                }
                report = facade.dailyReport(date);
            } else if ("/revenue".equals(path)) {
                String from = req.getParameter("from");
                String to = req.getParameter("to");
                if (from == null || to == null) {
                    JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "from and to parameters required");
                    return;
                }
                report = facade.revenueReport(from, to);
            } else {
                JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown report endpoint");
                return;
            }
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, report);
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
