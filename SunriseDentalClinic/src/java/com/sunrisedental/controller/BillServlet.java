package com.sunrisedental.controller;

import com.google.gson.JsonObject;
import com.sunrisedental.model.BillResult;
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

@WebServlet(name = "BillServlet", urlPatterns = {"/api/bills", "/api/bills/*"})
public class BillServlet extends BaseServlet {

    private final ClinicFacade facade = ClinicFacade.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserSession user = SessionHelper.getUser(req);
        try {
            SessionHelper.requireRole(user, "RECEPTIONIST", "ADMIN");
            if (!"/calculate".equals(req.getPathInfo())) {
                JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown billing endpoint");
                return;
            }
            JsonObject body = JsonUtil.parseBody(readBody(req));
            String num = body.has("appointmentNumber") ? body.get("appointmentNumber").getAsString().trim() : "";
            if (num.isEmpty()) {
                JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Appointment number required");
                return;
            }
            BillResult bill = facade.calculateBill(num);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, bill);
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
