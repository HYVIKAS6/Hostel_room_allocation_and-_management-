package com.hostel.dao;

import com.hostel.exception.DatabaseException;
import com.hostel.exception.InvalidInputException;
import com.hostel.model.Room;
import com.hostel.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLDatabase implements DatabaseOperations {
    private Connection conn;

    // Update these or use environment variables
    private final String URL = "jdbc:mysql://localhost:3306/hostel_db";
    private final String USER = "root";
    private final String PASS = "password";

    @Override
    public void connect() throws DatabaseException {
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            throw new DatabaseException("Unable to connect: " + e.getMessage(), e);
        }
    }

    @Override
    public int addStudent(Student s) throws DatabaseException, InvalidInputException {
        if (s.getName() == null || s.getName().isBlank()) throw new InvalidInputException("Name required");
        String sql = "INSERT INTO students (name,email,roll_no) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getEmail());
            ps.setString(3, s.getRollNo());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            return -1;
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public boolean allocateRoom(int studentId, int roomId) throws DatabaseException, InvalidInputException {
        String check = "SELECT capacity,occupied FROM rooms WHERE id = ?";
        String upd = "UPDATE rooms SET occupied = occupied + 1 WHERE id = ?";
        String ins = "INSERT INTO allocations (student_id,room_id) VALUES (?,?)";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new InvalidInputException("Room not found");
            int cap = rs.getInt("capacity");
            int occ = rs.getInt("occupied");
            if (occ >= cap) return false;
            try (PreparedStatement p2 = conn.prepareStatement(upd)) {
                p2.setInt(1, roomId);
                p2.executeUpdate();
            }
            try (PreparedStatement p3 = conn.prepareStatement(ins)) {
                p3.setInt(1, studentId);
                p3.setInt(2, roomId);
                p3.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteStudent(int studentId) throws DatabaseException {
        String delAlloc = "DELETE FROM allocations WHERE student_id = ?";
        String delStu = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement p = conn.prepareStatement(delAlloc)) {
            p.setInt(1, studentId);
            p.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
        try (PreparedStatement p2 = conn.prepareStatement(delStu)) {
            p2.setInt(1, studentId);
            int affected = p2.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public boolean deallocateStudent(int studentId) throws DatabaseException {
        String sel = "SELECT room_id FROM allocations WHERE student_id = ?";
        String upd = "UPDATE rooms SET occupied = occupied - 1 WHERE id = ? AND occupied > 0";
        String del = "DELETE FROM allocations WHERE student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sel)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int rid = rs.getInt(1);
                try (PreparedStatement p2 = conn.prepareStatement(upd)) {
                    p2.setInt(1, rid);
                    p2.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
        try (PreparedStatement p3 = conn.prepareStatement(del)) {
            p3.setInt(1, studentId);
            p3.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public boolean changeRoomNumber(int roomId, String newNumber) throws DatabaseException, InvalidInputException {
        String upd = "UPDATE rooms SET number = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(upd)) {
            ps.setString(1, newNumber);
            ps.setInt(2, roomId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public List<Room> getAllRooms() throws DatabaseException {
        String sql = "SELECT id,number,capacity,occupied FROM rooms";
        List<Room> list = new ArrayList<>();
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                list.add(new Room(rs.getInt("id"), rs.getString("number"), rs.getInt("capacity"), rs.getInt("occupied")));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public List<Room> getAvailableRooms() throws DatabaseException {
        String sql = "SELECT id,number,capacity,occupied FROM rooms WHERE occupied < capacity";
        List<Room> list = new ArrayList<>();
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                list.add(new Room(rs.getInt("id"), rs.getString("number"), rs.getInt("capacity"), rs.getInt("occupied")));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public Student getStudent(int studentId) throws DatabaseException {
        String sql = "SELECT id,name,email,roll_no FROM students WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Student s = new Student(rs.getString("name"), rs.getString("email"), rs.getString("roll_no"));
                s.setId(rs.getInt("id"));
                return s;
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public Room getAllocatedRoomForStudent(int studentId) throws DatabaseException {
        String sql = "SELECT r.id,r.number,r.capacity,r.occupied FROM rooms r JOIN allocations a ON r.id = a.room_id WHERE a.student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Room(rs.getInt("id"), rs.getString("number"), rs.getInt("capacity"), rs.getInt("occupied"));
            return null;
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public java.util.List<Student> getAllStudents() throws DatabaseException {
        String sql = "SELECT id,name,email,roll_no FROM students";
        java.util.List<Student> list = new ArrayList<>();
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Student s = new Student(rs.getString("name"), rs.getString("email"), rs.getString("roll_no"));
                s.setId(rs.getInt("id"));
                list.add(s);
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public int addRoom(Room r) throws DatabaseException, InvalidInputException {
        if (r.getNumber() == null || r.getNumber().isBlank()) throw new InvalidInputException("Room label required");
        String sql = "INSERT INTO rooms (number,capacity,occupied) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getNumber());
            ps.setInt(2, r.getCapacity());
            ps.setInt(3, r.getOccupied());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            return -1;
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public java.util.Map<Integer,Integer> getAllAllocations() throws DatabaseException {
        String sql = "SELECT student_id, room_id FROM allocations";
        java.util.Map<Integer,Integer> m = new java.util.HashMap<>();
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                m.put(rs.getInt("student_id"), rs.getInt("room_id"));
            }
            return m;
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }
}
