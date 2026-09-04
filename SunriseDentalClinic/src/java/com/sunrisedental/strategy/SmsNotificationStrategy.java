package com.sunrisedental.strategy;

import com.sunrisedental.model.AppointmentDetail;

public class SmsNotificationStrategy implements NotificationStrategy {

    @Override
    public String getType() {
        return "sms";
    }

    @Override
    public String send(AppointmentDetail appointment) {
        return String.format(
            "SMS to %s: Sunrise Dental reminder — Appt %s, %s %s, Dr. %s.",
            appointment.getContactNumber(),
            appointment.getAppointmentNumber(),
            appointment.getAppointmentDate(),
            appointment.getAppointmentTime(),
            appointment.getDentistName()
        );
    }
}
