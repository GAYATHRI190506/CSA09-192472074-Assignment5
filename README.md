# Programming in Java – Assignment 5

## CO5 – Data Persistence Engineering: Files and Databases

### Student Details

**Name:** Madaraboina Gayathri  
**Register Number:** 192472074  
**Course:** Programming in Java  
**Assignment:** Assignment 5  
**Course Outcome:** CO5 – Data Persistence Engineering  
**Java Version:** Java 26.0.1  
**Database:** MySQL 8.0.46  
**JDBC Driver:** MySQL Connector/J

---

## Project Overview

This assignment investigates Java persistence techniques using files, serialization, JDBC, transactions, and database operations.

The work is divided into two major parts:

- Part A – Persistence Design and Pseudocode
- Part B – Implementation and Experimental Investigation

---

## Project Structure

```text
Assignment5
│
├── screenshots
│
├── TaskA1_FileFormat
├── TaskA2_Buffering
├── TaskA3_DAO
├── TaskA4_Serialization
├── TaskA5_Transactions
│
├── TaskB1_FileStore
├── TaskB2_AdversarialInput
├── TaskB3_JDBC
└── TaskB4_Migration
```

---

## Part A – Persistence Design

### A1 – File Format Design

Designed text and binary formats for bus-pass records and developed pseudocode for adding, finding, and listing records.

### A2 – Buffering Analysis

Compared unbuffered byte-by-byte reading with buffered input and analyzed the performance and memory implications.

### A3 – DAO Design

Designed a generic DAO pattern for Student, Course, and Marks database operations.

### A4 – Serialization

Analyzed object graph serialization using Hostel, Room, and Resident objects, including transient fields and serialVersionUID.

### A5 – Transactions and Safety

Designed JDBC transaction handling for wallet transfers and compared unsafe SQL concatenation with PreparedStatement.

---

## Part B – Implementation and Experiments

### B1 – File Store

Implemented bus-pass file storage with add, find, list, damaged-record handling, buffering experiments, search-growth experiments, and file-size measurements.

### B2 – Adversarial Input

Tested SQL injection using unsafe SQL construction and compared it with PreparedStatement. Also tested malformed bus-pass records.

### B3 – JDBC Experiments

Compared:

- Statement
- PreparedStatement
- Batch processing
- Auto-commit
- Transactions
- Rollback

Performance was measured for 1,000, 10,000, and 100,000 rows.

ResultSetMetaData was also used to inspect database columns.

### B4 – File-to-Database Migration

Implemented migration from the bus-pass file store to MySQL using PreparedStatement, batch processing, transactions, rollback handling, and DatabaseMetaData.

---

## B3 Experimental Results

| Rows | Statement (ms) | PreparedStatement (ms) | Batch (ms) |
|---:|---:|---:|---:|
| 1,000 | 2,071 | 2,134 | 2,903 |
| 10,000 | 16,653 | 16,658 | 15,645 |
| 100,000 | 178,936 | 173,481 | 186,995 |

### Transaction Results

| Method | Time |
|---|---:|
| Auto-commit | 1,822 ms |
| Transaction | 305 ms |

### Rollback Result

The forced failure test successfully rolled back the transaction.

```text
Rows remaining after rollback: 0
ROLLBACK TEST: PASSED
```

---

## B4 Migration Result

The file-to-database migration was successfully tested using MySQL.

The migration used:

- Buffered file reading
- PreparedStatement
- Batch processing
- Transactions
- Commit and rollback
- DatabaseMetaData

The rollback experiment confirmed that partial records were not retained after a migration failure.

---

## How to Run

### B1

```powershell
javac TaskB1_FileStore\PassStore.java
java TaskB1_FileStore.PassStore
```

### B2

```powershell
javac TaskB2_AdversarialInput\B2_AdversarialInput.java
java TaskB2_AdversarialInput.B2_AdversarialInput
```

### B3

Compile using the MySQL Connector/J JAR:

```powershell
javac -cp "TaskB3_JDBC\lib\mysql-connector-j-26.10.0.jar" TaskB3_JDBC\PassLoader.java
```

Run:

```powershell
java -cp "TaskB3_JDBC;TaskB3_JDBC\lib\mysql-connector-j-26.10.0.jar" PassLoader
```

### B4

Compile:

```powershell
javac -cp "TaskB3_JDBC\lib\mysql-connector-j-26.10.0.jar" TaskB4_Migration\B4_Migration.java
```

Run:

```powershell
java -cp "TaskB4_Migration;TaskB3_JDBC\lib\mysql-connector-j-26.10.0.jar" B4_Migration
```

---

## Database Setup

Database:

```text
assignment5
```

Table:

```text
bus_passes
```

The database is created using MySQL and accessed through JDBC.

---

## Final Engineering Decision

For a multi-campus bus-pass system with high record volume and concurrent database operations, a relational database such as MySQL is preferred over plain text files or Java serialization.

PreparedStatement with transaction-controlled batch processing provides a suitable combination of security, consistency, and scalability.

---

## Result

All Assignment 5 tasks from A1 through B4 were implemented, documented, and experimentally evaluated.

The project includes source code, analysis documents, database experiments, migration testing, rollback testing, and screenshots as evidence.