package com.sunrisedental.dao;

import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.AppointmentDetail;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO {

    public Map<String, Object> dailyReport(String date) throws SQLException {
        String sql = "SELECT * FROM vw_appointment_details WHERE appointment_date = ? AND status <> 'Cancelled' ORDER BY "
                + "appointment_time";
        List<AppointmentDetail> appointments = new ArrayList<>();
        Map<String, Integer> byDentist = new HashMap<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AppointmentDetail a = mapRow(rs);
                    appointments.add(a);
                    byDentist.merge(a.getDentistName(), 1, Integer::sum);
                }
            }
        }
        Map<String, Object> report = new HashMap<>();
        report.put("date", date);
        report.put("total", appointments.size());
        report.put("appointments", appointments);
        report.put("byDentist", byDentist);
        return report;
    }

    public Map<String, Object> revenueReport(String from, String to) throws SQLException {
        String sql = """
            SELECT t.name AS treatment_type, SUM(b.total_amount) AS revenue
            FROM bills b
            JOIN appointments a ON a.appointment_id = b.appointment_id
            JOIN treatment_types t ON t.treatment_id = a.treatment_id
            WHERE b.bill_date BETWEEN ? AND ?
            GROUP BY t.name
            """;
        Map<String, Double> byTreatment = new HashMap<>();
        double total = 0;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from);
            ps.setString(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double rev = rs.getDouble("revenue");
                    byTreatment.put(rs.getString("treatment_type"), rev);
                    total += rev;
                }
            }
        }
        Map<String, Object> report = new HashMap<>();
        report.put("from", from);
        report.put("to", to);
        report.put("total", total);
        report.put("byTreatment", byTreatment);
        return report;
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
