# B2 – Adversarial Input Testing

## 1. Objective

The objective of this experiment is to test the Java application against adversarial inputs and verify whether it safely handles:

- SQL injection attempts
- Unsafe SQL string concatenation
- Safe `PreparedStatement` execution
- Damaged bus-pass records
- Wrong field counts
- Non-numeric amounts
- Empty records
- Invalid route numbers

The experiment also compares unsafe SQL construction with parameterized SQL and identifies suitable input-validation techniques.

---

## 2. Experimental Setup

**Programming Language:** Java  
**Java Version:** Java 26.0.1  
**Database:** MySQL  
**Database Name:** `assignment5`  
**Database URL:** `jdbc:mysql://localhost:3306/assignment5`

The implementation is contained in:

```text
TaskB2_AdversarialInput/
└── B2_AdversarialInput.java
```

The program performs two major tests:

1. SQL injection testing
2. Damaged bus-pass file testing

---

## 3. SQL Injection Test

### 3.1 Crafted Input

The following malicious input was used:

```text
Username: ' OR '1'='1
Password: ' OR '1'='1
```

This input attempts to change the logical condition of the SQL query.

---

### 3.2 Unsafe SQL Construction

The unsafe implementation constructs the SQL statement using string concatenation:

```java
String sql =
        "SELECT * FROM users WHERE username = '"
        + username
        + "' AND password = '"
        + password
        + "'";
```

For the crafted input, the generated SQL was:

```text
SELECT * FROM users WHERE username = '' OR '1'='1' AND password = '' OR '1'='1'
```

This demonstrates why directly concatenating user input into SQL is unsafe.

### Observation

The user-controlled values become part of the SQL command itself. Therefore, specially crafted input can modify the intended SQL logic.

---

## 4. PreparedStatement Test

The safe implementation uses parameterized SQL:

```java
String sql =
        "SELECT * FROM users "
        + "WHERE username = ? AND password = ?";
```

The values are supplied separately:

```java
ps.setString(1, username);
ps.setString(2, password);
```

The output showed:

```text
Safe SQL uses placeholders:
SELECT * FROM users WHERE username = ? AND password = ?
```

### Observation

The malicious input is supplied as parameter data rather than being directly inserted into the SQL command.

Therefore, `PreparedStatement` provides a safer approach for handling user input.

---

## 5. JDBC Driver Observation

During the experiment, the following message was obtained:

```text
Unsafe login error: No suitable driver found for jdbc:mysql://localhost:3306/assignment5
```

The same issue occurred during the safe login test.

### Finding

The Java program compiled successfully, but the MySQL JDBC driver was not available in the current classpath. Therefore, the database connection could not be established during this run.

However, the experiment successfully demonstrated:

- The unsafe SQL generated using string concatenation
- The safe parameterized SQL structure
- The difference between `Statement` and `PreparedStatement`

The actual database execution requires the MySQL Connector/J driver to be configured.

---

# 6. Damaged Bus-Pass File Test

A damaged input file was created:

```text
TaskB2_AdversarialInput/damaged_bus_passes.txt
```

The file contained valid and invalid records.

### Test Data

```text
P200,192472074,Gayathri,101,Avadi,2026-12-31,500.00
BAD001,192400001,WrongRecord
BAD002,192400002,Student2,102,Chennai,2026-12-31,ABC

BAD003,192400003,Student3,ABC,Chennai,2026-12-31,500.00
```

A valid bus-pass record contains seven fields:

```text
Pass ID
Registration Number
Name
Route Number
Boarding Stop
Validity Date
Amount Paid
```

---

## 7. Damaged Input Results

The program detected the following errors:

```text
Line 2: WRONG FIELD COUNT -> BAD001,192400001,WrongRecord

Line 3: NON-NUMERIC AMOUNT -> BAD002,192400002,Student2,102,Chennai,2026-12-31,ABC

Line 4: EMPTY LINE

Line 5: INVALID ROUTE -> BAD003,192400003,Student3,ABC,Chennai,2026-12-31,500.00
```

### Interpretation

| Test Case | Error Detected | Result |
|---|---|---|
| Wrong number of fields | `BAD001` | Detected |
| Non-numeric amount | `BAD002` | Detected |
| Empty record | Line 4 | Detected |
| Invalid route number | `BAD003` | Detected |

---

# 8. Findings

The experiment produced the following findings:

### Finding 1 – SQL Injection

SQL concatenation can allow specially crafted input to modify the intended SQL query.

### Finding 2 – PreparedStatement

`PreparedStatement` separates SQL commands from user-provided values and is the preferred approach for database input.

### Finding 3 – Wrong Field Count

The program detects records that do not contain exactly seven fields.

### Finding 4 – Non-Numeric Amount

The program detects an amount that cannot be converted to a numeric value.

### Finding 5 – Empty Records

Blank lines are detected and reported instead of being processed as valid records.

### Finding 6 – Invalid Route

A route number that cannot be converted into an integer is detected.

### Finding 7 – Additional Validation

Escaping and regular-expression validation can provide additional input validation, but they should not replace parameterized SQL for preventing SQL injection.

---

# 9. Alternative Approach

An alternative approach is to validate input before processing it.

Examples include:

- Checking whether required fields are present
- Validating the number of fields
- Validating numeric values
- Validating route numbers
- Checking date formats
- Rejecting empty records
- Applying regular expressions where appropriate
- Escaping special characters when handling text

For database operations, however, `PreparedStatement` should still be used because validation alone is not a complete defense against SQL injection.

---

# 10. Engineering Conclusion

The adversarial-input experiment shows that untrusted input must not be directly concatenated into SQL statements.

The recommended database approach is:

```text
User Input
    ↓
Input Validation
    ↓
PreparedStatement
    ↓
Database
```

For file-based bus-pass records, structural and datatype validation should be performed before accepting a record.

The experiment successfully demonstrated damaged-record detection and the difference between unsafe SQL construction and parameterized SQL. The database execution portion requires the MySQL JDBC driver to be configured before the login queries can be executed against the local database.

---

## 11. Program Execution

Compile:

```powershell
javac TaskB2_AdversarialInput\B2_AdversarialInput.java
```

Run:

```powershell
java -cp . TaskB2_AdversarialInput
```

Expected final message:

```text
B2 experiment completed successfully.
```

---

## 12. Screenshots

The following screenshots should be saved in the project `screenshots` folder:

```text
B2_01_SQL_Injection.png
B2_02_PreparedStatement.png
B2_03_Damaged_Input.png
B2_04_Findings.png
```

These screenshots provide evidence of the adversarial-input experiment and its results.

---

## 13. Final Result

The B2 Adversarial Input experiment was completed successfully.

The experiment demonstrated:

- SQL injection using crafted input
- Risk of SQL string concatenation
- Parameterized SQL using `PreparedStatement`
- Detection of wrong field counts
- Detection of non-numeric amounts
- Detection of empty records
- Detection of invalid route numbers
- Importance of input validation and safe database access