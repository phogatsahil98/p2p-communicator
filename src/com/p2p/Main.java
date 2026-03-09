package com.p2p;

import com.p2p.crypto.CryptoUtils;
import com.p2p.network.PeerNode;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        PeerNode node = new PeerNode();

        try {
            System.out.println("=== SECURE P2P COMMUNICATOR ===");
            System.out.println("1. Host a chat (Wait for connection)");
            System.out.println("2. Connect to a peer");
            System.out.print("Choose option (1 or 2): ");
            int choice = Integer.parseInt(scanner.nextLine());

            SecretKey aesKey = null;

            if (choice == 1) {
                System.out.print("Enter port to listen on (e.g., 8080): ");
                int port = Integer.parseInt(scanner.nextLine());

                // Generate AES Key for the session
                aesKey = CryptoUtils.generateAESKey();

                // Convert raw key bytes into readable text to share with the peer
                String encodedKey = Base64.getEncoder().encodeToString(aesKey.getEncoded());
                System.out.println("\n[IMPORTANT] Share this Session Key securely with your peer:");
                System.out.println(encodedKey + "\n");

                node.startHost(port, aesKey);

            } else if (choice == 2) {
                System.out.print("Enter peer IP address (e.g., 127.0.0.1): ");
                String ip = scanner.nextLine();
                System.out.print("Enter peer port (e.g., 8080): ");
                int port = Integer.parseInt(scanner.nextLine());

                System.out.print("Enter the Session Key provided by the host: ");
                String keyString = scanner.nextLine();

                // Reconstruct the AES key from the text provided by the host
                byte[] decodedKey = Base64.getDecoder().decode(keyString);
                aesKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

                node.connectToPeer(ip, port, aesKey);
            } else {
                System.out.println("Invalid choice. Exiting.");
                return;
            }

            // The Main Thread Chat Loop
            System.out.println("\n[System]: Chat started! Type your messages below. Type '/exit' to quit.");
            while (true) {
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("/exit")) {
                    System.out.println("Exiting chat...");
                    System.exit(0);
                }
                node.sendMessage(message);
            }

        } catch (Exception e) {
            System.out.println("[Error]: " + e.getMessage());
            e.printStackTrace();
        }
    }
}