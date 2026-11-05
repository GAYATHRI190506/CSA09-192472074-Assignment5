import java.sql.*;
import java.io.*;
import java.util.*;

public class B4_Migration {

    private static final String FILE_NAME =
            "TaskB1_FileStore" + File.separator + "bus_passes.txt";

    private static final String URL =
            "jdbc:mysql://localhost:3306/assignment5";

    private static final String USER = "root";

    private static final String PASSWORD = "gayathri";

    private static final String INSERT_SQL =
            "INSERT INTO bus_passes " +
            "(pass_id, reg_no, name, route_no, boarding_stop, " +
            "validity_date, amount_paid) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("       B4 FILE TO DATABASE MIGRATION");
        System.out.println("======================================");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con =
                         DriverManager.getConnection(
                                 URL, USER, PASSWORD)) {

                System.out.println("MySQL connection: SUCCESS");
                System.out.println("Database: assignment5");

                migrateFileToDatabase(con);

                rollbackFailureTest(con);

                showDatabaseMetadata(con);

                System.out.println("\n======================================");
                System.out.println(
                        "B4 MIGRATION COMPLETED SUCCESSFULLY");
                System.out.println("======================================");
            }

        } catch (ClassNotFoundException e) {

            System.out.println("MySQL JDBC driver not found.");
            System.out.println("Check the Connector/J JAR.");

        } catch (SQLException e) {

            System.out.println(
                    "Database error: " + e.getMessage());
        }
    }

    private static void migrateFileToDatabase(
            Connection con) throws SQLException {

        System.out.println("\n======================================");
        System.out.println("FILE TO DATABASE MIGRATION");
        System.out.println("======================================");

        File file = new File(FILE_NAME);

        if (!file.exists()) {
            System.out.println(
                    "Migration source file not found: "
                    + FILE_NAME);
            return;
        }

        try {
            con.setAutoCommit(false);

            try (BufferedReader reader =
                         new BufferedReader(
                                 new FileReader(file));
                 PreparedStatement ps =
                         con.prepareStatement(INSERT_SQL)) {

                String line;
                int rowCount = 0;
                int skipped = 0;

                while ((line = reader.readLine()) != null) {

                    if (line.trim().isEmpty()) {
                        skipped++;
                        continue;
                    }

                    String[] fields = line.split(",", -1);

                    if (fields.length != 7) {
                        System.out.println(
                                "Skipping invalid record: "
                                + line);
                        skipped++;
                        continue;
                    }

                    try {
                        ps.setString(1, fields[0]);
                        ps.setString(2, fields[1]);
                        ps.setString(3, fields[2]);
                        ps.setInt(4, Integer.parseInt(fields[3]));
                        ps.setString(5, fields[4]);
                        ps.setDate(
                                6,
                                java.sql.Date.valueOf(fields[5]));
                        ps.setDouble(
                                7,
                                Double.parseDouble(fields[6]));

                        ps.addBatch();
                        rowCount++;

                        if (rowCount % 1000 == 0) {
                            ps.executeBatch();

                            System.out.println(
                                    "Migrated records: "
                                    + rowCount);
                        }

                    } catch (Exception e) {

                        System.out.println(
                                "Invalid record skipped: "
                                + line);

                        skipped++;
                    }
                }

                ps.executeBatch();

                con.commit();

                System.out.println(
                        "\nMigration committed successfully.");

                System.out.println(
                        "Records migrated: " + rowCount);

                System.out.println(
                        "Records skipped: " + skipped);

            } catch (Exception e) {

                System.out.println(
                        "\nMigration failed: "
                        + e.getMessage());

                System.out.println(
                        "Rolling back migration...");

                con.rollback();

                System.out.println(
                        "Rollback completed.");

            } finally {

                con.setAutoCommit(true);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Transaction error: "
                    + e.getMessage());

            throw e;
        }
    }

    private static void rollbackFailureTest(
            Connection con) throws SQLException {

        System.out.println("\n======================================");
        System.out.println("MIGRATION FAILURE / ROLLBACK TEST");
        System.out.println("======================================");

        int beforeCount = 0;

        try (Statement stmt = con.createStatement();
             ResultSet rs =
                     stmt.executeQuery(
                             "SELECT COUNT(*) FROM bus_passes")) {

            rs.next();

            beforeCount = rs.getInt(1);
        }

        System.out.println(
                "Records before failure test: "
                + beforeCount);

        con.setAutoCommit(false);

        try (PreparedStatement ps =
                     con.prepareStatement(INSERT_SQL)) {

            for (int i = 11; i <= 20; i++) {

                if (i == 16) {

                    System.out.println(
                            "Forcing failure at record "
                            + i);

                    throw new SQLException(
                            "Invalid amount supplied during migration");
                }

                ps.setString(
                        1,
                        "FAIL" + i);

                ps.setString(
                        2,
                        "1924"
                        + String.format("%05d", i));

                ps.setString(
                        3,
                        "FailureTest" + i);

                ps.setInt(
                        4,
                        100 + (i % 10));

                ps.setString(
                        5,
                        "TestStop" + i);

                ps.setDate(
                        6,
                        java.sql.Date.valueOf(
                                "2026-12-31"));

                ps.setDouble(
                        7,
                        500.00);

                ps.executeUpdate();
            }

            con.commit();

        } catch (SQLException e) {

            System.out.println(
                    "Failure detected: "
                    + e.getMessage());

            System.out.println(
                    "Rolling back transaction...");

            con.rollback();

            System.out.println(
                    "Rollback completed.");

        } finally {

            con.setAutoCommit(true);
        }

        int afterCount = 0;

        try (Statement stmt = con.createStatement();
             ResultSet rs =
                     stmt.executeQuery(
                             "SELECT COUNT(*) FROM bus_passes")) {

            rs.next();

            afterCount = rs.getInt(1);
        }

        System.out.println(
                "Records after rollback: "
                + afterCount);

        if (afterCount == beforeCount) {

            System.out.println(
                    "ROLLBACK TEST: PASSED");

            System.out.println(
                    "No partial records remained.");

        } else {

            System.out.println(
                    "ROLLBACK TEST: FAILED");
        }
    }

    private static void showDatabaseMetadata(
            Connection con) throws SQLException {

        System.out.println("\n======================================");
        System.out.println("DATABASE METADATA");
        System.out.println("======================================");

        DatabaseMetaData meta =
                con.getMetaData();

        System.out.println(
                "Database Product: "
                + meta.getDatabaseProductName());

        System.out.println(
                "Database Version: "
                + meta.getDatabaseProductVersion());

        System.out.println(
                "JDBC Driver: "
                + meta.getDriverName());

        System.out.println(
                "JDBC Driver Version: "
                + meta.getDriverVersion());

        System.out.println("\nTables:");

        try (ResultSet tables =
                     meta.getTables(
                             "assignment5",
                             null,
                             "%",
                             new String[]{"TABLE"})) {

            while (tables.next()) {

                System.out.println(
                        "- "
                        + tables.getString(
                                "TABLE_NAME"));
            }
        }
    }
}
