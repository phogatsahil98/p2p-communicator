package com.p2p.network;

import com.p2p.crypto.CryptoUtils;
import com.p2p.ui.ChatInterface;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;

public class PeerNode {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private SecretKey secretKey;

    public void startHost(int port) throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        ChatInterface.showSystem("Listening for connections on port " + port + "...");
        this.socket = serverSocket.accept();
        ChatInterface.showSystem("Target peer connected! Initiating secure RSA handshake...");

        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // 1. Host generates the RSA Key Pair
        KeyPair rsaPair = CryptoUtils.generateRSAKeyPair();

        // 2. Host sends Public Key to Client
        String pubKeyString = Base64.getEncoder().encodeToString(rsaPair.getPublic().getEncoded());
        out.println(pubKeyString);

        // 3. Host waits to receive the encrypted AES key from Client
        String encryptedAesKeyStr = in.readLine();

        // 4. Host decrypts the AES key using their Private Key
        byte[] decryptedAesBytes = CryptoUtils.decryptRSA(encryptedAesKeyStr, rsaPair.getPrivate());
        this.secretKey = new SecretKeySpec(decryptedAesBytes, 0, decryptedAesBytes.length, "AES");

        ChatInterface.showSystem("Handshake complete. AES Session Key secured.");
        startChatThread();
    }

    public void connectToPeer(String ipAddress, int port) throws Exception {
        ChatInterface.showSystem("Attempting connection to " + ipAddress + ":" + port + "...");
        this.socket = new Socket(ipAddress, port);
        ChatInterface.showSystem("Connection established! Awaiting host's public key...");

        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // 1. Client receives Public Key from Host
        String pubKeyString = in.readLine();
        PublicKey hostPublicKey = CryptoUtils.getPublicKeyFromString(pubKeyString);

        // 2. Client generates the AES Session Key
        this.secretKey = CryptoUtils.generateAESKey();

        // 3. Client encrypts the AES Key with Host's Public Key and sends it
        String encryptedAesKey = CryptoUtils.encryptRSA(this.secretKey.getEncoded(), hostPublicKey);
        out.println(encryptedAesKey);

        ChatInterface.showSystem("AES Session Key generated and securely transmitted.");
        startChatThread();
    }

    private void startChatThread() {
        ConnectionThread listener = new ConnectionThread(socket, secretKey);
        listener.start();
    }

    public void sendMessage(String plainMessage) {
        try {
            String encryptedMessage = CryptoUtils.encryptMessage(plainMessage, secretKey);
            out.println(encryptedMessage);
        } catch (Exception e) {
            ChatInterface.showError("Failed to encrypt or send message.");
        }
    }
}