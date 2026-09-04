package com.sunrisedental.strategy;

import com.sunrisedental.model.AppointmentDetail;

/**
 * Strategy Pattern — interchangeable notification delivery algorithms.
 */
public interface NotificationStrategy {
    String getType();
    String send(AppointmentDetail appointment);
}
