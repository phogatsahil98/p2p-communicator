package com.p2p;

import com.p2p.network.PeerNode;
import com.p2p.db.DatabaseManager;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Initialize DB and print history on startup
        DatabaseManager.initializeDatabase();
        DatabaseManager.printHistory();

        Scanner scanner = new Scanner(System.in);
        PeerNode node = new PeerNode();

        try {
            System.out.println("=== SECURE P2P COMMUNICATOR (v3.0 - Stealth Mode) ===");
            System.out.println("1. Host a chat (Wait for connection)");
            System.out.println("2. Connect to a peer");
            System.out.print("Choose option (1 or 2): ");
            int choice = Integer.parseInt(scanner.nextLine());

            // Connection Logic
            if (choice == 1) {
                System.out.print("Enter port to listen on (e.g., 8080): ");
                int port = Integer.parseInt(scanner.nextLine());
                node.startHost(port);
            } else if (choice == 2) {
                System.out.print("Enter peer IP address (e.g., 127.0.0.1): ");
                String ip = scanner.nextLine();
                System.out.print("Enter peer port (e.g., 8080): ");
                int port = Integer.parseInt(scanner.nextLine());
                node.connectToPeer(ip, port);
            } else {
                System.out.println("Invalid choice. Exiting.");
                return;
            }

            // Chat Loop
            while (true) {
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase("/exit")) {
                    System.out.println("Exiting chat...");
                    System.exit(0);
                } else if (message.equalsIgnoreCase("/clear")) {
                    // --- NEW: Clear the database ---
                    DatabaseManager.clearHistory();
                    continue;
                } else if (message.startsWith("/send ")) {
                    String filePath = message.substring(6).trim();
                    node.sendFile(filePath);
                    DatabaseManager.saveMessage("You", "[Sent a file: " + filePath + "]");
                    continue;
                }

                node.sendMessage(message);
                DatabaseManager.saveMessage("You", message);
            }

        } catch (Exception e) {
            System.out.println("[Error]: " + e.getMessage());
            e.printStackTrace();
        }
    }
}