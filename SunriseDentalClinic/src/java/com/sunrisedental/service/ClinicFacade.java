package com.sunrisedental.service;

import com.sunrisedental.builder.AppointmentRegistrationBuilder;
import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.dao.BillDAO;
import com.sunrisedental.dao.NotificationDAO;
import com.sunrisedental.dao.ReportDAO;
import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.dao.AdminDAO;
import com.sunrisedental.factory.DAOFactory;
import com.sunrisedental.model.AppointmentDetail;
import com.sunrisedental.model.BillResult;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.StaffUser;
import com.sunrisedental.model.TreatmentType;
import com.sunrisedental.model.UserSession;
import com.sunrisedental.observer.AppointmentSubject;
import com.sunrisedental.strategy.NotificationStrategy;
import com.sunrisedental.strategy.NotificationStrategyFactory;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Facade Pattern — single simplified interface hiding DAO, Builder, Strategy and Observer complexity.
 */
public class ClinicFacade {

    private static ClinicFacade instance;

    private final UserDAO userDAO;
    private final AppointmentDAO appointmentDAO;
    private final BillDAO billDAO;
    private final AdminDAO adminDAO;
    private final ReportDAO reportDAO;
    private final NotificationDAO notificationDAO;
    private final AppointmentSubject appointmentSubject;

    private ClinicFacade() {
        DAOFactory factory = DAOFactory.getInstance();
        this.userDAO = factory.getUserDAO();
        this.appointmentDAO = factory.getAppointmentDAO();
        this.billDAO = factory.getBillDAO();
        this.adminDAO = factory.getAdminDAO();
        this.reportDAO = factory.getReportDAO();
        this.notificationDAO = factory.getNotificationDAO();
        this.appointmentSubject = AppointmentSubject.getInstance();
    }

    public static synchronized ClinicFacade getInstance() {
        if (instance == null) {
            instance = new ClinicFacade();
        }
        return instance;
    }

    public UserSession login(String username, String password) throws SQLException {
        return userDAO.login(username, password);
    }

    public String registerAppointment(Map<String, String> payload, int createdBy) throws SQLException {
        AppointmentRegistrationBuilder.AppointmentRegistration reg =
            new AppointmentRegistrationBuilder()
                .patientName(payload.get("patientName"))
                .address(payload.get("address"))
                .contactNumber(payload.get("contactNumber"))
                .dentistName(payload.get("dentistName"))
                .treatmentType(payload.get("treatmentType"))
                .appointmentDate(payload.get("appointmentDate"))
                .appointmentTime(payload.get("appointmentTime"))
                .build();

        Integer dentistId = appointmentDAO.findDentistIdByName(reg.getDentistName());
        Integer treatmentId = appointmentDAO.findTreatmentIdByName(reg.getTreatmentType());
        if (dentistId == null) {
            throw new SQLException("Invalid dentist selected");
        }
        if (treatmentId == null) {
            throw new SQLException("Invalid treatment type selected");
        }

        return appointmentDAO.createAppointment(
            reg.getPatientName(), reg.getAddress(), reg.getContactNumber(),
            dentistId, treatmentId, reg.getAppointmentDate(), reg.getAppointmentTime(), createdBy
        );
    }

    public AppointmentDetail getAppointment(String number) throws SQLException {
        return appointmentDAO.findByNumber(number);
    }

    public List<AppointmentDetail> getTodayAppointments() throws SQLException {
        return appointmentDAO.findToday();
    }

    public List<AppointmentDetail> getAllAppointments(String date, String dentist) throws SQLException {
        return appointmentDAO.findAll(date, dentist);
    }

    public void updateStatus(String number, String status, UserSession user) throws SQLException {
        AppointmentDetail apt = appointmentDAO.findByNumber(number);
        if (apt == null) {
            throw new SQLException("Appointment not found");
        }
        if ("DENTIST".equals(user.getRole()) && user.getDentistName() != null
                && !user.getDentistName().equals(apt.getDentistName())) {
            throw new SecurityException("You can only update your own appointments");
        }
        appointmentDAO.updateStatus(number, status);
        appointmentSubject.notifyStatusUpdated(number, status, user.getUserId());
    }

    public BillResult calculateBill(String appointmentNumber) throws SQLException {
        return billDAO.calculateBill(appointmentNumber);
    }

    public List<StaffUser> listStaff() throws SQLException {
        return adminDAO.listStaff();
    }

    public void createStaff(String fullName, String username, String password, String role) throws SQLException {
        adminDAO.createStaff(fullName, username, password, role);
    }

    public List<Dentist> listDentists() throws SQLException {
        return adminDAO.listDentists();
    }

    public void addDentist(String name, String specialization) throws SQLException {
        adminDAO.addDentist(name, specialization);
    }

    public List<TreatmentType> listTreatments() throws SQLException {
        return adminDAO.listTreatments();
    }

    public void addTreatment(String name, double price) throws SQLException {
        adminDAO.addTreatment(name, price);
    }

    public double getConsultationFee() throws SQLException {
        return adminDAO.getConsultationFee();
    }

    public void updateConsultationFee(double fee) throws SQLException {
        adminDAO.updateConsultationFee(fee);
    }

    public void updateStaff(int userId, String fullName, String username, String password, String role) throws SQLException {
        adminDAO.updateStaff(userId, fullName, username, password, role);
    }

    public void deleteStaff(int userId) throws SQLException {
        adminDAO.deleteStaff(userId);
    }

    public Map<String, Object> dailyReport(String date) throws SQLException {
        return reportDAO.dailyReport(date);
    }

    public Map<String, Object> revenueReport(String from, String to) throws SQLException {
        return reportDAO.revenueReport(from, to);
    }
public String sendReminder(String appointmentNumber, String type) throws SQLException {
        AppointmentDetail apt = appointmentDAO.findByNumber(appointmentNumber);
        if (apt == null) {
            throw new SQLException("Appointment not found");
        }
        NotificationStrategy strategy = NotificationStrategyFactory.getStrategy(type);
        String message = strategy.send(apt);
        int apptId = notificationDAO.findAppointmentId(appointmentNumber);
        String recipient = "email".equals(strategy.getType()) ? apt.getPatientName() : apt.getContactNumber();
        notificationDAO.saveNotification(apptId, strategy.getType(), recipient, message);
        return message;
    }
}
