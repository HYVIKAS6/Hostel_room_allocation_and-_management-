package com.hostel;

import com.hostel.dao.DatabaseOperations;
import com.hostel.dao.InMemoryDatabase;
import com.hostel.dao.MySQLDatabase;
import com.hostel.exception.DatabaseException;
import com.hostel.exception.InvalidInputException;
import com.hostel.model.Student;
import com.hostel.model.Room;
import com.hostel.service.AllocationService;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        DatabaseOperations db = new MySQLDatabase();
        try {
            db.connect();
        } catch (DatabaseException e) {
            System.err.println("DB connection failed: " + e.getMessage());
            System.err.println("Falling back to in-memory database for demo.");
            db = new InMemoryDatabase();
        }

        AllocationService service = new AllocationService(db);
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("========================================");
            System.out.println(" MIT HOSTEL MANAGEMENT SYSTEM");
            System.out.println("========================================");
            System.out.println("1. Admin Login");
            System.out.println("2. Student Login");
            System.out.println("3. Exit");
            System.out.println("========================================");
            System.out.print("Enter choice: ");
            String top = sc.nextLine().trim();
            if (top.equals("1")) {
                System.out.print("Admin password: ");
                String pass = sc.nextLine();
                if (!"admin".equalsIgnoreCase(pass == null ? "" : pass.trim())) {
                    System.out.println("Invalid password");
                    continue;
                }
                // Admin menu
                while (true) {
                    System.out.println("\nAdmin Menu:");
                    System.out.println("1. Add student");
                    System.out.println("2. Delete student");
                    System.out.println("3. Manual allocate student to room");
                    System.out.println("4. Deallocate student");
                    System.out.println("5. Show all rooms");
                    System.out.println("6. Show available rooms");
                    System.out.println("7. Change room label");
                    System.out.println("8. Show students");
                    System.out.println("9. Logout");
                    System.out.print("Enter choice: ");
                    String a = sc.nextLine().trim();
                    try {
                        switch (a) {
                            case "1": { // Add student (auto-allocate)
                                System.out.print("Name: ");
                                String name = sc.nextLine();
                                System.out.print("Email: ");
                                String email = sc.nextLine();
                                System.out.print("Roll No (numeric): ");
                                String roll = sc.nextLine();
                                Student s = new Student(name, email, roll);
                                service.addStudent(s);
                                System.out.println("Student added with roll=" + roll);
                                // Auto-allocate immediately
                                try {
                                    List<Room> avail = db.getAvailableRooms();
                                    if (avail.isEmpty()) {
                                        System.out.println("No available rooms; student added without allocation");
                                    } else {
                                        int rid = avail.get(0).getId();
                                        service.allocateRoom(Integer.parseInt(roll), rid);
                                        System.out.println("Auto-allocated student " + roll + " to room " + rid);
                                    }
                                } catch (DatabaseException | InvalidInputException ex) {
                                    System.err.println("Allocation error: " + ex.getMessage());
                                }
                                break;
                            }
                            case "2": { // Delete student
                                System.out.print("Roll/Student ID to delete: ");
                                int sid = Integer.parseInt(sc.nextLine());
                                boolean ok = db.deleteStudent(sid);
                                System.out.println(ok ? "Deleted" : "Student not found");
                                break;
                            }
                            case "3": { // Manual allocate
                                System.out.print("Student Roll/ID to allocate: ");
                                int sid = Integer.parseInt(sc.nextLine());
                                System.out.print("Room ID to allocate to: ");
                                int rid = Integer.parseInt(sc.nextLine());
                                service.allocateRoom(sid, rid);
                                System.out.println("Allocated student " + sid + " to room " + rid);
                                break;
                            }
                            case "4": { // Deallocate
                                System.out.print("Student Roll/ID to deallocate: ");
                                int sid = Integer.parseInt(sc.nextLine());
                                boolean ok = db.deallocateStudent(sid);
                                System.out.println(ok ? "Deallocated" : "No allocation found");
                                break;
                            }
                            case "5": { // Show all rooms with occupants
                                List<Room> rooms = db.getAllRooms();
                                Map<Integer,Integer> allocs = db.getAllAllocations();
                                System.out.println("RoomID | Number | Capacity | Occupied | Occupants");
                                for (Room r : rooms) {
                                    StringBuilder occStr = new StringBuilder();
                                    int count = r.getOccupied();
                                    // find students allocated to this room
                                    for (Map.Entry<Integer,Integer> e : allocs.entrySet()) {
                                        if (e.getValue() == r.getId()) {
                                            Student st = db.getStudent(e.getKey());
                                            if (st != null) {
                                                if (occStr.length() > 0) occStr.append("; ");
                                                occStr.append(st.getName()).append(" (").append(st.getId()).append(")");
                                            }
                                        }
                                    }
                                    System.out.printf("%6d | %6s | %8d | %8d | %s\n", r.getId(), r.getNumber(), r.getCapacity(), count, occStr.toString());
                                }
                                break;
                            }
                            case "6": { // Show available rooms
                                List<Room> avail = db.getAvailableRooms();
                                if (avail.isEmpty()) System.out.println("No available rooms");
                                else {
                                    System.out.println("Available rooms:");
                                    for (Room r : avail) System.out.printf("id=%d number=%s free=%d\n", r.getId(), r.getNumber(), r.getCapacity() - r.getOccupied());
                                }
                                break;
                            }
                            case "7": { // Change room label
                                System.out.print("Room ID to change: ");
                                int rid = Integer.parseInt(sc.nextLine());
                                System.out.print("New room label: ");
                                String label = sc.nextLine();
                                db.changeRoomNumber(rid, label);
                                System.out.println("Room label updated");
                                break;
                            }
                            case "8": { // Show students list with allocated room
                                List<Student> studs = db.getAllStudents();
                                if (studs.isEmpty()) System.out.println("No students");
                                else {
                                    System.out.println("Roll | Name | Email | AllocatedRoom");
                                    for (Student st : studs) {
                                        Room room = db.getAllocatedRoomForStudent(st.getId());
                                        String rn = (room == null) ? "-" : room.getNumber();
                                        System.out.printf("%4d | %s | %s | %s\n", st.getId(), st.getName(), st.getEmail(), rn);
                                    }
                                }
                                break;
                            }
                            case "9": {
                                System.out.println("Logging out");
                                break;
                            }
                            default:
                                System.out.println("Invalid option");
                        }
                        if (a.equals("9")) break;
                    } catch (InvalidInputException | DatabaseException ex) {
                        System.err.println("Error: " + ex.getMessage());
                    } catch (NumberFormatException nfe) {
                        System.err.println("Invalid number input.");
                    }
                }
            } else if (top.equals("2")) {
                // student login
                System.out.print("Enter roll number: ");
                String roll = sc.nextLine();
                System.out.print("Enter name: ");
                String name = sc.nextLine();
                try {
                    int sid = Integer.parseInt(roll.trim());
                    Student s = db.getStudent(sid);
                    if (s == null) {
                        System.out.println("Student not found");
                        continue;
                    }
                    if (!s.getName().equalsIgnoreCase(name.trim())) {
                        System.out.println("Name and roll do not match");
                        continue;
                    }
                    Room room = db.getAllocatedRoomForStudent(sid);
                    if (room == null) System.out.println("No room allocated yet");
                    else System.out.printf("Allocated Room ID=%d Label=%s\n", room.getId(), room.getNumber());
                } catch (NumberFormatException nfe) {
                    System.out.println("Invalid roll number");
                } catch (DatabaseException dbe) {
                    System.err.println("Error: " + dbe.getMessage());
                }
            } else if (top.equals("3")) {
                System.out.println("Bye");
                return;
            } else {
                System.out.println("Invalid option");
            }
        }
    }
}
package com.hostel;

