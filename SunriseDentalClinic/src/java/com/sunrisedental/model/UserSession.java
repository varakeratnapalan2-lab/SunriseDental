package com.sunrisedental.model;

public class UserSession {

    private int userId;
    private String username;
    private String fullName;
    private String role;
    private Integer dentistId;
    private String dentistName;

    public UserSession() {
    }

    public UserSession(int userId, String username, String fullName, String role, Integer dentistId, String dentistName) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.dentistId = dentistId;
        this.dentistName = dentistName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getDentistId() {
        return dentistId;
    }

    public void setDentistId(Integer dentistId) {
        this.dentistId = dentistId;
    }

    public String getDentistName() {
        return dentistName;
    }

    public void setDentistName(String dentistName) {
        this.dentistName = dentistName;
    }
}
