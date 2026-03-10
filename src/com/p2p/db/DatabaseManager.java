package com.p2p.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:chat_history.db";

    // --- NEW: Explicitly load the SQLite driver into memory ---
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("[Error]: SQLite driver not found. Check your classpath.");
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sender TEXT NOT NULL," +
                    "message TEXT NOT NULL," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            stmt.execute(sql);
        } catch (Exception e) {
            System.out.println("[Error]: Database initialization failed: " + e.getMessage());
        }
    }

    public static void saveMessage(String sender, String message) {
        String sql = "INSERT INTO messages(sender, message) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sender);
            pstmt.setString(2, message);
            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("[Error]: Failed to save message to history.");
        }
    }

    public static void printHistory() {
        String sql = "SELECT sender, message, timestamp FROM messages ORDER BY id ASC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=== LOCAL CHAT HISTORY ===");
            while (rs.next()) {
                System.out.println("[" + rs.getString("timestamp") + "] " +
                        rs.getString("sender") + ": " +
                        rs.getString("message"));
            }
            System.out.println("==========================\n");

        } catch (Exception e) {
            System.out.println("[Error]: Failed to load history.");
        }
    }

    public static void clearHistory() {
        String sql = "DELETE FROM messages";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("[System]: Local chat history has been wiped.");

        } catch (Exception e) {
            System.out.println("[Error]: Failed to clear history: " + e.getMessage());
        }
    }
}