import com.hostel.dao.DatabaseOperations;
import com.hostel.dao.MySQLDatabase;
import com.hostel.dao.InMemoryDatabase;
import com.hostel.exception.DatabaseException;
import com.hostel.exception.InvalidInputException;
import com.hostel.model.Student;
import com.hostel.service.AllocationService;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        DatabaseOperations db = new MySQLDatabase();
        try {
            db.connect();
        } catch (DatabaseException e) {
            System.err.println("DB connection failed: " + e.getMessage());
            System.err.println("Falling back to in-memory database for demo.");
            db = new InMemoryDatabase();
        }

        AllocationService service = new AllocationService(db);
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("Login:\n1. Admin\n2. Student\n3. Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine();
            if (choice.equals("1")) {
                System.out.print("Admin password: ");
                String pass = sc.nextLine();
                if (!"admin".equalsIgnoreCase(pass == null ? "" : pass.trim())) {
                    System.out.println("Invalid password");
                    continue;
                }
                // admin menu
                while (true) {
                    System.out.println("Admin Menu:\n1.Add student\n2.Delete student\n3.Manual allocate student to room\n4.Auto allocate student to first available room\n5.Deallocate student\n6.Show all rooms\n7.Show available rooms\n8.Change room label\n9.Show students\n10.Logout");
                    System.out.print("Choose: ");
                    String a = sc.nextLine();
                    try {
                        switch (a) {
                            // admin menu (MIT HOSTEL MANAGEMENT SYSTEM layout)
                            while (true) {
                                System.out.println("\nAdmin Menu:\n1. Add Room\n2. Allocate Room to Student\n3. View All Allocations\n4. View all student's list\n5. Generate Reports\n6. Logout");
                                System.out.print("Enter choice: ");
                                String a = sc.nextLine();
                                System.out.print("Roll No (numeric): ");
                                    switch (a) {
                                        case "1": {
                                            // Add Room
                                            System.out.print("Room label: ");
                                            String label = sc.nextLine();
                                            System.out.print("Capacity (default 2): ");
                                            String capS = sc.nextLine();
                                            int cap = 2;
                                            try { if (!capS.isBlank()) cap = Integer.parseInt(capS); } catch (Exception ignored) {}
                                            int rid = db.addRoom(new com.hostel.model.Room(0, label, cap, 0));
                                            System.out.println("Added room id=" + rid);
                                            break;
                                        }
                                        case "2": {
                                            // Allocate Room to Student (manual)
                                            System.out.print("Student Roll/ID to allocate: ");
                                            int sid = Integer.parseInt(sc.nextLine());
                                            System.out.print("Room ID to allocate to: ");
                                            int rid = Integer.parseInt(sc.nextLine());
                                            while (true) {
                                                System.out.println("\nAdmin Menu:\n1. Add Student\n2. Add Room\n3. Allocate Room to Student\n4. View All Allocations\n5. View all student's list\n6. Generate Reports\n7. Logout");
                                                System.out.print("Enter choice: ");
                                                String a = sc.nextLine();
                                        case "3": {
                                                    switch (a) {
                                                        case "1": {
                                                            // Add Student
                                                            System.out.print("Name: ");
                                                            String name = sc.nextLine();
                                                            System.out.print("Email: ");
                                                            String email = sc.nextLine();
                                                            System.out.print("Roll No (numeric): ");
                                                            String roll = sc.nextLine();
                                                            Student s = new Student(name, email, roll);
                                                            service.addStudent(s);
                                                            System.out.println("Student added with roll=" + roll);
                                                            // allocation choice
                                                            System.out.println("Allocate now? 1. Manually allocate 2. Allocate automatically 3. Skip");
                                                            System.out.print("Choose: ");
                                                            String choiceAlloc = sc.nextLine();
                                                            if (choiceAlloc.equals("1")) {
                                                                System.out.print("Room ID to allocate to: ");
                                                                int rid = Integer.parseInt(sc.nextLine());
                                                                service.allocateRoom(Integer.parseInt(roll), rid);
                                                                System.out.println("Allocated student " + roll + " to room " + rid);
                                                            } else if (choiceAlloc.equals("2")) {
                                                                var avail = db.getAvailableRooms();
                                                                if (avail.isEmpty()) System.out.println("No available rooms");
                                                                else {
                                                                    int rid = avail.get(0).getId();
                                                                    service.allocateRoom(Integer.parseInt(roll), rid);
                                                                    System.out.println("Auto-allocated student " + roll + " to room " + rid);
                                                                }
                                                            } else {
                                                                System.out.println("Skipping allocation for now");
                                                            }
                                                            break;
                                                        }
                                                        case "2": {
                                                            // Add Room
                                                            System.out.print("Room label: ");
                                                            String label = sc.nextLine();
                                                            System.out.print("Capacity (default 2): ");
                                                            String capS = sc.nextLine();
                                                            int cap = 2;
                                                            try { if (!capS.isBlank()) cap = Integer.parseInt(capS); } catch (Exception ignored) {}
                                                            int rid = db.addRoom(new com.hostel.model.Room(0, label, cap, 0));
                                                            System.out.println("Added room id=" + rid);
                                                            break;
                                                        }
                                                        case "3": {
                                                            // Allocate Room to Student (manual)
                                                            System.out.print("Student Roll/ID to allocate: ");
                                                            int sid = Integer.parseInt(sc.nextLine());
                                                            System.out.print("Room ID to allocate to: ");
                                                            int rid = Integer.parseInt(sc.nextLine());
                                                            service.allocateRoom(sid, rid);
                                                            System.out.println("Allocated student " + sid + " to room " + rid);
                                                            break;
                                                        }
                                                        case "4": {
                                                            // View All Allocations
                                                            var allocs = db.getAllAllocations();
                                                            if (allocs.isEmpty()) System.out.println("No allocations");
                                                            else {
                                                                System.out.println("StudentRoll | StudentName | RoomLabel");
                                                                for (var e : allocs.entrySet()) {
                                                                    int sid = e.getKey();
                                                                    int rid = e.getValue();
                                                                    var s = db.getStudent(sid);
                                                                    var r = db.getAllRooms().stream().filter(x->x.getId()==rid).findFirst().orElse(null);
                                                                    String rn = r==null?"-":r.getNumber();
                                                                    System.out.printf("%10d | %s | %s\n", sid, s==null?"-":s.getName(), rn);
                                                                }
                                                            }
                                                            break;
                                                        }
                                                        case "5": {
                                                            // View all student's list
                                                            var studs = db.getAllStudents();
                                                            if (studs.isEmpty()) System.out.println("No students");
                                                            else {
                                                                System.out.println("Roll | Name | AllocatedRoom");
                                                                for (var st : studs) {
                                                                    var room = db.getAllocatedRoomForStudent(st.getId());
                                                                    String rn = (room == null) ? "-" : room.getNumber();
                                                                    System.out.printf("%4d | %s | %s\n", st.getId(), st.getName(), rn);
                                                                }
                                                            }
                                                            break;
                                                        }
                                                        case "6": {
                                                            // Generate Reports
                                                            var allRooms = db.getAllRooms();
                                                            var students = db.getAllStudents();
                                                            var allocs = db.getAllAllocations();
                                                            int allocatedCount = allocs.size();
                                                            int totalRooms = allRooms.size();
                                                            int totalStudents = students.size();
                                                            int availableSlots = allRooms.stream().mapToInt(r->r.getCapacity()-r.getOccupied()).sum();
                                                            System.out.println("Report:");
                                                            System.out.println("Total rooms: " + totalRooms);
                                                            System.out.println("Total students: " + totalStudents);
                                                            System.out.println("Allocated students: " + allocatedCount);
                                                            System.out.println("Available slots: " + availableSlots);
                                                            break;
                                                        }
                                                        case "7": {
                                                            System.out.println("Logging out");
                                                            break;
                                                        }
                try {
                    int sid = Integer.parseInt(roll);
                    Student s = db.getStudent(sid);
                    if (s == null) {
                        System.out.println("Student not found");
                        continue;
                    }
                    if (!s.getName().equalsIgnoreCase(name)) {
                        System.out.println("Name and roll do not match");
                        continue;
                    }
                    var room = db.getAllocatedRoomForStudent(sid);
                    if (room == null) System.out.println("No room allocated yet");
                    else System.out.printf("Allocated Room ID=%d Label=%s\n", room.getId(), room.getNumber());
                } catch (NumberFormatException nfe) {
                    System.out.println("Invalid roll number");
                } catch (DatabaseException dbe) {
                    System.err.println("Error: " + dbe.getMessage());
                }
            } else if (choice.equals("3")) {
                System.out.println("Bye");
                return;
            } else {
                System.out.println("Invalid option");
            }
        }
    }
}
