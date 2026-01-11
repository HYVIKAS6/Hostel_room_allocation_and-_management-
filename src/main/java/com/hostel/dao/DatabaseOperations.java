package com.hostel.dao;

import com.hostel.exception.DatabaseException;
import com.hostel.exception.InvalidInputException;
import com.hostel.model.Room;
import com.hostel.model.Student;

import java.util.List;

public interface DatabaseOperations {
    void connect() throws DatabaseException;
    int addStudent(Student s) throws DatabaseException, InvalidInputException;
    boolean allocateRoom(int studentId, int roomId) throws DatabaseException, InvalidInputException;
    List<Room> getAvailableRooms() throws DatabaseException;
    boolean deleteStudent(int studentId) throws DatabaseException;
    boolean deallocateStudent(int studentId) throws DatabaseException;
    boolean changeRoomNumber(int roomId, String newNumber) throws DatabaseException, InvalidInputException;
    List<Room> getAllRooms() throws DatabaseException;
    Student getStudent(int studentId) throws DatabaseException;
    Room getAllocatedRoomForStudent(int studentId) throws DatabaseException;
    java.util.List<Student> getAllStudents() throws DatabaseException;
    int addRoom(Room r) throws DatabaseException, InvalidInputException;
    java.util.Map<Integer,Integer> getAllAllocations() throws DatabaseException;
}
