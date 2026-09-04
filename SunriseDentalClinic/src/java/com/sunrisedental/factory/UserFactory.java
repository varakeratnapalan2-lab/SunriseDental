package com.sunrisedental.factory;

import com.sunrisedental.model.UserSession;
import com.sunrisedental.model.StaffUser;

/**
 * Implementation of the Factory Design Pattern for User creation.
 * This pattern centralizes the creation logic for different types of user objects.
 */
public class UserFactory {

    /**
     * Factory method to create a UserSession object based on database records.
     */
    public static UserSession createUserSession(int userId, String username, String role, String fullName, String dentistName) {
        UserSession session = new UserSession();
        session.setUserId(userId);
        session.setUsername(username);
        session.setRole(role);
        session.setFullName(fullName);
        session.setDentistName(dentistName);
        return session;
    }

    /**
     * Factory method to create an empty StaffUser using the Builder Pattern.
     */
    public static StaffUser createEmptyStaffUser() {
        return new StaffUser(); // Can be expanded for different default staff types
    }
}
