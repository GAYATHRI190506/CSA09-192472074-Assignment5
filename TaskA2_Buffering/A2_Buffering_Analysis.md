# TASK A2 – DON'T READ WHAT YOU DON'T NEED

**Course:** Programming in Java  
**Assignment:** Assignment 5 – CO5  
**Topic:** Data Persistence Engineering – Files and Databases  
**Task:** A2 – Don't Read What You Don't Need  
**Bloom's Level:** K4 – Analyse  
**Marks:** 5  

---

## 1. Scenario

Assume an attendance log file has a size of approximately **50 MB**.

The inefficient approach is to read the file **one byte at a time** using an unbuffered input stream.

For example:

```java
FileInputStream fis = new FileInputStream("attendance.log");

int data;

while ((data = fis.read()) != -1) {
    // Process one byte
}

fis.close();
```

Here, `read()` is called once for every byte in the file.

---

## 2. Predict the Number of Read Calls

A 50 MB file contains approximately:

```text
50 × 1024 × 1024
= 52,428,800 bytes
```

Therefore, reading one byte at a time results in approximately:

```text
52,428,800 read() calls
```

This produces a very large number of I/O operations.

### Cost Analysis

Each `read()` operation may involve method-call overhead and interaction with the underlying input system.

Therefore:

```text
More read() calls
        ↓
More method-call and I/O overhead
        ↓
Slower file processing
```

The total amount of data is still 50 MB, but the number of individual read operations becomes extremely large.

---

## 3. Buffered Redesign

A better approach is to use `BufferedInputStream`.

```java
FileInputStream fis =
        new FileInputStream("attendance.log");

BufferedInputStream bis =
        new BufferedInputStream(fis);

int data;

while ((data = bis.read()) != -1) {
    // Process one byte
}

bis.close();
```

Although the program still processes the data byte by byte, `BufferedInputStream` internally reads larger blocks of data into memory.

Therefore, the application does not need to perform a physical file-system read for every individual byte.

### Concept

```text
Without Buffering

Application
    ↓
read one byte
    ↓
File System
    ↓
read one byte
    ↓
File System
    ↓
... millions of times


With Buffering

Application
    ↓
BufferedInputStream
    ↓
Read a larger block
    ↓
File System
```

This significantly reduces the number of expensive underlying I/O operations.

---

## 4. Why Buffering Improves Performance

Buffering improves performance because data is transferred in blocks rather than requiring a separate underlying I/O operation for every byte.

For example, if a buffer holds 8 KB:

```text
8 KB = 8192 bytes
```

A 50 MB file can be processed using approximately:

```text
52,428,800 / 8,192
≈ 6,400 buffer fills
```

instead of approximately:

```text
52,428,800 individual unbuffered reads
```

The exact number depends on the buffer size and implementation.

Thus, buffering reduces I/O overhead and improves throughput.

---

## 5. Limitation of Buffering

Buffering does not eliminate the need to process the complete file.

If the application must inspect every byte of a 50 MB attendance log, the program still has to process approximately 50 MB of data.

Therefore:

```text
Buffering reduces I/O overhead,
but it does not change the amount of data that must be processed.
```

Another limitation is that buffering requires some additional memory for the buffer.

However, this memory requirement is generally small and fixed compared with loading the complete file into memory.

---

## 6. Alternative – Files.readAllLines()

Another approach is:

```java
List<String> lines =
        Files.readAllLines(
            Path.of("attendance.log")
        );
```

This reads the complete file and stores the lines in memory.

### Advantage

The code is simple and convenient when the file is small and the complete contents are required.

### Disadvantage

For a 50 MB file, the complete file contents are loaded into memory.

The actual memory usage can be greater than 50 MB because Java also stores:

- `String` objects
- character data
- the `List`
- object metadata

Therefore, `Files.readAllLines()` may consume significantly more memory than a streaming approach.

---

## 7. Complexity Comparison

| Approach | Time Complexity | Extra Memory | Suitable for 50 MB? |
|---|---|---|---|
| `FileInputStream.read()` one byte at a time | O(n) | O(1) | Inefficient |
| `BufferedInputStream` | O(n) | O(buffer size) | Yes |
| `Files.readAllLines()` | O(n) | O(n) | Less suitable for large files |

Here, `n` represents the amount of data in the file.

All approaches must process the required data, so their asymptotic time complexity remains **O(n)**. The main improvement from buffering is the reduction of I/O overhead.

---

## 8. Final Analysis

For a 50 MB attendance log, reading one byte at a time is inefficient because it can result in approximately **52,428,800 read operations**.

A `BufferedInputStream` is a better design because it reads data in blocks and reduces the number of underlying I/O operations while keeping memory usage relatively small.

`Files.readAllLines()` is convenient for small files or when all lines are required simultaneously, but it can consume much more memory for a large file.

### Engineering Decision

For a large attendance log that can be processed sequentially:

**Use `BufferedInputStream` or another buffered streaming approach.**

This provides a good balance between:

- Performance
- Memory usage
- Scalability
- Simplicity