package com.sunrisedental.dao;

import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.StaffUser;
import com.sunrisedental.model.TreatmentType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.sunrisedental.model.StaffUserBuilder;

public class AdminDAO {

    public List<StaffUser> listStaff() throws SQLException {
        String sql = "SELECT user_id, username, full_name, role, status FROM users ORDER BY user_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<StaffUser> list = new ArrayList<>();
            while (rs.next()) {
                // Using Builder Pattern here
                StaffUser s = new StaffUserBuilder()
                                .setId(rs.getInt("user_id"))
                                .setUsername(rs.getString("username"))
                                .setFullName(rs.getString("full_name"))
                                .setRole(rs.getString("role"))
                                .setStatus(rs.getString("status"))
                                .build();
                list.add(s);
            }
            return list;
        }
    }

    public void createStaff(String fullName, String username, String password, String role) throws SQLException {
        String sql = "INSERT INTO users (username, password, full_name, role, status) VALUES (?,?,?,?, 'active')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullName);
            ps.setString(4, role);
            ps.executeUpdate();
        }
    }

    public List<Dentist> listDentists() throws SQLException {
        String sql = "SELECT dentist_id, name, specialization FROM dentists WHERE status = 'active' ORDER BY name";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Dentist> list = new ArrayList<>();
            while (rs.next()) {
                Dentist d = new Dentist();
                d.setId(rs.getInt("dentist_id"));
                d.setName(rs.getString("name"));
                d.setSpecialization(rs.getString("specialization"));
                list.add(d);
            }
            return list;
        }
    }

    public void addDentist(String name, String specialization) throws SQLException {
        String sql = "INSERT INTO dentists (name, specialization, status) VALUES (?,?, 'active')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, specialization != null ? specialization : "General Dentistry");
            ps.executeUpdate();
        }
    }

    public List<TreatmentType> listTreatments() throws SQLException {
        String sql = "SELECT treatment_id, name, price FROM treatment_types WHERE status = 'active' ORDER BY name";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<TreatmentType> list = new ArrayList<>();
            while (rs.next()) {
                TreatmentType t = new TreatmentType();
                t.setId(rs.getInt("treatment_id"));
                t.setName(rs.getString("name"));
                t.setPrice(rs.getDouble("price"));
                list.add(t);
            }
            return list;
        }
    }

    public void addTreatment(String name, double price) throws SQLException {
        String sql = "INSERT INTO treatment_types (name, price, status) VALUES (?,?, 'active')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.executeUpdate();
        }
    }

    public double getConsultationFee() throws SQLException {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = 'consultation_fee'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? Double.parseDouble(rs.getString("setting_value")) : 1500.0;
        }
    }

    public void updateConsultationFee(double fee) throws SQLException {
        String sql = "UPDATE settings SET setting_value = ? WHERE setting_key = 'consultation_fee'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(fee));
            ps.executeUpdate();
        }
    }

    /**
     * Update staff user details in the database.
     * If password is empty/null, keep the existing password.
     */
    public void updateStaff(int userId, String fullName, String username, String password, String role) throws SQLException {
        if (password != null && !password.trim().isEmpty()) {
            String sql = "UPDATE users SET full_name = ?, username = ?, password = ?, role = ? WHERE user_id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, fullName);
                ps.setString(2, username);
                ps.setString(3, password);
                ps.setString(4, role);
                ps.setInt(5, userId);
                ps.executeUpdate();
            }
        } else {
            String sql = "UPDATE users SET full_name = ?, username = ?, role = ? WHERE user_id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, fullName);
                ps.setString(2, username);
                ps.setString(3, role);
                ps.setInt(4, userId);
                ps.executeUpdate();
            }
        }
    }

   /**
     * Hard-delete staff user.
     */
    public void deleteStaff(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}
