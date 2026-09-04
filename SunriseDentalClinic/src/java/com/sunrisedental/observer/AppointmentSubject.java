package com.sunrisedental.observer;

import java.util.ArrayList;
import java.util.List;

public class AppointmentSubject {

    private static AppointmentSubject instance;
    private final List<AppointmentObserver> observers = new ArrayList<>();

    private AppointmentSubject() {
        registerObserver(new AuditLogObserver());
    }

    public static synchronized AppointmentSubject getInstance() {
        if (instance == null) {
            instance = new AppointmentSubject();
        }
        return instance;
    }

    public void registerObserver(AppointmentObserver observer) {
        observers.add(observer);
    }

    public void notifyStatusUpdated(String appointmentNumber, String newStatus, int userId) {
        for (AppointmentObserver observer : observers) {
            observer.onStatusUpdated(appointmentNumber, newStatus, userId);
        }
    }
}
