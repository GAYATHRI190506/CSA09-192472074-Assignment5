# B3 – Statement vs PreparedStatement vs Batch Processing

## 1. Objective

The objective of this experiment is to compare JDBC `Statement`, `PreparedStatement`, and batch processing for inserting bus-pass records into a MySQL database.

The experiment also investigates:

- Correctness of 10-row insertion
- ResultSetMetaData
- Performance for 1,000, 10,000, and 100,000 rows
- Auto-commit versus transaction performance
- Transaction rollback when a failure occurs

---

## 2. Database Used

**Database:** MySQL  
**Database Name:** assignment5  
**Table:** bus_passes  
**JDBC Driver:** MySQL Connector/J

The MySQL connection was successfully established.

```text
MySQL connection: SUCCESS
Database: assignment5
```

---

## 3. 10-Row Correctness Test

The program inserted 10 bus-pass records into the `bus_passes` table.

```text
Rows inserted: 10
10-row correctness test: PASSED
```

Therefore, the basic JDBC insertion functionality worked correctly.

---

## 4. ResultSetMetaData

The experiment used `ResultSetMetaData` to inspect the structure of the `bus_passes` table.

```text
Number of columns: 7

1. pass_id | Type: VARCHAR | Size: 20
2. reg_no | Type: VARCHAR | Size: 20
3. name | Type: VARCHAR | Size: 100
4. route_no | Type: INT | Size: 10
5. boarding_stop | Type: VARCHAR | Size: 100
6. validity_date | Type: DATE | Size: 10
7. amount_paid | Type: DECIMAL | Size: 10
```

The metadata confirms that the table contains the expected seven columns and their corresponding SQL data types.

---

## 5. Performance Benchmark

The three JDBC approaches were tested with 1,000, 10,000, and 100,000 rows.

| Rows | Statement (ms) | PreparedStatement (ms) | Batch (ms) |
|---:|---:|---:|---:|
| 1,000 | 2,071 | 2,134 | 2,903 |
| 10,000 | 16,653 | 16,658 | 15,645 |
| 100,000 | 178,936 | 173,481 | 186,995 |

### Findings

For 1,000 rows, the Statement approach was slightly faster than the other two approaches in this particular run.

For 10,000 rows, batch processing produced the lowest measured time.

For 100,000 rows, PreparedStatement produced the lowest measured time.

The results show that performance can vary depending on dataset size and execution conditions. Therefore, a single benchmark should not be treated as a universal ranking of the three approaches.

---

## 6. Auto-Commit vs Transaction

The experiment compared inserting records using auto-commit and using a transaction.

```text
Auto-commit time: 1822 ms
Transaction time: 305 ms
```

The transaction completed much faster in this experiment.

### Engineering Finding

With auto-commit enabled, individual database operations may be committed separately. A transaction groups multiple operations together and commits them as one unit.

Therefore, for large numbers of related inserts, transaction-based processing is preferable because it can reduce commit overhead and also provides atomicity.

---

## 7. Forced Failure and Rollback Test

A failure was intentionally forced at row 11.

```text
Forcing failure at row 11...
Failure detected: Invalid amount supplied at row 11
Rolling back transaction...
Rollback completed.
Rows remaining after rollback: 0
ROLLBACK TEST: PASSED
```

### Finding

The rollback test successfully demonstrated transaction atomicity.

When the failure occurred, the transaction was rolled back and no partial records remained in the table.

This prevents the database from being left in an inconsistent state when a migration or bulk insertion operation fails halfway through.

---

## 8. Statement vs PreparedStatement vs Batch – Engineering Trade-Off

### Statement

Advantages:

- Simple for basic SQL execution
- Suitable for fixed SQL statements

Disadvantages:

- SQL values must be constructed into the SQL string
- More difficult to safely handle user-supplied values
- Repeated execution can be inefficient

### PreparedStatement

Advantages:

- Uses parameterized SQL
- Safer for user-supplied values
- Suitable for repeated insert operations
- Separates SQL structure from data values

Disadvantages:

- Requires parameter binding
- Performance depends on the workload and database configuration

### Batch Processing

Advantages:

- Groups multiple operations
- Useful for bulk insertion
- Can reduce communication/processing overhead

Disadvantages:

- Requires additional batch management
- Large batches may consume more memory
- Appropriate batch size depends on the workload

---

## 9. Recommended Approach

For the bus-pass database application, `PreparedStatement` with transaction-based batch processing is the preferred engineering approach.

The reason is that it provides:

1. Safer parameter handling
2. Efficient repeated insertion
3. Transactional consistency
4. Better suitability for bulk migration
5. Easy rollback when an error occurs

The benchmark results also demonstrate that the performance characteristics change with dataset size, so the implementation should be tested with realistic workloads rather than relying only on a small dataset.

---

## 10. Alternative Approach

Another possible approach is a multi-row `INSERT` statement, where multiple records are inserted using one SQL command.

For very large datasets, MySQL's `LOAD DATA` mechanism can also be considered when the input is already available in a suitable file format.

These approaches may provide high throughput, but they should be evaluated against application requirements, validation needs, transaction control, and maintainability.

---

## 11. Conclusion

The B3 experiment successfully demonstrated JDBC data insertion, metadata inspection, performance benchmarking, transaction processing, and rollback handling.

The 10-row correctness test passed, the database metadata was successfully retrieved, and benchmarks were completed for 1,000, 10,000, and 100,000 rows.

The transaction test was faster than auto-commit in the measured experiment, and the forced-failure test successfully rolled back all changes.

Therefore, for this application, **PreparedStatement combined with transaction-controlled batch processing** is the preferred engineering design for reliable and secure bulk data insertion.