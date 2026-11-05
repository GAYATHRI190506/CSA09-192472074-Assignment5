# B1 – File Store Implementation and Experimental Investigation

## 1. Objective

The objective of B1 is to implement a file-based bus-pass store and experimentally investigate buffered versus unbuffered I/O and sequential search performance.

The bus-pass record contains:

1. Pass ID
2. Student Register Number
3. Student Name
4. Route Number
5. Boarding Stop
6. Validity Date
7. Amount Paid

---

## 2. Implementation

The `PassStore.java` program implements:

- Adding a bus-pass record
- Finding a pass using Pass ID
- Listing passes by route
- Handling a nonexistent file
- Detecting damaged or incomplete records
- Counting lines using unbuffered input
- Counting lines using buffered input
- Generating test files
- Measuring sequential search time

---

## 3. Stream Choices

### Adding records

`BufferedWriter` is used with `FileWriter`.

Reason:

- Character-oriented data is being written.
- Buffering reduces direct disk operations.
- Append mode allows new records to be added.

### Finding records

`BufferedReader` is used with `Files.newBufferedReader()`.

Reason:

- The file is text based.
- Records can be processed line by line.
- The complete file does not need to be loaded into memory.

### Buffered experiment

`BufferedInputStream` is compared with `FileInputStream`.

The unbuffered implementation reads one byte at a time, resulting in many underlying I/O operations.

The buffered implementation reads larger blocks internally and therefore significantly reduces expensive underlying I/O operations.

---

## 4. Required Functional Tests

### Test 1 – Add Pass

A sample pass was added successfully.

Example:

`P100,192472074,Gayathri,101,Avadi,2026-12-31,500.0`

Result:

`Pass added successfully.`

### Test 2 – Find Existing Pass

The program successfully located an existing Pass ID.

### Test 3 – Find Nonexistent Pass

The program correctly reported:

`Pass ID not found.`

### Test 4 – List Passes by Route

The program successfully displayed passes belonging to route 101.

### Test 5 – Nonexistent Route

The program correctly reported:

`No passes found for route 999`

### Test 6 – Damaged Record

A malformed record was inserted:

`BAD001,192400001,WrongRecord`

The record contains fewer than the required seven fields.

The program detects and skips the damaged record instead of terminating.

Expected result:

`Damaged record skipped: BAD001,192400001,WrongRecord`

---

## 5. Buffered vs Unbuffered Experiment

A test file containing 100,000 records was generated.

Both implementations produced the same line count.

The unbuffered implementation was significantly slower because it performs a large number of individual read operations.

The buffered implementation was significantly faster because data is read in larger blocks.

Example observation from the experiment:

| Method | Lines | Time |
|---|---:|---:|
| Unbuffered | 100000 | approximately 10,204 ms |
| Buffered | 100000 | approximately 134 ms |

The exact execution time can vary between runs because of operating-system caching, disk speed, CPU load, and JVM state.

### Finding

Buffered I/O is strongly preferred for large sequential files.

---

## 6. File-Size Experiment

The program also generates larger test files and compares unbuffered and buffered reading.

The experiment demonstrates that:

- File size increases the amount of data that must be processed.
- Unbuffered byte-by-byte processing has high I/O overhead.
- Buffered processing reduces the number of underlying I/O operations.
- Buffered reading remains the better engineering choice for large sequential files.

The exact timings depend on the machine and operating-system state.

---

## 7. Search Growth Experiment

The program measures sequential search for:

- 1,000 records
- 10,000 records
- 50,000 records
- 100,000 records

The target record is placed at the end of each generated file.

Therefore, the search must inspect almost every record.

Observed behavior:

| Records | Expected behavior |
|---:|---|
| 1,000 | Fast |
| 10,000 | Higher time |
| 50,000 | Higher time |
| 100,000 | Highest time |

### Complexity

Sequential search has:

`O(n)`

time complexity in the worst case.

As the number of records increases, the number of records inspected also increases.

---

## 8. Comparison with Pseudocode

The implementation follows the design described in A1.

| A1 Requirement | B1 Implementation |
|---|---|
| Add pass | `addPass()` |
| Find by ID | `findById()` |
| List by route | `listByRoute()` |
| Missing file handling | `Files.exists()` |
| Damaged record handling | Field-count and numeric validation |
| Buffered reading | `BufferedInputStream` / `BufferedReader` |
| Search growth | `measureFindTime()` |

The implementation therefore matches the persistence design specified in Part A.

---

## 9. Engineering Findings

1. Buffered streams are preferable for large sequential files.
2. Reading one byte at a time creates excessive I/O overhead.
3. Line-based buffered reading is appropriate for text records.
4. Damaged records should be detected and skipped rather than crashing the complete program.
5. Sequential file search has O(n) time complexity.
6. As the number of records increases, database indexing or another indexed structure becomes more appropriate for frequent searches.

---

## 10. Conclusion

The B1 implementation successfully demonstrates file-based persistence, safe handling of missing and damaged records, buffered and unbuffered I/O comparison, and growth of sequential search time.

The experiments support the conclusion that buffered I/O is more efficient for large sequential files, while a plain text file with sequential search becomes increasingly expensive as the number of records grows.