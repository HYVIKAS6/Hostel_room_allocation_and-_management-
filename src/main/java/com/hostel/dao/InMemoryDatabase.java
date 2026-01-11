package com.hostel.dao;

import com.hostel.exception.DatabaseException;
import com.hostel.exception.InvalidInputException;
import com.hostel.model.Room;
import com.hostel.model.Student;

import java.util.*;

public class InMemoryDatabase implements DatabaseOperations {
    private final Map<Integer, Student> students = new HashMap<>();
    private final Map<Integer, Room> rooms = new HashMap<>();
    private final Map<Integer, Integer> allocations = new HashMap<>(); // studentId -> roomId
    private int roomIdSeq = 1;

    public InMemoryDatabase() {
        // seed 4 rooms with capacity 2 each
        addRoom(new Room(0, "R1", 2, 0));
        addRoom(new Room(0, "R2", 2, 0));
        addRoom(new Room(0, "R3", 2, 0));
        addRoom(new Room(0, "R4", 2, 0));
    }

    private void addRoom(Room r) {
        int id = roomIdSeq++;
        Room room = new Room(id, r.getNumber(), r.getCapacity(), r.getOccupied());
        rooms.put(id, room);
    }

    @Override
    public int addRoom(Room r) throws DatabaseException, InvalidInputException {
        if (r.getNumber() == null || r.getNumber().isBlank()) throw new InvalidInputException("Room label required");
        int id = roomIdSeq++;
        Room room = new Room(id, r.getNumber(), r.getCapacity(), r.getOccupied());
        rooms.put(id, room);
        return id;
    }

    @Override
    public void connect() throws DatabaseException {
        // in-memory has no external connection
    }

    @Override
    public int addStudent(Student s) throws DatabaseException, InvalidInputException {
        if (s.getName() == null || s.getName().isBlank()) throw new InvalidInputException("Name required");
        if (s.getRollNo() == null || s.getRollNo().isBlank()) throw new InvalidInputException("Roll number required");
        int id;
        try {
            id = Integer.parseInt(s.getRollNo());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Roll number must be numeric and will be used as student ID");
        }
        if (students.containsKey(id)) throw new InvalidInputException("Student with this roll number already exists");
        s.setId(id);
        students.put(id, s);
        return id;
    }

    @Override
    public boolean allocateRoom(int studentId, int roomId) throws DatabaseException, InvalidInputException {
        Student st = students.get(studentId);
        if (st == null) throw new InvalidInputException("Student not found");
        Room r = rooms.get(roomId);
        if (r == null) throw new InvalidInputException("Room not found");
        if (allocations.containsKey(studentId)) throw new InvalidInputException("Student already allocated to a room");
        if (r.getOccupied() >= r.getCapacity()) return false;
        Room updated = new Room(r.getId(), r.getNumber(), r.getCapacity(), r.getOccupied() + 1);
        rooms.put(r.getId(), updated);
        allocations.put(studentId, roomId);
        return true;
    }

    @Override
    public java.util.List<Room> getAvailableRooms() throws DatabaseException {
        List<Room> list = new ArrayList<>();
        for (Room r : rooms.values()) {
            if (r.getOccupied() < r.getCapacity()) list.add(r);
        }
        return list;
    }

    @Override
    public boolean deleteStudent(int studentId) throws DatabaseException {
        if (!students.containsKey(studentId)) return false;
        // deallocate if allocated
        deallocateStudent(studentId);
        students.remove(studentId);
        return true;
    }

    @Override
    public boolean deallocateStudent(int studentId) throws DatabaseException {
        Integer rid = allocations.remove(studentId);
        if (rid == null) return false;
        Room r = rooms.get(rid);
        if (r != null) {
            Room updated = new Room(r.getId(), r.getNumber(), r.getCapacity(), Math.max(0, r.getOccupied() - 1));
            rooms.put(r.getId(), updated);
        }
        return true;
    }

    @Override
    public boolean changeRoomNumber(int roomId, String newNumber) throws DatabaseException, InvalidInputException {
        Room r = rooms.get(roomId);
        if (r == null) throw new InvalidInputException("Room not found");
        Room updated = new Room(r.getId(), newNumber, r.getCapacity(), r.getOccupied());
        rooms.put(r.getId(), updated);
        return true;
    }

    @Override
    public List<Room> getAllRooms() throws DatabaseException {
        return new ArrayList<>(rooms.values());
    }

    @Override
    public Student getStudent(int studentId) throws DatabaseException {
        return students.get(studentId);
    }

    @Override
    public Room getAllocatedRoomForStudent(int studentId) throws DatabaseException {
        Integer rid = allocations.get(studentId);
        if (rid == null) return null;
        return rooms.get(rid);
    }

    @Override
    public java.util.List<Student> getAllStudents() throws DatabaseException {
        return new ArrayList<>(students.values());
    }

    @Override
    public java.util.Map<Integer,Integer> getAllAllocations() throws DatabaseException {
        return new HashMap<>(allocations);
    }
}
