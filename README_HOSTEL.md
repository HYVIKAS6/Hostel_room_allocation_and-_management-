# Hostel Room Allocation and Management System (Java)

Overview
- Java-based mini project skeleton for managing hostel rooms, students, and allocations.

Key features implemented in skeleton
- Models: `User`, `Student`, `Admin`, `Room`
- DAO interface: `DatabaseOperations` and MySQL implementation `MySQLDatabase`
- Service: `AllocationService`
- Exceptions: `InvalidInputException`, `DatabaseException`
- SQL schema: `sql/schema.sql`

How to run
1. Create the database and tables by running `sql/schema.sql` against a MySQL server.
2. Update DB credentials in `src/main/java/com/hostel/dao/MySQLDatabase.java` (URL, USER, PASS).
3. Build with Maven:

```bash
mvn package
```

4. Run the app:

```bash
mvn exec:java -Dexec.mainClass="com.hostel.App"
```

Notes
- This is a minimal, instructional skeleton. Add authentication, validation, and UI as needed.
