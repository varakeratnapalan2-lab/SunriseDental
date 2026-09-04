package com.sunrisedental.dao;
import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.AppointmentDetail;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
public class AppointmentDAO {

    public String createAppointment(String patientName, String address, String contactNumber,
                                    int dentistId, int treatmentId, String date, String time,
                                    int createdBy) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall("{CALL sp_create_appointment(?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, patientName);
            cs.setString(2, address);
            cs.setString(3, contactNumber);
            cs.setInt(4, dentistId);
            cs.setInt(5, treatmentId);
            cs.setString(6, date);
            cs.setString(7, time);
            cs.setInt(8, createdBy);
            cs.registerOutParameter(9, Types.VARCHAR);
            cs.registerOutParameter(10, Types.VARCHAR);
            cs.execute();
            String apptNo = cs.getString(9);
            String message = cs.getString(10);
            if (apptNo == null || apptNo.isBlank()) {
                throw new SQLException(message != null ? message : "Registration failed");
            }
            return apptNo;
        }
    }

    public AppointmentDetail findByNumber(String appointmentNumber) throws SQLException {
        String sql = "SELECT * FROM vw_appointment_details WHERE appointment_number = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentNumber.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<AppointmentDetail> findToday() throws SQLException {
        String sql = "SELECT * FROM vw_daily_appointments ORDER BY appointment_time";
        return queryList(sql);
    }

    public List<AppointmentDetail> findAll(String date, String dentistName) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM vw_appointment_details WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (date != null && !date.isBlank()) {
            sql.append(" AND appointment_date = ?");
            params.add(date);
        }
        if (dentistName != null && !dentistName.isBlank()) {
            sql.append(" AND dentist_name = ?");
            params.add(dentistName);
        }
        sql.append(" ORDER BY appointment_date DESC, appointment_time");
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<AppointmentDetail> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        }
    }

    public void updateStatus(String appointmentNumber, String status) throws SQLException {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_number = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, appointmentNumber.toUpperCase());
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Appointment not found");
            }
        }
    }

    public Integer findDentistIdByName(String name) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT dentist_id FROM dentists WHERE name = ? AND status = 'active'")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("dentist_id") : null;
            }
        }
    }

    public Integer findTreatmentIdByName(String name) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT treatment_id FROM treatment_types WHERE name = ? AND status = 'active'")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("treatment_id") : null;
            }
        }
    }

    private List<AppointmentDetail> queryList(String sql) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<AppointmentDetail> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        }
    }

    private AppointmentDetail mapRow(ResultSet rs) throws SQLException {
        AppointmentDetail a = new AppointmentDetail();
        a.setAppointmentNumber(rs.getString("appointment_number"));
        a.setPatientName(rs.getString("patient_name"));
        a.setAddress(rs.getString("address"));
        a.setContactNumber(rs.getString("contact_number"));
        a.setDentistName(rs.getString("dentist_name"));
        a.setTreatmentType(rs.getString("treatment_type"));
        a.setAppointmentDate(rs.getDate("appointment_date").toString());
        a.setAppointmentTime(rs.getTime("appointment_time").toString().substring(0, 5));
        a.setStatus(rs.getString("status"));
        a.setTreatmentPrice(rs.getDouble("treatment_price"));
        return a;
    }
}
