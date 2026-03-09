package com.p2p.ui;

public class ChatInterface {
    // ANSI escape codes for terminal colors
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";   // For your messages
    private static final String BLUE = "\u001B[34m";    // For peer messages
    private static final String YELLOW = "\u001B[33m";  // For system alerts
    private static final String RED = "\u001B[31m";     // For errors

    public static void showSystem(String message) {
        System.out.println(YELLOW + "[System]: " + message + RESET);
    }

    public static void showPeer(String message) {
        System.out.println(BLUE + "\n[Peer]: " + message + RESET);
        showPrompt(); // Reset the typing prompt after receiving a message
    }

    public static void showError(String message) {
        System.out.println(RED + "[Error]: " + message + RESET);
    }

    public static void showPrompt() {
        System.out.print(GREEN + "[You]: " + RESET);
    }
}