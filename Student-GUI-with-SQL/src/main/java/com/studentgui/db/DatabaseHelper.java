package com.studentgui.db;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.*;
import java.util.*;

public class DatabaseHelper {
    private static final Map<String, Integer> SUBJ_MAP = new HashMap<>();

    static {
        SUBJ_MAP.put("Science", 101);
        SUBJ_MAP.put("Social", 102);
        SUBJ_MAP.put("Maths", 103);
        SUBJ_MAP.put("English", 104);
        SUBJ_MAP.put("Hindi", 105);
        SUBJ_MAP.put("Kannada", 106);
    }

    // We reverse map for easy lookup if needed, but not strictly required for this
    // logic

    private String host;
    private int port;
    private String user;
    private String password;
    private String database;

    public DatabaseHelper() {
        // Load .env
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.host = dotenv.get("DB_HOST");
        this.port = Integer.parseInt(dotenv.get("DB_PORT", "3306"));
        this.user = dotenv.get("DB_USER");
        this.password = dotenv.get("DB_PASS");
        this.database = dotenv.get("DB_NAME");
    }

    public Connection connect() throws SQLException {
        if (host == null || user == null) {
            throw new SQLException("Database credentials not configured in .env");
        }
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true", host, port,
                database);
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Save or update student marks.
     * 
     * @param name      Student name
     * @param rollNo    Roll number
     * @param marksDict Map of Subject Name -> Marks (Integer)
     * @throws SQLException if db error occurs
     */
    public void saveStudentMarks(String name, int rollNo, Map<String, Integer> marksDict) throws SQLException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                // Insert or Update Student
                String sqlStudent = "INSERT INTO STUDENTS (ROLL_NO, NAME) VALUES (?, ?) ON DUPLICATE KEY UPDATE NAME=?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlStudent)) {
                    pstmt.setInt(1, rollNo);
                    pstmt.setString(2, name);
                    pstmt.setString(3, name);
                    pstmt.executeUpdate();
                }

                // Insert Marks
                for (Map.Entry<String, Integer> entry : marksDict.entrySet()) {
                    String subName = entry.getKey();
                    Integer marks = entry.getValue();
                    if (marks == null)
                        continue;

                    Integer subId = SUBJ_MAP.get(subName);
                    if (subId != null) {
                        // Delete old marks
                        String sqlDelete = "DELETE FROM MARKS WHERE ROLL_NO=? AND SUBJ_ID=?";
                        try (PreparedStatement del = conn.prepareStatement(sqlDelete)) {
                            del.setInt(1, rollNo);
                            del.setInt(2, subId);
                            del.executeUpdate();
                        }

                        // Insert new marks
                        String sqlInsert = "INSERT INTO MARKS (ID, ROLL_NO, SUBJ_ID, MARKS) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement ins = conn.prepareStatement(sqlInsert)) {
                            ins.setString(1, UUID.randomUUID().toString());
                            ins.setInt(2, rollNo);
                            ins.setInt(3, subId);
                            ins.setInt(4, marks);
                            ins.executeUpdate();
                        }
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Map<String, Object>> getAllRecords() throws SQLException {
        List<Map<String, Object>> records = new ArrayList<>();
        String query = """
                SELECT s.ROLL_NO, s.NAME,
                    MAX(CASE WHEN m.SUBJ_ID = 101 THEN m.MARKS END) AS Science,
                    MAX(CASE WHEN m.SUBJ_ID = 102 THEN m.MARKS END) AS Social,
                    MAX(CASE WHEN m.SUBJ_ID = 103 THEN m.MARKS END) AS Maths,
                    MAX(CASE WHEN m.SUBJ_ID = 104 THEN m.MARKS END) AS English,
                    MAX(CASE WHEN m.SUBJ_ID = 105 THEN m.MARKS END) AS Hindi,
                    MAX(CASE WHEN m.SUBJ_ID = 106 THEN m.MARKS END) AS Kannada
                FROM STUDENTS s
                LEFT JOIN MARKS m ON s.ROLL_NO = m.ROLL_NO
                GROUP BY s.ROLL_NO, s.NAME
                """;

        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    String colName = meta.getColumnLabel(i);
                    Object val = rs.getObject(i);
                    row.put(colName, val);
                }
                records.add(row);
            }
        }
        return records;
    }

    public void deleteStudent(int rollNo) throws SQLException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM MARKS WHERE ROLL_NO=?")) {
                    ps1.setInt(1, rollNo);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM STUDENTS WHERE ROLL_NO=?")) {
                    ps2.setInt(1, rollNo);
                    ps2.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
