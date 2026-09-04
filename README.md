# SunriseDental

## Sunrise Dental Clinic Appointment and Patient Management System

SunriseDental is a web-based Dental Clinic Appointment and Patient Management System developed to support the daily operations of a private dental clinic in Colombo.

The system replaces manual appointment and patient record management with a centralized digital solution for managing appointments, patient information, billing, staff access and clinic reports.

## Project Overview

The system provides role-based access for three staff roles:

- Admin
- Receptionist
- Dentist

Each role is provided with appropriate functions based on their responsibilities.

## Main Features

### Authentication and Security
- Staff login
- Session-based authentication
- Role-based access control
- Protected system functions
- Secure logout

### Appointment Management
- Register new appointments
- Automatically generate appointment numbers
- Search appointments
- View today's appointments
- Dentist availability checking
- Double-booking prevention
- Appointment status tracking

### Billing
- Automatic treatment price retrieval
- Consultation fee calculation
- Automatic total bill calculation
- Print-friendly patient receipt

### Appointment Reminders
- Email appointment reminders
- SMS appointment reminders

### Administration
- Staff user management
- Dentist management
- Treatment type and price management
- Consultation fee configuration

### Reports
- Daily appointment reports
- Dentist-based appointment information
- Revenue reports
- Treatment-based revenue analysis

## Technology Stack

- HTML
- CSS
- JavaScript
- Java
- Jakarta Servlets
- RESTful Web Services
- MySQL
- JDBC
- Apache Tomcat
- Apache NetBeans
- Git
- GitHub

## System Architecture

The system follows a three-tier architecture:

1. **Presentation Tier**  
   HTML, CSS and JavaScript provide the user interface.

2. **Business Logic Tier**  
   Java RESTful web services and application components implement authentication, business rules, appointment management, billing and reporting.

3. **Data Tier**  
   MySQL stores users, patients, dentists, treatments, appointments, bills and related system data.

## Database

The system uses MySQL as the relational database management system.

The database includes tables for:

- Users
- Patients
- Dentists
- Treatment Types
- Appointments
- Bills
- Settings
- Audit Logs
- Notifications

Stored procedures, functions, triggers and views are used to support important business rules and reporting requirements.

## Design Patterns

The Java backend applies the following design patterns:

- MVC
- DAO
- Singleton
- Simple Factory
- Strategy
- Observer
- Facade
- Builder

These patterns improve separation of concerns, code organisation, maintainability and reuse.

## Version Control

Git is used for local version control and GitHub is used as the public remote repository.

Development changes are recorded using meaningful commits and project versions. GitHub Actions is used to support automated workflow validation of the project.

## Project Structure

```text
SunriseDental
│
├── database
│   └── sunrise_dental_clinic.sql
│
├── SunriseDentalClinic
│   ├── src
│   ├── web
│   ├── nbproject
│   ├── lib
│   └── build.xml
│
└── README.md
