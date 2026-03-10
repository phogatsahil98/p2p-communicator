package com.p2p.network;

import com.p2p.crypto.CryptoUtils;
import com.p2p.ui.ChatInterface;
import com.p2p.db.DatabaseManager;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.File;
import java.io.FileOutputStream;

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

                // Check if the message is actually a file payload
                if (decryptedMessage.startsWith("[FILE]:")) {
                    String[] parts = decryptedMessage.split(":", 3);
                    String fileName = parts[1];
                    String encryptedFileData = parts[2];

                    ChatInterface.showSystem("Receiving secure file: " + fileName + "...");

                    // Decrypt the Base64 file data back into raw bytes
                    byte[] fileBytes = CryptoUtils.decryptFile(encryptedFileData, secretKey);

                    // Create a downloads directory if it doesn't exist
                    File downloadDir = new File("downloads");
                    if (!downloadDir.exists()) downloadDir.mkdir();

                    // Save the file
                    File outputFile = new File(downloadDir, "secure_" + fileName);
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        fos.write(fileBytes);
                    }

                    ChatInterface.showSystem("File saved securely to: " + outputFile.getAbsolutePath());
                    DatabaseManager.saveMessage("Peer", "[Sent a file: " + fileName + "]");

                } else {
                    // Standard text message
                    ChatInterface.showPeer(decryptedMessage);
                    DatabaseManager.saveMessage("Peer", decryptedMessage);
                }
            }
        } catch (Exception e) {
            ChatInterface.showError("Connection lost.");
        }
    }
}