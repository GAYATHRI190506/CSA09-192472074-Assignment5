# TASK A1 – DESIGN A FILE FORMAT

**Course:** Programming in Java  
**Assignment:** Assignment 5 – CO5  
**Topic:** Data Persistence Engineering – Files and Databases  
**Task:** A1 – Design a File Format  
**Bloom's Level:** K4 – Analyse  
**Marks:** 7  

---

## A1.1 – Represent the Record

The transport office bus-pass record contains the following fields:

1. Pass ID
2. Student Register Number
3. Name
4. Route Number
5. Boarding Stop
6. Validity Date
7. Amount Paid

### (a) Delimited Text File

A comma (`,`) is used as the delimiter.

### Field Layout

| Field | Data Type | Order |
|---|---|---:|
| Pass ID | String | 1 |
| Student Register Number | String | 2 |
| Name | String | 3 |
| Route Number | Integer | 4 |
| Boarding Stop | String | 5 |
| Validity Date | String (`YYYY-MM-DD`) | 6 |
| Amount Paid | Double | 7 |

### Example

```text
P1001,192472074,Gayathri,12,Avadi,2027-03-31,4500.0
```

If a name contains the delimiter, CSV quoting is used.

Example:

```text
P1002,192472075,"Gayathri, M",12,Avadi,2027-03-31,4500.0
```

If a double quote occurs inside a quoted field, it is represented by two double quotes.

Example:

```text
"Gayathri ""GM"""
```

This prevents commas inside a name from being incorrectly interpreted as field separators.

### (b) Binary File using DataOutputStream

The binary record is written in a fixed field order using `DataOutputStream`.

| Field | Data Type | DataOutputStream Method |
|---|---|---|
| Pass ID | String | `writeUTF()` |
| Student Register Number | String | `writeUTF()` |
| Name | String | `writeUTF()` |
| Route Number | Integer | `writeInt()` |
| Boarding Stop | String | `writeUTF()` |
| Validity Date | String | `writeUTF()` |
| Amount Paid | Double | `writeDouble()` |

The record is written conceptually as:

```text
writeUTF(passId)
writeUTF(registerNumber)
writeUTF(name)
writeInt(routeNumber)
writeUTF(boardingStop)
writeUTF(validityDate)
writeDouble(amountPaid)
```

The binary format does not require delimiters because the data types and the order of reading and writing define the record structure.

---

## A1.2 – Design the Operations

### Operation 1 – Add a New Pass

**Algorithm: ADD_PASS**

```text
ALGORITHM ADD_PASS(passId, regNo, name, routeNo, boardingStop, validityDate, amountPaid)

BEGIN

    OPEN the pass file using FileWriter
    CREATE a BufferedWriter around the FileWriter

    CREATE a record using:
        passId, regNo, name, routeNo,
        boardingStop, validityDate, amountPaid

    WRITE the record to the file
    WRITE a newline

    CLOSE the BufferedWriter

    IF an I/O error occurs THEN
        REPORT "Unable to add pass"
    END IF

END
```

**Stream justification:**  
`FileWriter` is used because the data is character/text data. `BufferedWriter` is used to reduce direct file-writing operations and improve efficiency when records are written repeatedly.

---

### Operation 2 – Find a Pass by Pass ID

**Algorithm: FIND_PASS_BY_ID**

```text
ALGORITHM FIND_PASS_BY_ID(passId)

BEGIN

    IF the pass file does not exist THEN
        REPORT "Pass file not found"
        RETURN NOT_FOUND
    END IF

    OPEN the pass file using FileReader
    CREATE a BufferedReader around the FileReader

    WHILE a line can be read from the file DO

        IF the line is damaged or does not contain
           exactly 7 valid fields THEN
            SKIP the line
            CONTINUE reading the next record
        END IF

        SPLIT the line into its fields

        IF the Pass ID field equals passId THEN
            DISPLAY the complete pass record
            CLOSE the BufferedReader
            RETURN the record
        END IF

    END WHILE

    CLOSE the BufferedReader

    REPORT "Pass not found"

    RETURN NOT_FOUND

END
```

**Stream justification:**  
`FileReader` reads character data from the text file. `BufferedReader` allows efficient line-by-line reading using `readLine()`.

---

### Operation 3 – List All Passes on a Given Route

**Algorithm: LIST_PASSES_BY_ROUTE**

```text
ALGORITHM LIST_PASSES_BY_ROUTE(routeNo)

BEGIN

    IF the pass file does not exist THEN
        REPORT "Pass file not found"
        RETURN
    END IF

    OPEN the pass file using FileReader
    CREATE a BufferedReader around the FileReader

    SET found ← FALSE

    WHILE a line can be read from the file DO

        IF the line is damaged or does not contain
           exactly 7 valid fields THEN
            SKIP the line
            CONTINUE reading the next record
        END IF

        SPLIT the line into its fields

        IF the Route Number field equals routeNo THEN
            DISPLAY the pass record
            SET found ← TRUE
        END IF

    END WHILE

    CLOSE the BufferedReader

    IF found = FALSE THEN
        REPORT "No passes found for the given route"
    END IF

END
```

