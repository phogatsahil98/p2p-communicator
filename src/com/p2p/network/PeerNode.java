package com.p2p.network;

import com.p2p.crypto.CryptoUtils;
import javax.crypto.SecretKey;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerNode {
    private Socket socket;
    private PrintWriter out;
    private SecretKey secretKey;

    // 1. Start as a HOST (Waiting for someone to connect to you)
    public void startHost(int port, SecretKey key) throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("[System]: Listening for connections on port " + port + "...");

        // The program pauses here until another peer connects
        this.socket = serverSocket.accept();
        System.out.println("[System]: Target peer connected securely!");

        setupConnection(key);
    }

    // 2. Start as a CLIENT (Connecting to a known IP address)
    public void connectToPeer(String ipAddress, int port, SecretKey key) throws Exception {
        System.out.println("[System]: Attempting connection to " + ipAddress + ":" + port + "...");
        this.socket = new Socket(ipAddress, port);
        System.out.println("[System]: Connection established securely!");

        setupConnection(key);
    }

    // 3. Shared setup for both roles
    private void setupConnection(SecretKey key) throws Exception {
        this.secretKey = key;

        // Set up the output stream to send data OVER the network
        // The 'true' flag means auto-flush (send immediately, don't buffer)
        this.out = new PrintWriter(socket.getOutputStream(), true);

        // Start the background thread we created in Step 2 to listen for messages
        ConnectionThread listener = new ConnectionThread(socket, secretKey);
        listener.start();
    }

    // 4. Encrypt and push the message over the network
    public void sendMessage(String plainMessage) {
        try {
            String encryptedMessage = CryptoUtils.encryptMessage(plainMessage, secretKey);
            out.println(encryptedMessage);
        } catch (Exception e) {
            System.out.println("[Error]: Failed to encrypt or send message.");
        }
    }
}