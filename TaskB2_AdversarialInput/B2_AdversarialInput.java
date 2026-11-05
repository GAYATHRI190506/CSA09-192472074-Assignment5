import java.io.*;
import java.nio.file.*;
import java.sql.*;

public class B2_AdversarialInput {

    // ============================================================
    // DATABASE CONNECTION
    // ============================================================

    private static final String URL =
            "jdbc:mysql://localhost:3306/assignment5";

    private static final String USER = "root";

    private static final String PASSWORD = "";

    // ============================================================
    // 1. UNSAFE LOGIN - SQL INJECTION VULNERABLE
    // ============================================================

    public static boolean unsafeLogin(
            String username,
            String password) {

        String sql =
                "SELECT * FROM users WHERE username = '"
                + username
                + "' AND password = '"
                + password
                + "'";

        System.out.println("\nUnsafe SQL:");
        System.out.println(sql);

        try (Connection con =
                     DriverManager.getConnection(
                             URL, USER, PASSWORD);

             Statement stmt =
                     con.createStatement();

             ResultSet rs =
                     stmt.executeQuery(sql)) {

            return rs.next();

        } catch (SQLException e) {

            System.out.println(
                    "Unsafe login error: "
                    + e.getMessage());

            return false;
        }
    }

    // ============================================================
    // 2. SAFE LOGIN - PREPARED STATEMENT
    // ============================================================

    public static boolean safeLogin(
            String username,
            String password) {

        String sql =
                "SELECT * FROM users "
                + "WHERE username = ? AND password = ?";

        System.out.println("\nSafe SQL uses placeholders:");
        System.out.println(sql);

        try (Connection con =
                     DriverManager.getConnection(
                             URL, USER, PASSWORD);

             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs =
                         ps.executeQuery()) {

                return rs.next();
            }

        } catch (SQLException e) {

            System.out.println(
                    "Safe login error: "
                    + e.getMessage());

            return false;
        }
    }

    // ============================================================
    // 3. DAMAGED BUS-PASS FILE TEST
    // ============================================================

    public static void checkDamagedBusPassFile(
            String fileName) {

        System.out.println(
                "\n======================================");

        System.out.println(
                "DAMAGED BUS-PASS FILE TEST");

        System.out.println(
                "======================================");

        Path path =
                Paths.get(fileName);

        if (!Files.exists(path)) {

            System.out.println(
                    "File does not exist.");

            return;
        }

        boolean damagedFound = false;

        try (BufferedReader reader =
                     Files.newBufferedReader(path)) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {

                lineNumber++;

                // Empty line
                if (line.trim().isEmpty()) {

                    System.out.println(
                            "Line "
                            + lineNumber
                            + ": EMPTY LINE");

                    damagedFound = true;
                    continue;
                }

                String[] fields =
                        line.split(",", -1);

                // Wrong number of fields
                if (fields.length != 7) {

                    System.out.println(
                            "Line "
                            + lineNumber
                            + ": WRONG FIELD COUNT -> "
                            + line);

                    damagedFound = true;
                    continue;
                }

                // Check route number
                try {

                    Integer.parseInt(fields[3]);

                } catch (NumberFormatException e) {

                    System.out.println(
                            "Line "
                            + lineNumber
                            + ": INVALID ROUTE -> "
                            + line);

                    damagedFound = true;
                }

                // Check amount
                try {

                    Double.parseDouble(fields[6]);

                } catch (NumberFormatException e) {

                    System.out.println(
                            "Line "
                            + lineNumber
                            + ": NON-NUMERIC AMOUNT -> "
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
                    "File error: "
                    + e.getMessage());
        }
    }

    // ============================================================
    // 4. CREATE ADVERSARIAL TEST FILE
    // ============================================================

    public static void createDamagedFile(
            String fileName) throws IOException {

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             Paths.get(fileName))) {

            // Valid record
            writer.write(
                    "P200,192472074,Gayathri,101,"
                    + "Avadi,2026-12-31,500.00");

            writer.newLine();

            // Wrong number of fields
            writer.write(
                    "BAD001,192400001,WrongRecord");

            writer.newLine();

            // Non-numeric amount
            writer.write(
                    "BAD002,192400002,Student2,102,"
                    + "Chennai,2026-12-31,ABC");

            writer.newLine();

            // Empty line
            writer.newLine();

            // Invalid route
            writer.write(
                    "BAD003,192400003,Student3,ABC,"
                    + "Chennai,2026-12-31,500.00");

            writer.newLine();
        }
    }

    // ============================================================
    // MAIN
    // ============================================================

    public static void main(String[] args) {

        System.out.println(
                "======================================");

        System.out.println(
                "       B2 ADVERSARIAL INPUT TEST");

        System.out.println(
                "======================================");

        // --------------------------------------------------------
        // TEST 1: SQL INJECTION INPUT
        // --------------------------------------------------------

        String maliciousUsername =
                "' OR '1'='1";

        String maliciousPassword =
                "' OR '1'='1";

        System.out.println(
                "\n1. Crafted SQL injection input:");

        System.out.println(
                "Username: "
                + maliciousUsername);

        System.out.println(
                "Password: "
                + maliciousPassword);

        System.out.println(
                "\nThe unsafe version concatenates "
                + "user input directly into SQL.");

        // --------------------------------------------------------
        // TEST 2: UNSAFE LOGIN
        // --------------------------------------------------------

        System.out.println(
                "\n2. Unsafe login test:");

        unsafeLogin(
                maliciousUsername,
                maliciousPassword);

        // --------------------------------------------------------
        // TEST 3: SAFE LOGIN
        // --------------------------------------------------------

        System.out.println(
                "\n3. Safe login test:");

        safeLogin(
                maliciousUsername,
                maliciousPassword);

        // --------------------------------------------------------
        // TEST 4: DAMAGED FILE
        // --------------------------------------------------------

        try {

            String damagedFile =
                    "TaskB2_AdversarialInput"
                    + File.separator
                    + "damaged_bus_passes.txt";

            createDamagedFile(
                    damagedFile);

            System.out.println(
                    "\n4. Damaged bus-pass file created.");

            checkDamagedBusPassFile(
                    damagedFile);

        } catch (IOException e) {

            System.out.println(
                    "Error creating damaged file: "
                    + e.getMessage());
        }

        // --------------------------------------------------------
        // FINAL FINDINGS
        // --------------------------------------------------------

        System.out.println(
                "\n======================================");

        System.out.println(
                "B2 FINDINGS");

        System.out.println(
                "======================================");

        System.out.println(
                "1. SQL concatenation can allow "
                + "SQL injection.");

        System.out.println(
                "2. PreparedStatement treats input "
                + "as data instead of SQL.");

        System.out.println(
                "3. Wrong field counts are detected.");

        System.out.println(
                "4. Non-numeric amounts are detected.");

        System.out.println(
                "5. Empty records are detected.");

        System.out.println(
                "6. Invalid route numbers are detected.");

        System.out.println(
                "7. Escaping/regex can provide "
                + "additional input validation.");

        System.out.println(
                "\nB2 experiment completed successfully.");

        System.out.println(
                "======================================");
    }
}