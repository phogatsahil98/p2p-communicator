package com.p2p.network;

import com.p2p.crypto.CryptoUtils;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ConnectionThread extends Thread {
    private Socket socket;
    private SecretKey secretKey;

    // Constructor to pass in the connected socket and the shared AES key
    public ConnectionThread(Socket socket, SecretKey secretKey) {
        this.socket = socket;
        this.secretKey = secretKey;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String encryptedMessage;

            // Continuously listen for incoming lines of text
            while ((encryptedMessage = in.readLine()) != null) {
                // Decrypt the message using our CryptoUtils class
                String decryptedMessage = CryptoUtils.decryptMessage(encryptedMessage, secretKey);

                // Print the message and reset the typing prompt for the user
                System.out.println("\n[Peer]: " + decryptedMessage);
                System.out.print("[You]: ");
            }
        } catch (Exception e) {
            System.out.println("\n[System]: Peer disconnected or a network error occurred.");
        }
    }
}