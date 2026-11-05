\# B4 – Final Engineering Decision



\## 1. Objective



The objective of B4 is to migrate bus-pass records from the file-based storage system developed in B1 into the MySQL database developed in B3.



The migration uses:



\- `BufferedReader` for reading the source file.

\- `PreparedStatement` for safe database insertion.

\- Batch processing using `addBatch()` and `executeBatch()`.

\- JDBC transactions using `setAutoCommit(false)`.

\- `commit()` when migration succeeds.

\- `rollback()` when a migration failure occurs.

\- `DatabaseMetaData` to identify the database product, version, JDBC driver, and available tables.



\---



\## 2. File-to-Database Migration



The source file is:



`TaskB1\_FileStore/bus\_passes.txt`



Each valid record contains seven fields:



1\. Pass ID

2\. Registration number

3\. Name

4\. Route number

5\. Boarding stop

6\. Validity date

7\. Amount paid



The Java program reads each line using `BufferedReader`.



The line is split using a comma delimiter and the seven fields are converted to the appropriate JDBC types.



The database insertion uses a `PreparedStatement`:



```text

INSERT INTO bus\_passes

(pass\_id, reg\_no, name, route\_no, boarding\_stop,

&#x20;validity\_date, amount\_paid)

VALUES (?, ?, ?, ?, ?, ?, ?)

```



Records are added to a batch and executed using `executeBatch()`.



\---



\## 3. Transaction and Failure Handling



The migration uses an explicit transaction.



The connection is changed to manual commit mode:



```text

con.setAutoCommit(false);

```



After successful migration:



```text

con.commit();

```



If an exception occurs:



```text

con.rollback();

```



This prevents a partially completed migration from leaving inconsistent database data.



The migration also checks for invalid records and skips records that do not contain exactly seven fields.



\---



\## 4. Migration Experiment Result



The clean migration test successfully migrated ten records.



Observed result:



```text

MySQL connection: SUCCESS

Database: assignment5



Migration committed successfully.

Records migrated: 10

Records skipped: 0

```



The database was independently verified using MySQL.



The verification returned:



```text

total\_records

10

```



All ten bus-pass records were present in the `bus\_passes` table.



\---



\## 5. Failure and Rollback Experiment



A forced failure was introduced during the migration transaction.



The test started with:



```text

Records before failure test: 10

```



A failure was deliberately generated at record 16.



The program detected the failure and executed:



```text

Rolling back transaction...

Rollback completed.

```



The final database count was:



```text

Records after rollback: 10

```



The rollback test therefore passed:



```text

ROLLBACK TEST: PASSED

No partial records remained.

```



\### Finding



The transaction successfully prevented partial insertion.



The records inserted before the forced failure were removed by the rollback, leaving the database in its original consistent state.



\---



\## 6. Failure Report



| Failure condition | Program response | Result |

|---|---|---|

| Missing source file | Reports source file not found | Migration stops safely |

| Empty line | Skips the line | Database remains valid |

| Incorrect field count | Skips invalid record | Invalid record is not inserted |

| Invalid numeric/date value | Detects conversion error | Record is skipped |

| Database insertion failure | Rolls back transaction | No partial transaction remains |

| Forced failure during transaction | Calls `rollback()` | Database returns to previous state |



\---



\## 7. Database Metadata



The B4 program used `DatabaseMetaData` to inspect the database.



Observed environment:



```text

Database Product: MySQL

Database Version: 8.0.46

JDBC Driver: MySQL Connector/J

JDBC Driver Version: mysql-connector-j-26.10.0

```



The detected table was:



```text

bus\_passes

```



This demonstrates that the application can discover database and driver information programmatically rather than relying only on hard-coded assumptions.



\---



\## 8. Final Engineering Decision



For a deployment involving 12 campuses, approximately 100 times the current pass volume, and concurrent counters or updates, the recommended primary storage solution is a \*\*relational database\*\*.



\### Comparison



| Storage method | Advantages | Limitations |

|---|---|---|

| Text file | Simple, human-readable, easy to create | Difficult concurrent updates, weak integrity, manual searching |

| Java serialization | Preserves Java object structure | Java-specific, less suitable for concurrent multi-user access |

| MySQL database | Transactions, indexing, concurrency, structured queries, metadata, scalable storage | Requires database setup and administration |



\### Decision



\*\*MySQL database is the preferred production storage solution.\*\*



The migration should use:



\- `PreparedStatement`

\- Batch processing

\- Transactions

\- `commit()`

\- `rollback()`

\- Database constraints

\- Proper indexing

\- Connection management



This design provides stronger consistency and concurrent-access support than a simple file-based approach.



\---



\## 9. Alternative Approach



An alternative migration strategy is to use a multi-row `INSERT` statement or MySQL's `LOAD DATA` facility for large datasets.



These approaches can provide high-throughput bulk loading.



However, the implemented JDBC batch approach provides clearer control from the Java application and integrates naturally with transaction handling and validation.



\---



\## 10. Engineering Conclusion



The B4 experiment demonstrates a complete migration workflow from file storage to a relational database.



The final implementation successfully demonstrated:



\- File reading using buffered I/O.

\- Prepared database insertion.

\- Batch processing.

\- Transaction management.

\- Failure detection.

\- Rollback protection.

\- Database metadata discovery.

\- Verification of migrated records.



The experiment shows that database storage is the stronger engineering choice when the system must support larger data volumes, multiple campuses, concurrent operations, and reliable transactional updates.

