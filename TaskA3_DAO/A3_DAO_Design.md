# TASK A3 – DISCOVER DATA-ACCESS DESIGN PATTERN

**Course:** Programming in Java  
**Assignment:** Assignment 5 – CO5  
**Topic:** Data Persistence Engineering – Files and Databases  
**Task:** A3 – Discover Data-Access Design Pattern  
**Bloom's Level:** K4 – Analyse  
**Marks:** 5  

---

## 1. Problem with Repeated Database Code

Consider an application containing separate classes such as:

```text
StudentDB
CourseDB
MarksDB
```

If each class directly contains JDBC connection code, SQL queries, `PreparedStatement` creation, `ResultSet` processing, and exception handling, the same type of database logic gets repeated.

For example:

```text
StudentDB
    ├── Connection code
    ├── INSERT SQL
    ├── SELECT SQL
    ├── UPDATE SQL
    └── DELETE SQL

CourseDB
    ├── Connection code
    ├── INSERT SQL
    ├── SELECT SQL
    ├── UPDATE SQL
    └── DELETE SQL
```

This creates several problems:

- Code duplication
- Difficult maintenance
- Strong coupling between business logic and database code
- Difficult testing
- Changes to database access may require modifications in many classes

A better approach is to separate data-access logic from the service or presentation layer.

---

## 2. Data Access Object (DAO) Pattern

The **Data Access Object (DAO)** pattern provides a separate layer for accessing persistent data.

The application can be organized as:

```text
Presentation / GUI
        ↓
Service / Business Layer
        ↓
DAO Layer
        ↓
JDBC
        ↓
Database
```

The service layer performs business operations, while the DAO layer performs database operations.

---

## 3. Generic DAO Interface

A generic DAO interface can be created so that the same design can be reused for different entity types.

Example:

```java
public interface Dao<T, K> {

    void insert(T entity);

    T findById(K id);

    List<T> findAll();

    void update(T entity);

    void delete(K id);
}
```

Here:

- `T` represents the entity type.
- `K` represents the type of the primary key.
- `insert()` adds a new record.
- `findById()` searches for one record.
- `findAll()` retrieves all records.
- `update()` modifies an existing record.
- `delete()` removes a record.

---

## 4. Student DAO Example

Suppose the application has a `Student` class:

```java
public class Student {

    private int id;
    private String name;
    private String registerNumber;

    public Student(int id, String name, String registerNumber) {
        this.id = id;
        this.name = name;
        this.registerNumber = registerNumber;
    }
}
```

The database table can be:

```text
STUDENT
--------------------------------
id              INT PRIMARY KEY
name            VARCHAR(100)
register_number VARCHAR(20)
```

The DAO can implement the generic interface:

```java
public class StudentDao implements Dao<Student, Integer> {

    @Override
    public void insert(Student student) {
        // JDBC INSERT operation
    }

    @Override
    public Student findById(Integer id) {
        // JDBC SELECT operation
        return null;
    }

    @Override
    public List<Student> findAll() {
        // JDBC SELECT operation
        return null;
    }

    @Override
    public void update(Student student) {
        // JDBC UPDATE operation
    }

    @Override
    public void delete(Integer id) {
        // JDBC DELETE operation
    }
}
```

The `StudentDao` is responsible for converting between Java objects and database rows.

---

## 5. Mapping Student to the Database Table

The mapping can be represented as:

| Java Attribute | Database Column | Type |
|---|---|---|
| `id` | `id` | INT |
| `name` | `name` | VARCHAR |
| `registerNumber` | `register_number` | VARCHAR |

For example:

```text
Java Object

Student
id = 101
name = "Gayathri"
registerNumber = "192472074"

        ↓ DAO mapping

Database Row

101 | Gayathri | 192472074
```

When inserting, the DAO converts the Java object into an SQL operation.

When retrieving, the DAO converts the database row back into a `Student` object.

---

## 6. Example DAO SQL

The `insert()` method can use a `PreparedStatement`:

```java
String sql =
    "INSERT INTO student(id, name, register_number) VALUES (?, ?, ?)";

PreparedStatement ps =
    connection.prepareStatement(sql);

ps.setInt(1, student.getId());
ps.setString(2, student.getName());
ps.setString(3, student.getRegisterNumber());

ps.executeUpdate();
```

The SQL and JDBC code remain inside the DAO rather than being repeated in the service or GUI.

---

## 7. Why SQL Should Not Be Placed Directly in the Service or GUI

Putting SQL directly inside the service or GUI layer creates tight coupling.

For example:

```text
GUI
 ↓
SQL
 ↓
JDBC
 ↓
Database
```

If the database structure changes, the GUI or service code may also need to be changed.

With DAO:

```text
GUI
 ↓
Service
 ↓
DAO
 ↓
JDBC
 ↓
Database
```

The GUI does not need to know how the database is accessed.

### Advantages

1. **Separation of concerns**  
   Business logic and database access remain separate.

2. **Reusability**  
   The same DAO can be used by multiple services or interfaces.

3. **Maintainability**  
   Database-related changes are concentrated in the DAO layer.

4. **Testability**  
   The service layer can be tested separately from the database.

5. **Reduced duplication**  
   Common database operations are organized into reusable DAO classes.

---

## 8. Complexity Analysis

Assume there are `n` records in a table.

| Operation | Typical Complexity | Explanation |
|---|---:|---|
| `insert()` | O(1) average | One row is inserted |
| `findById()` | O(1) average with indexed primary key | Database index can locate the row efficiently |
| `findAll()` | O(n) | All rows must be retrieved |
| `update()` | O(1) average with indexed key | Target row can be located using its key |
| `delete()` | O(1) average with indexed key | Target row can be located using its key |

The exact database performance depends on indexes, query plans, hardware, and database size.

---

## 9. Alternative Design – SQL Directly in Service or GUI

An alternative is to place SQL directly inside the service or GUI layer.

Example:

```text
StudentService
    ↓
SQL + JDBC
    ↓
Database
```

### Advantages

- Simple for very small applications
- Fewer classes
- Faster to implement initially

### Disadvantages

- High coupling
- Code duplication
- Difficult maintenance
- Difficult testing
- Database changes affect business/presentation code

For a larger application, the DAO approach provides better separation and maintainability.

---

## 10. Final Engineering Decision

The **DAO pattern** is preferred because it separates database-access responsibilities from business and presentation logic.

The generic interface:

```java
Dao<T, K>
```

allows the same design to be reused for:

```text
StudentDao
CourseDao
MarksDao
```

Therefore, DAO provides a cleaner architecture, reduces repeated JDBC code, improves maintainability, and makes the application easier to test and extend.

### Final Architecture

```text
              User / GUI
                  ↓
          Service / Business
                  ↓
              DAO Layer
        ┌─────────┼─────────┐
        ↓         ↓         ↓
 StudentDao   CourseDao   MarksDao
        └─────────┼─────────┘
                  ↓
                 JDBC
                  ↓
              Database
```