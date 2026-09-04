package com.sunrisedental.factory;

import com.sunrisedental.dao.AdminDAO;
import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.dao.BillDAO;
import com.sunrisedental.dao.NotificationDAO;
import com.sunrisedental.dao.ReportDAO;
import com.sunrisedental.dao.UserDAO;

/**
 * Factory Pattern — central creation point for all DAO instances.
 */
public final class DAOFactory {

    private static DAOFactory instance;

    private final UserDAO userDAO = new UserDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final BillDAO billDAO = new BillDAO();
    private final AdminDAO adminDAO = new AdminDAO();
    private final ReportDAO reportDAO = new ReportDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private DAOFactory() {
    }

    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public AppointmentDAO getAppointmentDAO() {
        return appointmentDAO;
    }

    public BillDAO getBillDAO() {
        return billDAO;
    }

    public AdminDAO getAdminDAO() {
        return adminDAO;
    }

    public ReportDAO getReportDAO() {
        return reportDAO;
    }

    public NotificationDAO getNotificationDAO() {
        return notificationDAO;
    }
}