**Stream justification:**  
`FileReader` and `BufferedReader` are used because the records are stored as text and need to be processed line by line.

### Stream Selection Summary

| Operation | Stream Classes | Reason |
|---|---|---|
| Add pass | `FileWriter` + `BufferedWriter` | Efficient character/text writing |
| Find pass | `FileReader` + `BufferedReader` | Efficient line-by-line reading |
| List by route | `FileReader` + `BufferedReader` | Sequential line-by-line searching |

For binary data, `FileInputStream`/`FileOutputStream` can be combined with `DataInputStream`/`DataOutputStream`.

### Complexity of the Text-File Design

For `n` records:

- Add a new pass: approximately **O(1)** for an append operation.
- Find a pass by ID: **O(n)** in the worst case because records are scanned sequentially.
- List passes by route: **O(n)** because every record may need to be examined.

---

## A1.3 – Design for Damaged Data

### 1. Damaged Line

A record is considered damaged when:

- It does not contain exactly seven fields.
- A numeric field such as route number or amount cannot be converted to the required type.
- A required field is empty or invalid.

The program should report the damaged record, skip it, and continue processing the remaining records.

```text
IF the number of fields is not 7 THEN
    REPORT the line as damaged
    SKIP the line
    CONTINUE reading the next record
END IF
```

A count or report of damaged records can also be maintained so that the transport office can correct them later.

### 2. Missing File

Before a read operation:

```text
IF the pass file does not exist THEN
    REPORT "Pass file not found"
    RETURN an appropriate error status
END IF
```

For an add operation, the file can be created if it does not already exist.

### 3. Incomplete Write Caused by Power Failure

A power failure during a write may leave an incomplete final record.

Example of an incomplete record:

```text
P1004,192472078,Gayathri,12,Avadi
```

The complete record should contain seven fields:

```text
P1004,192472078,Gayathri,12,Avadi,2027-03-31,4500.0
```

During the next read, the incomplete record fails validation because it does not contain the expected seven fields. It is therefore reported as damaged and skipped.

For stronger protection, the application can use a temporary-file-and-replace strategy:

```text
WRITE new records to a temporary file
VALIDATE the temporary file
CLOSE and flush the file
REPLACE the original file with the validated file
```

This reduces the possibility of leaving the main data file partially updated.

---

# Alternative Concept – RandomAccessFile with Fixed-Length Records

Instead of a delimited text file, the records can be stored using `RandomAccessFile` with fixed-length records.

Each record is assigned the same number of bytes.

For example:

```text
Record 0 → bytes 0 to recordSize - 1
Record 1 → bytes recordSize to 2 × recordSize - 1
Record 2 → bytes 2 × recordSize to 3 × recordSize - 1
```

To access record `i`:

```text
position ← i × recordSize
seek(position)
read the record
```

### Complexity Comparison

| Operation/Property | Delimited Text File | Fixed-Length RandomAccessFile |
|---|---:|---:|
| Sequential find by ID | O(n) | O(n) without an index |
| Direct record access | Difficult | O(1) when record number is known |
| Space efficiency | Higher | Lower because of padding |
| Variable-length names | Easy | Require a fixed maximum size |
| Random access | Poor | Efficient |

An important limitation is that **O(1) direct access requires knowing the record number**. If only the pass ID is known and no index is maintained, the program still has to scan the records, resulting in O(n) search time.

An index can map:

```text
Pass ID → Record Number
```

Then the record position can be calculated as:

```text
recordNumber × recordSize
```

and accessed using `seek()`.

### Space Wasted by Padding

Suppose fixed-length fields allocate:

```text
Pass ID          20 bytes
Register No      15 bytes
Name             50 bytes
Route             4 bytes
Boarding Stop    40 bytes
Validity Date    10 bytes
Amount             8 bytes
--------------------------------
Total            147 bytes
```

If an actual name requires only 10 bytes but 50 bytes are reserved, then:

```text
50 - 10 = 40 bytes
```

are unused for that field.

For thousands of records, this padding can consume significant storage space.

---

# A1 Conclusion

The delimited text design is simple, human-readable and flexible for approximately 8,000 annual bus-pass records. Buffered character streams make sequential reading and writing efficient, while validation allows damaged records to be detected without stopping the entire operation.

A fixed-length `RandomAccessFile` design provides efficient direct access when a record number or index is available, but it wastes space because of padding and is less flexible for variable-length fields.

For the current requirement, the **buffered delimited text format** is appropriate because the main requirements are adding records, searching records, listing records by route, and handling damaged data safely.