package com.sunrisedental.dao;

import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.BillResult;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class BillDAO {

    public BillResult calculateBill(String appointmentNumber) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall("{CALL sp_calculate_bill(?,?,?,?,?,?)}")) {
            cs.setString(1, appointmentNumber.toUpperCase());
            cs.registerOutParameter(2, Types.INTEGER);
            cs.registerOutParameter(3, Types.DECIMAL);
            cs.registerOutParameter(4, Types.DECIMAL);
            cs.registerOutParameter(5, Types.DECIMAL);
            cs.registerOutParameter(6, Types.VARCHAR);
            cs.execute();
            String message = cs.getString(6);
            if (cs.getObject(2) == null) {
                throw new SQLException(message != null ? message : "Bill calculation failed");
            }
            BillResult bill = new BillResult();
            bill.setAppointmentNumber(appointmentNumber.toUpperCase());
            bill.setTreatmentCost(cs.getDouble(3));
            bill.setConsultationFee(cs.getDouble(4));
            bill.setTotalAmount(cs.getDouble(5));
            bill.setBillDate(java.time.LocalDate.now().toString());
            fillPatientInfo(conn, bill);
            return bill;
        }
    }

    private void fillPatientInfo(Connection conn, BillResult bill) throws SQLException {
        String sql = "SELECT patient_name, treatment_type, address, contact_number, dentist_name FROM vw_appointment_details "
                + "WHERE appointment_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bill.getAppointmentNumber());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    bill.setPatientName(rs.getString("patient_name"));
                    bill.setTreatmentType(rs.getString("treatment_type"));
                    bill.setPatientAddress(rs.getString("address"));
                    bill.setPatientContact(rs.getString("contact_number"));
                    bill.setDentistName(rs.getString("dentist_name"));
                }
            }
        }
    }
}
