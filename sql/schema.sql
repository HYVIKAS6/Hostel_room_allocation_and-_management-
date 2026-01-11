-- Schema for Hostel Room Allocation
CREATE DATABASE IF NOT EXISTS hostel_db;
USE hostel_db;

CREATE TABLE IF NOT EXISTS students (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  roll_no VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS rooms (
  id INT AUTO_INCREMENT PRIMARY KEY,
  number VARCHAR(20) NOT NULL,
  capacity INT NOT NULL DEFAULT 1,
  occupied INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS allocations (
  id INT AUTO_INCREMENT PRIMARY KEY,
  student_id INT NOT NULL,
  room_id INT NOT NULL,
  allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (student_id) REFERENCES students(id),
  FOREIGN KEY (room_id) REFERENCES rooms(id)
);

INSERT INTO rooms (number,capacity,occupied) VALUES ('A101',2,0),('A102',2,0),('B201',1,0);
