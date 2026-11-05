# TASK A4 – SERIALIZATION

**Course:** Programming in Java  
**Assignment:** Assignment 5 – CO5  
**Topic:** Data Persistence Engineering – Files and Databases  
**Task:** A4 – Serialization  
**Bloom's Level:** K4 – Analyse  
**Marks:** 4  

---

## 1. Object Graph

Consider the following hostel management object structure:

```text
Hostel
  |
  +-- Room
        |
        +-- Resident
```

A `Hostel` object contains a `Room` object, and the `Room` object contains a `Resident` object.

This forms an **object graph**.

For example:

```text
Hostel
  name = "ABC Hostel"
       |
       ↓
Room
  roomNumber = 101
       |
       ↓
Resident
  name = "Gayathri"
  registerNumber = "192472074"
  password = "mypassword"
  connection = database connection
```

---

## 2. Serialization of the Object Graph

Java serialization can convert an object and its reachable serializable objects into a byte stream.

The classes can implement `Serializable`.

Example:

```java
import java.io.Serializable;

class Hostel implements Serializable {
    private static final long serialVersionUID = 1L;

    String hostelName;
    Room room;
}

class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    int roomNumber;
    Resident resident;
}

class Resident implements Serializable {
    private static final long serialVersionUID = 1L;

    String name;
    String registerNumber;
    String password;

    transient Connection connection;
}
```

If the `Hostel` object is serialized, the reachable `Room` and `Resident` objects are also serialized, provided they implement `Serializable`.

Conceptually:

```text
Hostel
  ↓
Room
  ↓
Resident
```

The complete serializable object graph can therefore be stored in a file.

---

## 3. Use of transient

The `Resident` object contains a `Connection` field.

A database connection should not normally be serialized because it represents a live runtime resource.

Therefore:

```java
transient Connection connection;
```

is used.

The `transient` keyword tells Java serialization to skip that field.

### Before Serialization

```text
Resident
 ├── name
 ├── registerNumber
 ├── password
 └── connection
```

### After Deserialization

```text
Resident
 ├── name
 ├── registerNumber
 ├── password
 └── connection = null
```

The connection must be recreated after deserialization when required.

This prevents a runtime database resource from being stored as part of the persistent object state.

---

## 4. Adding a New Phone Field

Suppose a new field is added:

```java
String phone;
```

The class becomes:

```java
class Resident implements Serializable {

    private static final long serialVersionUID = 1L;

    String name;
    String registerNumber;
    String password;
    String phone;

    transient Connection connection;
}
```

If an older serialized `Resident` object is loaded after this compatible field addition, Java serialization can assign the default value to the newly added field when appropriate.

For a reference type such as `String`, the default value is:

```text
null
```

Therefore, the program should handle the possibility that an older object does not contain a value for the new field.

---

## 5. serialVersionUID

`serialVersionUID` is a version identifier for a serializable class.

Example:

```java
private static final long serialVersionUID = 1L;
```

During deserialization, Java checks the serialized class version against the current class version.

If the versions are incompatible, an `InvalidClassException` can occur.

Explicitly declaring `serialVersionUID` gives the developer control over the version identifier instead of relying on a compiler-generated value.

Example:

```text
Old serialized object
        ↓
serialVersionUID = 1
        ↓
Current Resident class
        ↓
serialVersionUID = 1
        ↓
Compatible version
```

If the class changes in an incompatible way, the developer can intentionally change the `serialVersionUID` and handle the migration appropriately.

---

## 6. Serialization Process

The object can be written using `ObjectOutputStream`.

Example:

```java
ObjectOutputStream out =
    new ObjectOutputStream(
        new FileOutputStream("hostel.dat")
    );

out.writeObject(hostel);
out.close();
```

The object can later be read using `ObjectInputStream`.

```java
ObjectInputStream in =
    new ObjectInputStream(
        new FileInputStream("hostel.dat")
    );

Hostel hostel =
    (Hostel) in.readObject();

in.close();
```

The object graph is reconstructed during deserialization.

---

## 7. Alternative – CSV or JSON

Instead of Java serialization, the application could store the data using CSV or JSON.

### CSV

Example:

```text
hostelName,roomNumber,name,registerNumber,phone
ABC Hostel,101,Gayathri,192472074,9876543210
```

### JSON

Conceptually:

```json
{
  "hostelName": "ABC Hostel",
  "room": {
    "roomNumber": 101,
    "resident": {
      "name": "Gayathri",
      "registerNumber": "192472074",
      "phone": "9876543210"
    }
  }
}
```

### Comparison

| Feature | Java Serialization | CSV / JSON |
|---|---|---|
| Java object graph | Directly supported | Requires mapping |
| Human readability | Low | High |
| Language interoperability | Low | High |
| Schema flexibility | Moderate | High |
| Java-specific | Yes | No |
| Runtime resources | Can use `transient` | Explicitly choose stored fields |

CSV/JSON is often preferable when the stored data needs to be read by different programming languages or systems.

---

## 8. Complexity Analysis

Let `n` represent the number of objects or the amount of data being serialized.

### Serialization

```text
Time: O(n)
Space: O(n)
```

The object graph must be traversed and written to the stream.

### Deserialization

```text
Time: O(n)
Space: O(n)
```

The stored object graph must be read and reconstructed in memory.

For CSV/JSON, parsing also generally requires processing the amount of stored data, so the time complexity is approximately:

```text
O(n)
```

The exact memory usage depends on whether the entire document is loaded into memory or processed as a stream.

---

## 9. Final Engineering Decision

Java serialization is convenient when the application needs to persist and restore Java object graphs directly.

However, fields representing runtime resources such as database connections should be marked `transient`.

An explicit `serialVersionUID` should be declared to control class-version compatibility.

For systems that require interoperability, human readability, or long-term data exchange, CSV or JSON can be a better alternative.

### Final Design

```text
Hostel
  |
  ↓
Room
  |
  ↓
Resident
  |
  +-- Persistent fields
  |
  +-- transient Connection
```

Therefore, the recommended design is to serialize the persistent object state while excluding runtime resources using `transient`, and to use an explicit `serialVersionUID` for version control.