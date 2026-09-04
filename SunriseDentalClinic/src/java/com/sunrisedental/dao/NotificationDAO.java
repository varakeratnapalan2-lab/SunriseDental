package com.sunrisedental.dao;

import com.sunrisedental.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NotificationDAO {

    public int findAppointmentId(String appointmentNumber) throws SQLException {
        String sql = "SELECT appointment_id FROM appointments WHERE appointment_number = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentNumber.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("appointment_id") : -1;
            }
        }
    }

    public void saveNotification(int appointmentId, String type, String recipient, String message) throws SQLException {
        String sql = "INSERT INTO notifications (appointment_id, notify_type, recipient, message, status) VALUES (?,?,?,?,'Sent')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ps.setString(2, type.toLowerCase());
            ps.setString(3, recipient);
            ps.setString(4, message);
            ps.executeUpdate();
        }
    }
}
