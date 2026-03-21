package com.p2p;

import com.p2p.db.DatabaseManager;
import com.p2p.ui.ChatInterface;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Initialize the Database
        DatabaseManager.initialize();

        // Launch the Graphical User Interface
        SwingUtilities.invokeLater(() -> {
            ChatInterface gui = new ChatInterface();
            gui.setVisible(true);
            ChatInterface.printToScreen("=== SECURE P2P COMMUNICATOR (GUI Edition) ===");
            ChatInterface.printToScreen("[System]: Database loaded. Select your mode at the top and click Connect.");
        });
    }
}