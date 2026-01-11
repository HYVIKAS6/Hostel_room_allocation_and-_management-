package com.hostel.model;

public class Student extends User {
    private String rollNo;

    public Student() {}

    public Student(String name, String email, String rollNo) {
        super(name, email);
        this.rollNo = rollNo;
    }

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
}
