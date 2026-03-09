package com.p2p.network;

import com.p2p.crypto.CryptoUtils;
import com.p2p.ui.ChatInterface;
import com.p2p.db.DatabaseManager;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ConnectionThread extends Thread {
    private Socket socket;
    private SecretKey secretKey;

    public ConnectionThread(Socket socket, SecretKey secretKey) {
        this.socket = socket;
        this.secretKey = secretKey;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String encryptedMessage;

            while ((encryptedMessage = in.readLine()) != null) {
                String decryptedMessage = CryptoUtils.decryptMessage(encryptedMessage, secretKey);
                ChatInterface.showPeer(decryptedMessage);

                // --- NEW: Save incoming message to SQLite ---
                DatabaseManager.saveMessage("Peer", decryptedMessage);
            }
        } catch (Exception e) {
            ChatInterface.showError("Connection lost.");
        }
    }
}