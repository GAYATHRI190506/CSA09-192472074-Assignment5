import java.io.*;
import java.nio.file.*;

public class PassStore {

    private static final String FILE_NAME =
            "TaskB1_FileStore" + File.separator + "bus_passes.txt";

    // ============================================================
    // 1. ADD A BUS PASS
    // ============================================================
    public static void addPass(
            String passId,
            String regNo,
            String name,
            int routeNumber,
            String boardingStop,
            String validityDate,
            double amountPaid) {

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(FILE_NAME, true))) {

            writer.write(passId + "," +
                    regNo + "," +
                    name + "," +
                    routeNumber + "," +
                    boardingStop + "," +
                    validityDate + "," +
                    amountPaid);

            writer.newLine();

            System.out.println("Pass added successfully.");

        } catch (IOException e) {
            System.out.println(
                    "Error while adding pass: " + e.getMessage());
        }
    }

    // ============================================================
    // 2. FIND PASS BY ID
    // ============================================================
    public static void findById(String passId) {

        Path path = Paths.get(FILE_NAME);

        if (!Files.exists(path)) {
            System.out.println("File does not exist.");
            return;
        }

        try (BufferedReader reader =
                     Files.newBufferedReader(path)) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] fields = line.split(",", -1);

                // Detect damaged/incomplete record
                if (fields.length != 7) {
                    System.out.println(
                            "Damaged record skipped: " + line);
                    continue;
                }

                if (fields[0].equals(passId)) {

                    System.out.println("Pass found:");
                    System.out.println(line);
                    return;
                }
            }

            System.out.println("Pass ID not found.");

        } catch (IOException e) {
            System.out.println(
                    "Error reading file: " + e.getMessage());
        }
    }

    // ============================================================
    // 3. LIST PASSES BY ROUTE
    // ============================================================
    public static void listByRoute(int routeNumber) {

        Path path = Paths.get(FILE_NAME);

        if (!Files.exists(path)) {
            System.out.println("File does not exist.");
            return;
        }

        boolean found = false;

        try (BufferedReader reader =
                     Files.newBufferedReader(path)) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] fields = line.split(",", -1);

                if (fields.length != 7) {

                    System.out.println(
                            "Damaged record skipped: " + line);

                    continue;
                }

                try {

                    int route =
                            Integer.parseInt(fields[3]);

                    if (route == routeNumber) {

                        System.out.println(line);
                        found = true;
                    }

                } catch (NumberFormatException e) {

                    System.out.println(
                            "Invalid route number in record: "
                            + line);
                }
            }

            if (!found) {

                System.out.println(
                        "No passes found for route "
                        + routeNumber);
            }

        } catch (IOException e) {

            System.out.println(
                    "Error reading file: " + e.getMessage());
        }
    }

    // ============================================================
    // 4. CHECK ALL DAMAGED RECORDS
    // ============================================================
    public static void checkDamagedRecords(String fileName) {

        Path path = Paths.get(fileName);

        if (!Files.exists(path)) {

            System.out.println(
                    "File does not exist.");

            return;
        }

        boolean damagedFound = false;

        try (BufferedReader reader =
                     Files.newBufferedReader(path)) {

            String line;

            while ((line = reader.readLine()) != null) {

                // Empty line
                if (line.trim().isEmpty()) {

                    System.out.println(
                            "Damaged record skipped: EMPTY LINE");

                    damagedFound = true;
                    continue;
                }

                String[] fields =
                        line.split(",", -1);

                // Wrong number of fields
                if (fields.length != 7) {

                    System.out.println(
                            "Damaged record skipped: " + line);

                    damagedFound = true;
                    continue;
                }

                // Validate route number
                try {

                    Integer.parseInt(fields[3]);

                } catch (NumberFormatException e) {

                    System.out.println(
                            "Damaged record skipped: "
                            + line);

                    damagedFound = true;
                    continue;
                }

                // Validate amount
                try {

                    Double.parseDouble(fields[6]);

                } catch (NumberFormatException e) {

                    System.out.println(
                            "Damaged record skipped: "
                            + line);

                    damagedFound = true;
                }
            }

            if (!damagedFound) {

                System.out.println(
                        "No damaged records found.");
            }

        } catch (IOException e) {

            System.out.println(
                    "Error checking file: "
                    + e.getMessage());
        }
    }

    // ============================================================
    // 5. UNBUFFERED LINE COUNT
    // ============================================================
    public static long countUnbuffered(
            String fileName) throws IOException {

        long count = 0;

        try (InputStream input =
                     new FileInputStream(fileName)) {

            int data;

            while ((data = input.read()) != -1) {

                if (data == '\n') {
                    count++;
                }
            }
        }

        return count;
    }

    // ============================================================
    // 6. BUFFERED LINE COUNT
    // ============================================================
    public static long countBuffered(
            String fileName) throws IOException {

        long count = 0;

        try (InputStream input =
                     new BufferedInputStream(
                             new FileInputStream(fileName))) {

            int data;

            while ((data = input.read()) != -1) {

                if (data == '\n') {
                    count++;
                }
            }
        }

        return count;
    }

    // ============================================================
    // 7. GENERATE TEST FILE
    // ============================================================
    public static void generateTestFile(
            String fileName,
            int numberOfRecords) throws IOException {

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(fileName))) {

            for (int i = 1;
                 i <= numberOfRecords;
                 i++) {

                writer.write(
                        "P" + i + "," +
                        "REG" + i + "," +
                        "Student" + i + "," +
                        (100 + (i % 10)) + "," +
                        "Stop" + (i % 20) + "," +
                        "2026-12-31," +
                        "500.00");

                writer.newLine();
            }
        }
    }

    // ============================================================
    // 8. MEASURE SEARCH TIME
    // ============================================================
    public static long measureFindTime(
            String fileName,
            String targetId) throws IOException {

        long start = System.nanoTime();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             Paths.get(fileName))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] fields =
                        line.split(",", -1);

                if (fields.length == 7 &&
                        fields[0].equals(targetId)) {

                    break;
                }
            }
        }

        long end = System.nanoTime();

        return end - start;
    }

    // ============================================================
    // 9. MAIN PROGRAM
    // ============================================================
    public static void main(String[] args) {

        System.out.println(
                "======================================");

        System.out.println(
                "       B1 FILE STORE TEST");

        System.out.println(
                "======================================");

        try {

            // ----------------------------------------------------
            // TEST 1: CREATE SAMPLE DATA
            // ----------------------------------------------------

            String sampleFile =
                    "sample_passes.txt";

            generateTestFile(
                    sampleFile,
                    10);

            System.out.println(
                    "\n1. Test file created.");

            // ----------------------------------------------------
            // TEST 2: ADD PASS
            // ----------------------------------------------------

            addPass(
                    "P100",
                    "192472074",
                    "Gayathri",
                    101,
                    "Avadi",
                    "2026-12-31",
                    500.00);

            // ----------------------------------------------------
            // TEST 3: FIND EXISTING PASS
            // ----------------------------------------------------

            System.out.println(
                    "\n2. Finding P100:");

            findById("P100");

            // ----------------------------------------------------
            // TEST 4: FIND NONEXISTENT PASS
            // ----------------------------------------------------

            System.out.println(
                    "\n3. Finding nonexistent ID:");

            findById("P999");

            // ----------------------------------------------------
            // TEST 5: LIST ROUTE
            // ----------------------------------------------------

            System.out.println(
                    "\n4. Passes for route 101:");

            listByRoute(101);

            // ----------------------------------------------------
            // TEST 6: NONEXISTENT ROUTE
            // ----------------------------------------------------

            System.out.println(
                    "\n5. Passes for route 999:");

            listByRoute(999);

            // ----------------------------------------------------
            // TEST 7: DAMAGED RECORD
            // ----------------------------------------------------

            System.out.println(
                    "\n6. Damaged record test:");

            checkDamagedRecords(FILE_NAME);

            // ----------------------------------------------------
            // TEST 8: BUFFERING
            // ----------------------------------------------------

            System.out.println(
                    "\n7. Buffering experiment:");

            String experimentFile =
                    "buffer_test.txt";

            generateTestFile(
                    experimentFile,
                    100000);

            // Unbuffered
            long start =
                    System.nanoTime();

            long unbufferedLines =
                    countUnbuffered(
                            experimentFile);

            long unbufferedTime =
                    System.nanoTime() - start;

            // Buffered
            start =
                    System.nanoTime();

            long bufferedLines =
                    countBuffered(
                            experimentFile);

            long bufferedTime =
                    System.nanoTime() - start;

            System.out.println(
                    "Unbuffered lines: "
                    + unbufferedLines);

            System.out.println(
                    "Buffered lines: "
                    + bufferedLines);

            System.out.println(
                    "Unbuffered time: "
                    + unbufferedTime / 1_000_000.0
                    + " ms");

            System.out.println(
                    "Buffered time: "
                    + bufferedTime / 1_000_000.0
                    + " ms");

            // ----------------------------------------------------
            // TEST 9: SEARCH GROWTH
            // ----------------------------------------------------

            System.out.println(
                    "\n8. Search growth experiment:");

            int[] sizes = {
                    1000,
                    10000,
                    50000,
                    100000
            };

            for (int size : sizes) {

                String testFile =
                        "growth_" + size + ".txt";

                generateTestFile(
                        testFile,
                        size);

                String target =
                        "P" + size;

                long time =
                        measureFindTime(
                                testFile,
                                target);

                System.out.println(
                        size
                        + " records -> "
                        + time / 1_000_000.0
                        + " ms");
            }

            // ----------------------------------------------------
            // TEST 10: FILE-SIZE BUFFERING EXPERIMENT
            // ----------------------------------------------------

            System.out.println(
                    "\n9. File-size buffering experiment:");

            int[] fileSizes = {
                    10000,
                    100000,
                    500000
            };

            for (int records : fileSizes) {

                String sizeFile =
                        "TaskB1_FileStore"
                        + File.separator
                        + "size_test_"
                        + records
                        + ".txt";

                generateTestFile(
                        sizeFile,
                        records);

                File file =
                        new File(sizeFile);

                long sizeKB =
                        file.length() / 1024;

                start =
                        System.nanoTime();

                countUnbuffered(sizeFile);

                long unbuffered =
                        System.nanoTime() - start;

                start =
                        System.nanoTime();

                countBuffered(sizeFile);

                long buffered =
                        System.nanoTime() - start;

                System.out.println(
                        "Records: "
                        + records);

                System.out.println(
                        "File size: "
                        + sizeKB
                        + " KB");

                System.out.println(
                        "Unbuffered: "
                        + unbuffered / 1_000_000.0
                        + " ms");

                System.out.println(
                        "Buffered: "
                        + buffered / 1_000_000.0
                        + " ms");
            }

            // ----------------------------------------------------
            // FINAL MESSAGE
            // ----------------------------------------------------

            System.out.println(
                    "\n======================================");

            System.out.println(
                    "B1 experiment completed successfully.");

            System.out.println(
                    "======================================");

        } catch (IOException e) {

            System.out.println(
                    "\nExperiment error: "
                    + e.getMessage());
        }
    }
}