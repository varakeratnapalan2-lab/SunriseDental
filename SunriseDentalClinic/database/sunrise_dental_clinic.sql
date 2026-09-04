-- =============================================================================
-- Sunrise Dental Clinic — MySQL Database (Assignment Scenario + Task B)
-- File: sunrise_dental_clinic.sql
-- Passwords stored as PLAIN TEXT (visible in DB for assignment demo)
-- =============================================================================
-- Staff logins (type manually on login page — no auto-fill):
--   Username: admin      Password: admin123      Role: ADMIN
--   Username: reception  Password: reception123  Role: RECEPTIONIST
--   Username: dentist    Password: dentist123    Role: DENTIST
-- =============================================================================

DROP DATABASE IF EXISTS sunrise_dental_clinic;
CREATE DATABASE sunrise_dental_clinic
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE sunrise_dental_clinic;

-- -----------------------------------------------------------------------------
-- TABLES (Scenario fields + Task B advanced DB support)
-- -----------------------------------------------------------------------------

-- Staff authentication (Scenario #1) — Admin creates accounts later via system
CREATE TABLE users (
  user_id      INT AUTO_INCREMENT PRIMARY KEY,
  username     VARCHAR(50)  NOT NULL UNIQUE,
  password     VARCHAR(100) NOT NULL COMMENT 'Plain text password for assignment',
  full_name    VARCHAR(100) NOT NULL,
  role         ENUM('ADMIN', 'RECEPTIONIST', 'DENTIST') NOT NULL,
  status       ENUM('active', 'inactive') NOT NULL DEFAULT 'active',
  dentist_id   INT NULL,
  created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Dentist master (Admin settings + appointment dropdown)
CREATE TABLE dentists (
  dentist_id     INT AUTO_INCREMENT PRIMARY KEY,
  name           VARCHAR(100) NOT NULL,
  specialization VARCHAR(100) NOT NULL DEFAULT 'General Dentistry',
  status         ENUM('active', 'inactive') NOT NULL DEFAULT 'active'
) ENGINE=InnoDB;

-- Patient details (Scenario #2 fields)
CREATE TABLE patients (
  patient_id      INT AUTO_INCREMENT PRIMARY KEY,
  name            VARCHAR(100) NOT NULL,
  address         VARCHAR(255) NOT NULL,
  contact_number  VARCHAR(15)  NOT NULL,
  registered_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_contact (contact_number),
  INDEX idx_name (name)
) ENGINE=InnoDB;

-- Treatment types + prices (Scenario #4 billing)
CREATE TABLE treatment_types (
  treatment_id INT AUTO_INCREMENT PRIMARY KEY,
  name         VARCHAR(100) NOT NULL UNIQUE,
  price        DECIMAL(10,2) NOT NULL,
  status       ENUM('active', 'inactive') NOT NULL DEFAULT 'active'
) ENGINE=InnoDB;

-- System settings (consultation fee)
CREATE TABLE settings (
  setting_key   VARCHAR(50) PRIMARY KEY,
  setting_value VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

-- Appointments (Scenario #2 + #3)
CREATE TABLE appointments (
  appointment_id     INT AUTO_INCREMENT PRIMARY KEY,
  appointment_number VARCHAR(20) NOT NULL UNIQUE,
  patient_id         INT NOT NULL,
  dentist_id         INT NOT NULL,
  treatment_id       INT NOT NULL,
  appointment_date   DATE NOT NULL,
  appointment_time   TIME NOT NULL,
  status             ENUM('Scheduled', 'In Progress', 'Completed', 'Cancelled')
                     NOT NULL DEFAULT 'Scheduled',
  created_by         INT NULL,
  created_date       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_appt_patient   FOREIGN KEY (patient_id)   REFERENCES patients(patient_id),
  CONSTRAINT fk_appt_dentist   FOREIGN KEY (dentist_id)   REFERENCES dentists(dentist_id),
  CONSTRAINT fk_appt_treatment FOREIGN KEY (treatment_id) REFERENCES treatment_types(treatment_id),
  CONSTRAINT fk_appt_user      FOREIGN KEY (created_by)   REFERENCES users(user_id),
  INDEX idx_date (appointment_date),
  INDEX idx_slot (dentist_id, appointment_date, appointment_time)
) ENGINE=InnoDB;

ALTER TABLE users
  ADD CONSTRAINT fk_user_dentist
  FOREIGN KEY (dentist_id) REFERENCES dentists(dentist_id)
  ON DELETE SET NULL;

-- Bills (Scenario #4 — treatment cost + consultation fee)
CREATE TABLE bills (
  bill_id          INT AUTO_INCREMENT PRIMARY KEY,
  appointment_id   INT NOT NULL UNIQUE,
  treatment_cost   DECIMAL(10,2) NOT NULL,
  consultation_fee DECIMAL(10,2) NOT NULL,
  total_amount     DECIMAL(10,2) NOT NULL,
  bill_date        DATE NOT NULL,
  status           ENUM('Unpaid', 'Paid') NOT NULL DEFAULT 'Unpaid',
  CONSTRAINT fk_bill_appt FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id)
) ENGINE=InnoDB;

-- Audit log (Task B — trigger support)
CREATE TABLE audit_log (
  log_id     INT AUTO_INCREMENT PRIMARY KEY,
  user_id    INT NULL,
  action     VARCHAR(255) NOT NULL,
  details    TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB;

-- Email/SMS reminders (Task B — complex functionality)
CREATE TABLE notifications (
  notification_id INT AUTO_INCREMENT PRIMARY KEY,
  appointment_id  INT NOT NULL,
  notify_type     ENUM('sms', 'email') NOT NULL,
  recipient       VARCHAR(100) NOT NULL,
  message         TEXT NOT NULL,
  status          ENUM('Pending', 'Sent', 'Failed') NOT NULL DEFAULT 'Pending',
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notify_appt FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- FUNCTIONS (Task B advanced DB)
-- -----------------------------------------------------------------------------

DELIMITER $$

CREATE FUNCTION fn_generate_appointment_no()
RETURNS VARCHAR(20)
DETERMINISTIC
READS SQL DATA
BEGIN
  DECLARE next_num INT;
  SELECT IFNULL(MAX(CAST(SUBSTRING(appointment_number, 4) AS UNSIGNED)), 0) + 1
    INTO next_num FROM appointments;
  RETURN CONCAT('APT', LPAD(next_num, 4, '0'));
END$$

CREATE FUNCTION fn_check_slot_available(
  p_dentist_id INT,
  p_date DATE,
  p_time TIME
)
RETURNS TINYINT
DETERMINISTIC
READS SQL DATA
BEGIN
  DECLARE slot_count INT;
  SELECT COUNT(*) INTO slot_count
  FROM appointments
  WHERE dentist_id = p_dentist_id
    AND appointment_date = p_date
    AND appointment_time = p_time
    AND status <> 'Cancelled';
  IF slot_count = 0 THEN
    RETURN 1;
  ELSE
    RETURN 0;
  END IF;
END$$

-- -----------------------------------------------------------------------------
-- STORED PROCEDURES
-- -----------------------------------------------------------------------------

CREATE PROCEDURE sp_login_user(
  IN p_username VARCHAR(50),
  IN p_password VARCHAR(100)
)
BEGIN
  SELECT user_id, username, full_name, role, status, dentist_id
  FROM users
  WHERE username = p_username
    AND password = p_password
    AND status = 'active'
  LIMIT 1;
END$$

CREATE PROCEDURE sp_create_appointment(
  IN  p_patient_name     VARCHAR(100),
  IN  p_address          VARCHAR(255),
  IN  p_contact_number   VARCHAR(15),
  IN  p_dentist_id       INT,
  IN  p_treatment_id     INT,
  IN  p_appointment_date DATE,
  IN  p_appointment_time TIME,
  IN  p_created_by       INT,
  OUT p_appointment_no   VARCHAR(20),
  OUT p_message          VARCHAR(255)
)
BEGIN
  DECLARE v_patient_id INT;
  DECLARE v_slot_ok TINYINT;
  DECLARE v_appt_no VARCHAR(20);

  IF p_appointment_date < CURDATE() THEN
    SET p_appointment_no = NULL;
    SET p_message = 'Appointment date cannot be in the past';
  ELSEIF p_appointment_time < '08:00:00' OR p_appointment_time > '18:00:00' THEN
    SET p_appointment_no = NULL;
    SET p_message = 'Appointment time must be between 08:00 and 18:00';
  ELSEIF p_contact_number NOT REGEXP '^07[0-9]{8}$' THEN
    SET p_appointment_no = NULL;
    SET p_message = 'Contact must be valid Sri Lankan mobile (07XXXXXXXX)';
  ELSE
    SET v_slot_ok = fn_check_slot_available(p_dentist_id, p_appointment_date, p_appointment_time);
    IF v_slot_ok = 0 THEN
      SET p_appointment_no = NULL;
      SET p_message = 'This time slot is already booked for the selected dentist';
    ELSE
      SELECT patient_id INTO v_patient_id
      FROM patients WHERE contact_number = p_contact_number LIMIT 1;

      IF v_patient_id IS NULL THEN
        INSERT INTO patients (name, address, contact_number)
        VALUES (p_patient_name, p_address, p_contact_number);
        SET v_patient_id = LAST_INSERT_ID();
      ELSE
        UPDATE patients
        SET name = p_patient_name, address = p_address
        WHERE patient_id = v_patient_id;
      END IF;

      SET v_appt_no = fn_generate_appointment_no();

      INSERT INTO appointments (
        appointment_number, patient_id, dentist_id, treatment_id,
        appointment_date, appointment_time, status, created_by
      ) VALUES (
        v_appt_no, v_patient_id, p_dentist_id, p_treatment_id,
        p_appointment_date, p_appointment_time, 'Scheduled', p_created_by
      );

      SET p_appointment_no = v_appt_no;
      SET p_message = 'Appointment registered successfully';
    END IF;
  END IF;
END$$

CREATE PROCEDURE sp_calculate_bill(
  IN  p_appointment_number VARCHAR(20),
  OUT p_bill_id            INT,
  OUT p_treatment_cost     DECIMAL(10,2),
  OUT p_consultation_fee   DECIMAL(10,2),
  OUT p_total_amount       DECIMAL(10,2),
  OUT p_message            VARCHAR(255)
)
BEGIN
  DECLARE v_appointment_id INT;
  DECLARE v_treatment_id INT;
  DECLARE v_fee DECIMAL(10,2);
  DECLARE v_price DECIMAL(10,2);
  DECLARE v_existing_bill INT;

  SELECT a.appointment_id, a.treatment_id
    INTO v_appointment_id, v_treatment_id
  FROM appointments a
  WHERE a.appointment_number = p_appointment_number
  LIMIT 1;

  IF v_appointment_id IS NULL THEN
    SET p_bill_id = NULL;
    SET p_treatment_cost = NULL;
    SET p_consultation_fee = NULL;
    SET p_total_amount = NULL;
    SET p_message = 'Appointment not found';
  ELSE
    SELECT CAST(setting_value AS DECIMAL(10,2)) INTO v_fee
    FROM settings WHERE setting_key = 'consultation_fee';
    IF v_fee IS NULL THEN SET v_fee = 1500.00; END IF;

    SELECT price INTO v_price FROM treatment_types WHERE treatment_id = v_treatment_id;

    SET p_treatment_cost = v_price;
    SET p_consultation_fee = v_fee;
    SET p_total_amount = v_price + v_fee;

    SELECT bill_id INTO v_existing_bill FROM bills WHERE appointment_id = v_appointment_id LIMIT 1;

    IF v_existing_bill IS NULL THEN
      INSERT INTO bills (appointment_id, treatment_cost, consultation_fee, total_amount, bill_date, status)
      VALUES (v_appointment_id, p_treatment_cost, p_consultation_fee, p_total_amount, CURDATE(), 'Unpaid');
      SET p_bill_id = LAST_INSERT_ID();
    ELSE
      UPDATE bills
      SET treatment_cost = p_treatment_cost,
          consultation_fee = p_consultation_fee,
          total_amount = p_total_amount,
          bill_date = CURDATE()
      WHERE bill_id = v_existing_bill;
      SET p_bill_id = v_existing_bill;
    END IF;

    SET p_message = 'Bill calculated successfully';
  END IF;
END$$

DELIMITER ;

-- -----------------------------------------------------------------------------
-- TRIGGERS (Task B)
-- -----------------------------------------------------------------------------

DELIMITER $$

CREATE TRIGGER trg_after_appointment_insert
AFTER INSERT ON appointments
FOR EACH ROW
BEGIN
  INSERT INTO audit_log (user_id, action, details)
  VALUES (
    NEW.created_by,
    'APPOINTMENT_CREATED',
    CONCAT('Appointment ', NEW.appointment_number, ' created')
  );
END$$

CREATE TRIGGER trg_before_appointment_delete
BEFORE DELETE ON appointments
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Hard delete not allowed. Set status to Cancelled instead.';
END$$

DELIMITER ;

-- -----------------------------------------------------------------------------
-- VIEWS (Reports — Task B decision-making)
-- -----------------------------------------------------------------------------

CREATE OR REPLACE VIEW vw_daily_appointments AS
SELECT
  a.appointment_number,
  a.appointment_date,
  a.appointment_time,
  a.status,
  p.name AS patient_name,
  p.contact_number,
  p.address,
  d.name AS dentist_name,
  t.name AS treatment_type,
  t.price AS treatment_price
FROM appointments a
JOIN patients p ON p.patient_id = a.patient_id
JOIN dentists d ON d.dentist_id = a.dentist_id
JOIN treatment_types t ON t.treatment_id = a.treatment_id
WHERE a.appointment_date = CURDATE()
  AND a.status <> 'Cancelled'
ORDER BY a.appointment_time;

CREATE OR REPLACE VIEW vw_revenue_summary AS
SELECT
  DATE(b.bill_date) AS bill_day,
  t.name AS treatment_type,
  COUNT(b.bill_id) AS bill_count,
  SUM(b.total_amount) AS total_revenue
FROM bills b
JOIN appointments a ON a.appointment_id = b.appointment_id
JOIN treatment_types t ON t.treatment_id = a.treatment_id
GROUP BY DATE(b.bill_date), t.name
ORDER BY bill_day DESC;

CREATE OR REPLACE VIEW vw_appointment_details AS
SELECT
  a.appointment_number,
  a.appointment_date,
  a.appointment_time,
  a.status,
  p.name AS patient_name,
  p.address,
  p.contact_number,
  d.dentist_id,
  d.name AS dentist_name,
  t.treatment_id,
  t.name AS treatment_type,
  t.price AS treatment_price
FROM appointments a
JOIN patients p ON p.patient_id = a.patient_id
JOIN dentists d ON d.dentist_id = a.dentist_id
JOIN treatment_types t ON t.treatment_id = a.treatment_id;

-- -----------------------------------------------------------------------------
-- SEED DATA (plain text passwords — visible in users table)
-- -----------------------------------------------------------------------------

INSERT INTO settings (setting_key, setting_value) VALUES
('consultation_fee', '1500.00'),
('clinic_name', 'Sunrise Dental Clinic'),
('clinic_city', 'Colombo');

INSERT INTO dentists (name, specialization, status) VALUES
('Dr. Nimal Silva', 'General Dentistry', 'active'),
('Dr. Anjali Fernando', 'Orthodontics', 'active'),
('Dr. Ruwan Perera', 'Oral Surgery', 'active');

INSERT INTO treatment_types (name, price, status) VALUES
('Dental Cleaning', 4500.00, 'active'),
('Tooth Filling', 6000.00, 'active'),
('Root Canal', 25000.00, 'active'),
('Tooth Extraction', 8000.00, 'active'),
('Dental Crown', 35000.00, 'active');

-- 3 staff users — PLAIN passwords (visible in DB)
INSERT INTO users (username, password, full_name, role, status, dentist_id) VALUES
('admin',     'admin123',     'Clinic Administrator', 'ADMIN',        'active', NULL),
('reception', 'reception123', 'Sarah Jayawardena',    'RECEPTIONIST', 'active', NULL),
('dentist',   'dentist123',   'Dr. Nimal Silva',      'DENTIST',      'active', 1);

-- Optional sample appointment for testing search/bill
INSERT INTO patients (name, address, contact_number) VALUES
('Kamal Perera', '45 Galle Road, Colombo 03', '0771234567');

INSERT INTO appointments (
  appointment_number, patient_id, dentist_id, treatment_id,
  appointment_date, appointment_time, status, created_by
) VALUES (
  'APT0001', 1, 1, 1, CURDATE(), '10:00:00', 'Scheduled', 2
);

-- =============================================================================
-- VERIFY:
--   SELECT username, password, full_name, role FROM users;
--   CALL sp_login_user('admin', 'admin123');
-- =============================================================================
