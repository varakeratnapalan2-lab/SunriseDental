# Sunrise Dental Clinic — Backend Setup (Pure Java, No Spring Boot)

## Tech Stack
| Layer | Technology |
|-------|------------|
| Frontend | HTML, CSS, JavaScript |
| Backend | **Pure Java** — Jakarta Servlets (REST API) |
| Server | Apache Tomcat 10.1 |
| Database | MySQL 8 |

## 8 Design Patterns Used
1. **Singleton** — `DatabaseConnection`
2. **DAO** — `UserDAO`, `AppointmentDAO`, `BillDAO`, etc.
3. **Factory** — `DAOFactory`
4. **Builder** — `AppointmentRegistrationBuilder`
5. **Strategy** — `EmailNotificationStrategy`, `SmsNotificationStrategy`
6. **Observer** — `AppointmentSubject`, `AuditLogObserver`
7. **Facade** — `ClinicFacade`
8. **MVC** — Servlets (Controller) + Model classes + HTML (View)

## Step 1 — Import Database
1. Open **MySQL Workbench** or command line
2. Run: `database/sunrise_dental_clinic.sql`
3. Verify: `SELECT username, password, role FROM users;`

## Step 2 — Configure Database Password
Edit `src/conf/db.properties`:
```properties
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

## Step 3 — JAR Files (already in WEB-INF/lib)
- `gson-2.10.1.jar`
- `mysql-connector-j-8.3.0.jar`

## Step 4 — Run in NetBeans
1. Open project **SunriseDentalClinic**
2. Right-click → **Clean and Build**
3. Right-click → **Run**
4. Browser: `http://localhost:8080/SunriseDentalClinic/`

## Login Credentials (from database)
| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | Admin |
| reception | reception123 | Receptionist |
| dentist | dentist123 | Dentist |

## REST API Endpoints
- `POST /api/auth/login` — Login (HttpSession)
- `POST /api/auth/logout` — Logout
- `GET/POST /api/appointments/*` — Appointments
- `POST /api/bills/calculate` — Calculate bill
- `GET/POST /api/admin/*` — Admin settings
- `GET /api/reports/*` — Reports
- `POST /api/notifications/send` — Email/SMS reminder
