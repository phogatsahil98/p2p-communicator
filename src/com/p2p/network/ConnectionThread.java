package com.p2p.network;

import com.p2p.crypto.CryptoUtils;
import com.p2p.db.DatabaseManager;
import com.p2p.ui.ChatInterface;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.PublicKey;

public class ConnectionThread extends Thread {
    private Socket socket;
    private PeerNode node;

    public ConnectionThread(Socket socket, PeerNode node) {
        this.socket = socket;
        this.node = node;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String incoming;
            while ((incoming = in.readLine()) != null) {

                if (node.isGroupChat() && incoming.startsWith("[KEY_REQUEST]:") && node.isHost()) {
                    String pubKeyStr = incoming.substring(14);
                    PublicKey peerKey = CryptoUtils.decodePublicKey(pubKeyStr);
                    String encKey = CryptoUtils.encryptAESKey(node.getSecretKey(), peerKey);
                    node.getOut().println("[KEY_RESPONSE]:" + encKey);
                    ChatInterface.printToScreen("[System]: A new peer joined the group securely.");
                }

                else if (node.isGroupChat() && incoming.startsWith("[KEY_RESPONSE]:") && !node.isHost() && node.getSecretKey() == null) {
                    try {
                        String encKey = incoming.substring(15);
                        SecretKey key = CryptoUtils.decryptAESKey(encKey, node.getMyKeyPair().getPrivate());
                        node.setSecretKey(key);
                        ChatInterface.printToScreen("[System]: Secure group connection established!");
                    } catch (Exception e) { }
                }

                else if (node.getSecretKey() != null && !incoming.startsWith("[KEY_")) {
                    try {
                        String decrypted = CryptoUtils.decryptMessage(incoming, node.getSecretKey());

                        if (decrypted.startsWith("[FILE]:")) {
                            // Unpack: [FILE] : Username : Filename : EncryptedData
                            String[] parts = decrypted.split(":", 4);
                            ChatInterface.printToScreen("\uD83D\uDCCE " + parts[1] + " sent a secure file: " + parts[2]);
                            DatabaseManager.saveMessage(parts[1], "[Received file: " + parts[2] + "]");
                        }
                        else if (decrypted.startsWith("[MSG]:")) {
                            // Unpack: [MSG] : Username : Message
                            String[] parts = decrypted.split(":", 3);
                            ChatInterface.printToScreen(parts[1] + ": " + parts[2]);
                            DatabaseManager.saveMessage(parts[1], parts[2]);
                        }
                    } catch (Exception e) { }
                }
            }
        } catch (Exception e) {
            ChatInterface.printToScreen("[System]: Disconnected.");
        }
    }
}