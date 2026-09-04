package com.sunrisedental.dao;

import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.UserSession;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.sunrisedental.factory.UserFactory;

public class UserDAO {

    public UserSession login(String username, String password) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall("{CALL sp_login_user(?, ?)}")) {
            cs.setString(1, username);
            cs.setString(2, password);
            try (ResultSet rs = cs.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                
                // Using Factory Pattern here
                UserSession user = UserFactory.createUserSession(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("full_name"),
                    null // Dentist name will be set later if applicable
                );
                int dentistId = rs.getInt("dentist_id");
                if (!rs.wasNull()) {
                    user.setDentistId(dentistId);
                    user.setDentistName(findDentistName(conn, dentistId));
                }
                return user;
            }
        }
    }

    private String findDentistName(Connection conn, int dentistId) throws SQLException {
        try (var ps = conn.prepareStatement("SELECT name FROM dentists WHERE dentist_id = ?")) {
            ps.setInt(1, dentistId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("name") : null;
            }
        }
    }
}
