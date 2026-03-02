package com.studentgui.db;

import com.github.f4b6a3.uuid.UuidCreator;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.*;

public class DatabaseHelper {

    private static final String[] SUBJECTS = {"Science", "Social", "Maths", "English", "Hindi", "Kannada"};
    private static final Map<String, Integer> SUBJ_MAP = new LinkedHashMap<>();
    static {
        SUBJ_MAP.put("Science", 101);
        SUBJ_MAP.put("Social",  102);
        SUBJ_MAP.put("Maths",   103);
        SUBJ_MAP.put("English", 104);
        SUBJ_MAP.put("Hindi",   105);
        SUBJ_MAP.put("Kannada", 106);
    }

    private final String url;
    private final String user;
    private final String password;

    public DatabaseHelper() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String host = dotenv.get("DB_HOST", "localhost");
        String port = dotenv.get("DB_PORT", "3306");
        String db   = dotenv.get("DB_NAME", "defaultdb");
        this.user     = dotenv.get("DB_USER", "root");
        this.password = dotenv.get("DB_PASS", "");
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=true&serverTimezone=UTC";
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Save student marks. Uses UUID v7 (time-ordered) for the MARKS.ID column.
     */
    public void saveStudentMarks(String name, int rollNo, Map<String, Integer> marksMap) throws SQLException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                // Upsert student
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO STUDENTS (ROLL_NO, NAME) VALUES (?, ?) ON DUPLICATE KEY UPDATE NAME=?")) {
                    ps.setInt(1, rollNo);
                    ps.setString(2, name);
                    ps.setString(3, name);
                    ps.executeUpdate();
                }

                // Upsert marks per subject
                for (Map.Entry<String, Integer> entry : marksMap.entrySet()) {
                    Integer subjId = SUBJ_MAP.get(entry.getKey());
                    if (subjId == null) continue;

                    // Delete old mark for this student/subject then insert fresh with UUID v7
                    try (PreparedStatement del = conn.prepareStatement(
                            "DELETE FROM MARKS WHERE ROLL_NO=? AND SUBJ_ID=?")) {
                        del.setInt(1, rollNo);
                        del.setInt(2, subjId);
                        del.executeUpdate();
                    }

                    // UUID v7 — time-ordered, sequential, globally unique
                    String uuid7 = UuidCreator.getTimeOrderedWithRandom().toString();

                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO MARKS (ID, ROLL_NO, SUBJ_ID, MARKS) VALUES (?, ?, ?, ?)")) {
                        ins.setString(1, uuid7);
                        ins.setInt(2, rollNo);
                        ins.setInt(3, subjId);
                        ins.setInt(4, entry.getValue());
                        ins.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Fetch all student records as a pivoted list of maps.
     * Returns null on connection failure.
     */
    public List<Map<String, Object>> getAllRecords() throws SQLException {
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
            ORDER BY s.ROLL_NO
            """;

        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= cols; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                results.add(row);
            }
        }
        return results;
    }

    public void deleteStudent(int rollNo) throws SQLException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM MARKS WHERE ROLL_NO=?")) {
                    ps.setInt(1, rollNo);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM STUDENTS WHERE ROLL_NO=?")) {
                    ps.setInt(1, rollNo);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static String[] getSubjects() {
        return SUBJECTS;
    }
}
