package com.sunrisedental.strategy;

import com.sunrisedental.model.AppointmentDetail;

public class EmailNotificationStrategy implements NotificationStrategy {

    @Override
    public String getType() {
        return "email";
    }

    @Override
    public String send(AppointmentDetail appointment) {
        return String.format(
            "EMAIL to patient %s: Reminder for appointment %s on %s at %s with %s.",
            appointment.getPatientName(),
            appointment.getAppointmentNumber(),
            appointment.getAppointmentDate(),
            appointment.getAppointmentTime(),
            appointment.getDentistName()
        );
    }
}
