import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PassLoader {

    private static final String URL =
            "jdbc:mysql://localhost:3306/assignment5";

    private static final String USER = "root";

    private static final String PASSWORD = "gayathri";

    private static final String INSERT_SQL =
            "INSERT INTO bus_passes " +
            "(pass_id, reg_no, name, route_no, boarding_stop, " +
            "validity_date, amount_paid) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    // ---------------------------------------------------------
    // Create a sample pass
    // ---------------------------------------------------------
    private static Object[] createPass(int i) {

        return new Object[]{
                "P" + i,
                "1924" + String.format("%05d", i),
                "Student" + i,
                100 + (i % 10),
                "Stop" + (i % 20),
                Date.valueOf("2026-12-31"),
                500.00 + (i % 5) * 50.00
        };
    }

    // ---------------------------------------------------------
    // Delete all records
    // ---------------------------------------------------------
    private static void clearTable(Connection con)
            throws SQLException {

        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate("DELETE FROM bus_passes");
        }
    }

    // ---------------------------------------------------------
    // Statement insertion
    // ---------------------------------------------------------
    public static long insertUsingStatement(
            Connection con,
            int count) throws SQLException {

        clearTable(con);

        long start = System.nanoTime();

        try (Statement stmt = con.createStatement()) {

            for (int i = 1; i <= count; i++) {

                Object[] p = createPass(i);

                String sql =
                        "INSERT INTO bus_passes " +
                        "(pass_id, reg_no, name, route_no, " +
                        "boarding_stop, validity_date, amount_paid) " +
                        "VALUES ('"
                        + p[0] + "','"
                        + p[1] + "','"
                        + p[2] + "',"
                        + p[3] + ",'"
                        + p[4] + "','"
                        + p[5] + "',"
                        + p[6] + ")";

                stmt.executeUpdate(sql);
            }
        }

        return (System.nanoTime() - start) / 1_000_000;
    }

    // ---------------------------------------------------------
    // PreparedStatement insertion
    // ---------------------------------------------------------
    public static long insertUsingPreparedStatement(
            Connection con,
            int count) throws SQLException {

        clearTable(con);

        long start = System.nanoTime();

        try (PreparedStatement ps =
                     con.prepareStatement(INSERT_SQL)) {

            for (int i = 1; i <= count; i++) {

                Object[] p = createPass(i);

                ps.setString(1, (String) p[0]);
                ps.setString(2, (String) p[1]);
                ps.setString(3, (String) p[2]);
                ps.setInt(4, (Integer) p[3]);
                ps.setString(5, (String) p[4]);
                ps.setDate(6, (Date) p[5]);
                ps.setDouble(7, (Double) p[6]);

                ps.executeUpdate();
            }
        }

        return (System.nanoTime() - start) / 1_000_000;
    }

    // ---------------------------------------------------------
    // Batch insertion using PreparedStatement
    // ---------------------------------------------------------
    public static long insertUsingBatch(
            Connection con,
            int count) throws SQLException {

        clearTable(con);

        long start = System.nanoTime();

        try (PreparedStatement ps =
                     con.prepareStatement(INSERT_SQL)) {

            for (int i = 1; i <= count; i++) {

                Object[] p = createPass(i);

                ps.setString(1, (String) p[0]);
                ps.setString(2, (String) p[1]);
                ps.setString(3, (String) p[2]);
                ps.setInt(4, (Integer) p[3]);
                ps.setString(5, (String) p[4]);
                ps.setDate(6, (Date) p[5]);
                ps.setDouble(7, (Double) p[6]);

                ps.addBatch();

                // Execute in chunks to avoid a huge batch.
                if (i % 1000 == 0) {
                    ps.executeBatch();
                }
            }

            ps.executeBatch();
        }

        return (System.nanoTime() - start) / 1_000_000;
    }

    // ---------------------------------------------------------
    // ResultSetMetaData test
    // ---------------------------------------------------------
    public static void showMetadata(Connection con)
            throws SQLException {

        System.out.println("\n======================================");
        System.out.println("RESULTSET METADATA");
        System.out.println("======================================");

        String sql = "SELECT * FROM bus_passes LIMIT 1";

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta =
                    rs.getMetaData();

            int columns = meta.getColumnCount();

            System.out.println(
                    "Number of columns: " + columns);

            for (int i = 1; i <= columns; i++) {

                System.out.println(
                        i + ". "
                        + meta.getColumnName(i)
                        + " | Type: "
                        + meta.getColumnTypeName(i)
                        + " | Size: "
                        + meta.getColumnDisplaySize(i));
            }
        }
    }

    // ---------------------------------------------------------
    // Correctness test – insert 10 rows
    // ---------------------------------------------------------
    public static void correctnessTest(
            Connection con) throws SQLException {

        System.out.println("\n======================================");
        System.out.println("10-ROW CORRECTNESS TEST");
        System.out.println("======================================");

        clearTable(con);

        try (PreparedStatement ps =
                     con.prepareStatement(INSERT_SQL)) {

            for (int i = 1; i <= 10; i++) {

                Object[] p = createPass(i);

                ps.setString(1, (String) p[0]);
                ps.setString(2, (String) p[1]);
                ps.setString(3, (String) p[2]);
                ps.setInt(4, (Integer) p[3]);
                ps.setString(5, (String) p[4]);
                ps.setDate(6, (Date) p[5]);
                ps.setDouble(7, (Double) p[6]);

                ps.executeUpdate();
            }
        }

        try (Statement stmt = con.createStatement();
             ResultSet rs =
                     stmt.executeQuery(
                             "SELECT COUNT(*) FROM bus_passes")) {

            rs.next();

            int count = rs.getInt(1);

            System.out.println(
                    "Rows inserted: " + count);

            if (count == 10) {
                System.out.println(
                        "10-row correctness test: PASSED");
            } else {
                System.out.println(
                        "10-row correctness test: FAILED");
            }
        }

        showMetadata(con);
    }

    // ---------------------------------------------------------
    // Benchmark
    // ---------------------------------------------------------
    public static void benchmark(
            Connection con) throws SQLException {

        int[] sizes = {1000, 10000, 100000};

        System.out.println("\n======================================");
        System.out.println("PERFORMANCE BENCHMARK");
        System.out.println("======================================");

        System.out.printf(
                "%-12s %-18s %-20s %-15s%n",
                "Rows",
                "Statement(ms)",
                "Prepared(ms)",
                "Batch(ms)");

        System.out.println(
                "----------------------------------------------------------");

        for (int size : sizes) {

            long statementTime =
                    insertUsingStatement(con, size);

            long preparedTime =
                    insertUsingPreparedStatement(con, size);

            long batchTime =
                    insertUsingBatch(con, size);

            System.out.printf(
                    "%-12d %-18d %-20d %-15d%n",
                    size,
                    statementTime,
                    preparedTime,
                    batchTime);
        }
    }

    // ---------------------------------------------------------
    // Auto-commit vs transaction
    // ---------------------------------------------------------
    public static void transactionComparison(
            Connection con) throws SQLException {

        System.out.println("\n======================================");
        System.out.println("AUTO-COMMIT VS TRANSACTION");
        System.out.println("======================================");

        int count = 1000;

        // Auto-commit
        con.setAutoCommit(true);
        clearTable(con);

        long startAuto = System.nanoTime();

        try (PreparedStatement ps =
                     con.prepareStatement(INSERT_SQL)) {

            for (int i = 1; i <= count; i++) {

                Object[] p = createPass(i);

                ps.setString(1, (String) p[0]);
                ps.setString(2, (String) p[1]);
                ps.setString(3, (String) p[2]);
                ps.setInt(4, (Integer) p[3]);
                ps.setString(5, (String) p[4]);
                ps.setDate(6, (Date) p[5]);
                ps.setDouble(7, (Double) p[6]);

                ps.executeUpdate();
            }
        }

        long autoTime =
                (System.nanoTime() - startAuto) / 1_000_000;

        // Transaction
        con.setAutoCommit(false);
        clearTable(con);

        long startTransaction = System.nanoTime();

        try (PreparedStatement ps =
                     con.prepareStatement(INSERT_SQL)) {

            for (int i = 1; i <= count; i++) {

                Object[] p = createPass(i);

                ps.setString(1, (String) p[0]);
                ps.setString(2, (String) p[1]);
                ps.setString(3, (String) p[2]);
                ps.setInt(4, (Integer) p[3]);
                ps.setString(5, (String) p[4]);
                ps.setDate(6, (Date) p[5]);
                ps.setDouble(7, (Double) p[6]);

                ps.executeUpdate();
            }

            con.commit();

        } catch (SQLException e) {

            con.rollback();
            throw e;

        } finally {

            con.setAutoCommit(true);
        }

        long transactionTime =
                (System.nanoTime() - startTransaction) / 1_000_000;

        System.out.println(
                "Auto-commit time: "
                + autoTime + " ms");

        System.out.println(
                "Transaction time: "
                + transactionTime + " ms");
    }

    // ---------------------------------------------------------
    // Forced failure and rollback
    // ---------------------------------------------------------
    public static void rollbackTest(
            Connection con) throws SQLException {

        System.out.println("\n======================================");
        System.out.println("FORCED FAILURE / ROLLBACK TEST");
        System.out.println("======================================");

        clearTable(con);

        con.setAutoCommit(false);

        try (PreparedStatement ps =
                     con.prepareStatement(INSERT_SQL)) {

            for (int i = 1; i <= 20; i++) {

                if (i == 11) {

                    System.out.println(
                            "Forcing failure at row 11...");

                    throw new SQLException(
                            "Invalid amount supplied at row 11");
                }

                Object[] p = createPass(i);

                ps.setString(1, (String) p[0]);
                ps.setString(2, (String) p[1]);
                ps.setString(3, (String) p[2]);
                ps.setInt(4, (Integer) p[3]);
                ps.setString(5, (String) p[4]);
                ps.setDate(6, (Date) p[5]);
                ps.setDouble(7, (Double) p[6]);

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

        try (Statement stmt = con.createStatement();
             ResultSet rs =
                     stmt.executeQuery(
                             "SELECT COUNT(*) FROM bus_passes")) {

            rs.next();

            int count = rs.getInt(1);

            System.out.println(
                    "Rows remaining after rollback: "
                    + count);

            if (count == 0) {
                System.out.println(
                        "ROLLBACK TEST: PASSED");
            } else {
                System.out.println(
                        "ROLLBACK TEST: FAILED");
            }
        }
    }

    // ---------------------------------------------------------
    // Display sample records
    // ---------------------------------------------------------
    public static void displayRecords(
            Connection con) throws SQLException {

        System.out.println("\n======================================");
        System.out.println("SAMPLE RECORDS");
        System.out.println("======================================");

        String sql =
                "SELECT * FROM bus_passes LIMIT 10";

        try (Statement stmt = con.createStatement();
             ResultSet rs =
                     stmt.executeQuery(sql)) {

            while (rs.next()) {

                System.out.println(
                        rs.getString("pass_id")
                        + " | "
                        + rs.getString("reg_no")
                        + " | "
                        + rs.getString("name")
                        + " | Route "
                        + rs.getInt("route_no")
                        + " | "
                        + rs.getString("boarding_stop")
                        + " | "
                        + rs.getDate("validity_date")
                        + " | ₹"
                        + rs.getDouble("amount_paid"));
            }
        }
    }

    // ---------------------------------------------------------
    // Main
    // ---------------------------------------------------------
    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("       B3 JDBC EXPERIMENT");
        System.out.println("======================================");

        try {

            Class.forName(
                    "com.mysql.cj.jdbc.Driver");

            try (Connection con =
                         DriverManager.getConnection(
                                 URL,
                                 USER,
                                 PASSWORD)) {

                System.out.println(
                        "MySQL connection: SUCCESS");

                System.out.println(
                        "Database: assignment5");

                correctnessTest(con);

                benchmark(con);

                transactionComparison(con);

                rollbackTest(con);

                System.out.println("\n======================================");
                System.out.println(
                        "B3 EXPERIMENT COMPLETED SUCCESSFULLY");
                System.out.println("======================================");
            }

        } catch (ClassNotFoundException e) {

            System.out.println(
                    "MySQL JDBC driver not found.");

            System.out.println(
                    "Check the Connector/J JAR.");

        } catch (SQLException e) {

            System.out.println(
                    "Database error: "
                    + e.getMessage());

            e.printStackTrace();
        }
    }
}