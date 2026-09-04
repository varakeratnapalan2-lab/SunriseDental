package com.sunrisedental.builder;

/**
 * Builder Pattern — step-by-step construction of appointment registration data with validation.
 */
public class AppointmentRegistrationBuilder {

    private String patientName;
    private String address;
    private String contactNumber;
    private String dentistName;
    private String treatmentType;
    private String appointmentDate;
    private String appointmentTime;

    public AppointmentRegistrationBuilder patientName(String patientName) {
        this.patientName = patientName;
        return this;
    }

    public AppointmentRegistrationBuilder address(String address) {
        this.address = address;
        return this;
    }

    public AppointmentRegistrationBuilder contactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
        return this;
    }

    public AppointmentRegistrationBuilder dentistName(String dentistName) {
        this.dentistName = dentistName;
        return this;
    }

    public AppointmentRegistrationBuilder treatmentType(String treatmentType) {
        this.treatmentType = treatmentType;
        return this;
    }

    public AppointmentRegistrationBuilder appointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
        return this;
    }

    public AppointmentRegistrationBuilder appointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
        return this;
    }

    public AppointmentRegistration build() {
        validate();
        AppointmentRegistration reg = new AppointmentRegistration();
        reg.setPatientName(patientName.trim());
        reg.setAddress(address.trim());
        reg.setContactNumber(contactNumber.replaceAll("\\s", ""));
        reg.setDentistName(dentistName);
        reg.setTreatmentType(treatmentType);
        reg.setAppointmentDate(appointmentDate);
        reg.setAppointmentTime(normalizeTime(appointmentTime));
        return reg;
    }

    private void validate() {
        if (patientName == null || patientName.trim().length() < 2) {
            throw new IllegalArgumentException("Patient name required (min 2 characters)");
        }
        if (address == null || address.trim().length() < 5) {
            throw new IllegalArgumentException("Address required (min 5 characters)");
        }
        if (contactNumber == null || !contactNumber.replaceAll("\\s", "").matches("^07\\d{8}$")) {
            throw new IllegalArgumentException("Valid Sri Lankan mobile required (07XXXXXXXX)");
        }
        if (dentistName == null || dentistName.isBlank()) {
            throw new IllegalArgumentException("Dentist is required");
        }
        if (treatmentType == null || treatmentType.isBlank()) {
            throw new IllegalArgumentException("Treatment type is required");
        }
        if (appointmentDate == null || appointmentDate.isBlank()) {
            throw new IllegalArgumentException("Appointment date is required");
        }
        if (appointmentTime == null || appointmentTime.isBlank()) {
            throw new IllegalArgumentException("Appointment time is required");
        }
    }

    private String normalizeTime(String time) {
        return time.length() == 5 ? time + ":00" : time;
    }

    public static class AppointmentRegistration {
        private String patientName;
        private String address;
        private String contactNumber;
        private String dentistName;
        private String treatmentType;
        private String appointmentDate;
        private String appointmentTime;

        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getContactNumber() { return contactNumber; }
        public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
        public String getDentistName() { return dentistName; }
        public void setDentistName(String dentistName) { this.dentistName = dentistName; }
        public String getTreatmentType() { return treatmentType; }
        public void setTreatmentType(String treatmentType) { this.treatmentType = treatmentType; }
        public String getAppointmentDate() { return appointmentDate; }
        public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }
        public String getAppointmentTime() { return appointmentTime; }
        public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }
    }
}
