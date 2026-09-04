package com.sunrisedental.observer;

import com.sunrisedental.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditLogObserver implements AppointmentObserver {

    @Override
    public void onStatusUpdated(String appointmentNumber, String newStatus, int userId) {
        String sql = "INSERT INTO audit_log (user_id, action, details) VALUES (?,?,?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, "STATUS_UPDATED");
            ps.setString(3, "Appointment " + appointmentNumber + " status changed to " + newStatus);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Audit log failed: " + e.getMessage());
        }
    }
}
