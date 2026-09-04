package com.sunrisedental.model;

/**
 * Implementation of the Builder Design Pattern for StaffUser.
 * This pattern is used to construct complex StaffUser objects step-by-step
 * and provides a flexible solution to object creation.
 */
public class StaffUserBuilder {

    private StaffUser staffUser;

    public StaffUserBuilder() {
        this.staffUser = new StaffUser();
    }

    public StaffUserBuilder setId(int id) {
        this.staffUser.setId(id);
        return this;
    }

    public StaffUserBuilder setUsername(String username) {
        this.staffUser.setUsername(username);
        return this;
    }

    public StaffUserBuilder setFullName(String fullName) {
        this.staffUser.setFullName(fullName);
        return this;
    }

    public StaffUserBuilder setRole(String role) {
        this.staffUser.setRole(role);
        return this;
    }

    public StaffUserBuilder setStatus(String status) {
        this.staffUser.setStatus(status);
        return this;
    }

    public StaffUser build() {
        return this.staffUser;
    }
}
