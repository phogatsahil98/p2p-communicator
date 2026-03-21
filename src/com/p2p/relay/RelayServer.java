package com.p2p.relay;

import java.io.*;
import java.net.*;
import java.util.*;

public class RelayServer {
    private static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) throws IOException {
        int port = 9000; // Standard relay port
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("P2P Global Relay started on port " + port);

        while (true) {
            new Handler(serverSocket.accept()).start();
        }
    }

    private static class Handler extends Thread {
        private Socket socket;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                clientWriters.add(out);
                System.out.println("New peer joined the relay.");

                String message;
                while ((message = in.readLine()) != null) {
                    // Broadcast the encrypted message to the OTHER peer
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            if (writer != out) {
                                writer.println(message);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Peer disconnected.");
            } finally {
                if (out != null) clientWriters.remove(out);
            }
        }
    }
}