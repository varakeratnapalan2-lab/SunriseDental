package com.sunrisedental.observer;

/**
 * Observer Pattern — listeners react to appointment lifecycle events.
 */
public interface AppointmentObserver {
    void onStatusUpdated(String appointmentNumber, String newStatus, int userId);
}
