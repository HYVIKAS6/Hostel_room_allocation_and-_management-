package com.hostel.service;

import com.hostel.dao.DatabaseOperations;
import com.hostel.exception.DatabaseException;
import com.hostel.exception.InvalidInputException;
import com.hostel.model.Student;

public class AllocationService {
    private final DatabaseOperations db;

    public AllocationService(DatabaseOperations db) { this.db = db; }

    public void addStudent(Student s) throws DatabaseException, InvalidInputException {
        int id = db.addStudent(s);
        if (id <= 0) throw new DatabaseException("Failed to add student", null);
    }

    public void allocateRoom(int studentId, int roomId) throws DatabaseException, InvalidInputException {
        boolean ok = db.allocateRoom(studentId, roomId);
        if (!ok) throw new InvalidInputException("Room is full or unavailable");
    }
}
