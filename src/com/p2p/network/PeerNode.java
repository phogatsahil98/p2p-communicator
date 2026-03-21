package com.p2p.network;

import com.p2p.crypto.CryptoUtils;
import com.p2p.ui.ChatInterface;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.security.KeyPair;

public class PeerNode {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private SecretKey secretKey;
    private KeyPair myKeyPair;
    private boolean isHost = false;
    private boolean isGroupChat = false;

    public boolean isHost() { return isHost; }
    public boolean isGroupChat() { return isGroupChat; }
    public SecretKey getSecretKey() { return secretKey; }
    public void setSecretKey(SecretKey key) { this.secretKey = key; }
    public KeyPair getMyKeyPair() { return myKeyPair; }
    public PrintWriter getOut() { return out; }

    public void startHost(int port) throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        ChatInterface.printToScreen("[System]: Waiting for a peer on port " + port + "...");
        this.socket = serverSocket.accept();
        ChatInterface.printToScreen("[System]: Peer connected!");
        setupStreams();
        performLocalHandshake(true);
        new ConnectionThread(socket, this).start();
    }

    public void connectToPeer(String ip, int port) throws Exception {
        ChatInterface.printToScreen("[System]: Connecting to " + ip + ":" + port + "...");
        this.socket = new Socket(ip, port);
        ChatInterface.printToScreen("[System]: Connected!");
        setupStreams();
        performLocalHandshake(false);
        new ConnectionThread(socket, this).start();
    }

    public void connectToRelay(String relayIp, int port, boolean isHost, boolean isGroup) throws Exception {
        ChatInterface.printToScreen("[System]: Connecting to AWS Relay at " + relayIp + ":" + port + "...");
        this.socket = new Socket(relayIp, port);
        setupStreams();
        this.isHost = isHost;
        this.isGroupChat = isGroup;

        if (isGroup) {
            if (isHost) {
                this.secretKey = CryptoUtils.generateAESKey();
                ChatInterface.printToScreen("[System]: Group Admin initialized. Waiting for peers...");
            } else {
                this.myKeyPair = CryptoUtils.generateRSAKeyPair();
                out.println("[KEY_REQUEST]:" + CryptoUtils.encodePublicKey(myKeyPair.getPublic()));
                ChatInterface.printToScreen("[System]: Sent secure entry request. Waiting for Room Admin...");
            }
            new ConnectionThread(socket, this).start();
        } else {
            ChatInterface.printToScreen("[System]: Connected to Relay. Waiting for 1-to-1 peer...");
            performLocalHandshake(isHost);
            new ConnectionThread(socket, this).start();
        }
    }

    private void setupStreams() throws IOException {
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void performLocalHandshake(boolean isHost) throws Exception {
        if (isHost) {
            KeyPair keyPair = CryptoUtils.generateRSAKeyPair();
            out.println(CryptoUtils.encodePublicKey(keyPair.getPublic()));
            String encryptedAesKey = in.readLine();
            secretKey = CryptoUtils.decryptAESKey(encryptedAesKey, keyPair.getPrivate());
        } else {
            String publicKeyStr = in.readLine();
            secretKey = CryptoUtils.generateAESKey();
            out.println(CryptoUtils.encryptAESKey(secretKey, CryptoUtils.decodePublicKey(publicKeyStr)));
        }
        ChatInterface.printToScreen("[System]: RSA-AES Handshake complete. Connection secure.");
    }

    public void sendMessage(String message, String username) {
        try {
            if (secretKey != null) {
                String payload = "[MSG]:" + username + ":" + message;
                out.println(CryptoUtils.encryptMessage(payload, secretKey));
            } else {
                ChatInterface.printToScreen("[System]: Cannot send, secure connection not established.");
            }
        } catch (Exception e) {
            ChatInterface.printToScreen("[Error]: Failed to send: " + e.getMessage());
        }
    }

    public void sendFile(String filePath, String username) {
        try {
            if (secretKey == null) return;
            File file = new File(filePath);
            if (!file.exists()) {
                ChatInterface.printToScreen("[Error]: File not found.");
                return;
            }
            ChatInterface.printToScreen("[System]: Encrypting and sending file...");
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String encryptedData = CryptoUtils.encryptFile(fileBytes, secretKey);

            String payload = "[FILE]:" + username + ":" + file.getName() + ":" + encryptedData;
            out.println(CryptoUtils.encryptMessage(payload, secretKey));
            ChatInterface.printToScreen("[System]: File sent: " + file.getName());
        } catch (Exception e) {
            ChatInterface.printToScreen("[Error]: File send failed: " + e.getMessage());
        }
    }
}