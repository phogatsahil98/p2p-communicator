package com.p2p.ui;

import com.p2p.network.PeerNode;
import com.p2p.db.DatabaseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class ChatInterface extends JFrame {
    private static ChatInterface instance;
    private JTextArea chatArea;
    private JTextField inputField;
    private JTextField usernameField;
    private PeerNode node;

    private final Color BG_MAIN = new Color(30, 30, 36);
    private final Color BG_PANEL = new Color(45, 45, 50);
    private final Color TEXT_LIGHT = new Color(230, 230, 235);
    private final Color ACCENT_BLUE = new Color(10, 132, 255);
    private final Color ACCENT_GRAY = new Color(99, 99, 102);
    private final Color ACCENT_RED = new Color(255, 69, 58); // Apple Red for Clear

    public ChatInterface() {
        instance = this;
        this.node = new PeerNode();

        setTitle("ChitChat");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        // --- 1. TOP CONTROL BAR ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        topPanel.setBackground(BG_PANEL);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 65)));

        String[] modes = {"Host (Local)", "Join (Local)", "Host AWS (1-to-1)", "Join AWS (1-to-1)", "Host AWS (Group)", "Join AWS (Group)"};
        JComboBox<String> modeBox = new JComboBox<>(modes);
        modeBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        usernameField = new JTextField("Sahil", 8);
        styleTextField(usernameField);

        JTextField ipField = new JTextField("16.171.1.19", 11);
        styleTextField(ipField);

        JButton connectBtn = new JButton("Connect");
        styleButton(connectBtn, ACCENT_BLUE, Color.WHITE);

        topPanel.add(createLabel("Name:"));
        topPanel.add(usernameField);
        topPanel.add(createLabel("Mode:"));
        topPanel.add(modeBox);
        topPanel.add(createLabel("IP/Port:"));
        topPanel.add(ipField);
        topPanel.add(connectBtn);

        // --- 2. CHAT AREA ---
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(BG_MAIN);
        chatArea.setForeground(TEXT_LIGHT);
        chatArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        chatArea.setMargin(new Insets(15, 15, 15, 15));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(null);

        // --- 3. BOTTOM INPUT AREA ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBackground(BG_MAIN);
        bottomPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        inputField = new JTextField();
        styleTextField(inputField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BG_MAIN);

        // NEW CLEAR BUTTON
        JButton clearBtn = new JButton("🗑️ Clear");
        styleButton(clearBtn, ACCENT_RED, Color.WHITE);

        JButton attachBtn = new JButton("\uD83D\uDCCE Attach");
        styleButton(attachBtn, ACCENT_GRAY, Color.WHITE);

        JButton sendBtn = new JButton("Send");
        styleButton(sendBtn, ACCENT_BLUE, Color.WHITE);

        buttonPanel.add(clearBtn);
        buttonPanel.add(attachBtn);
        buttonPanel.add(sendBtn);

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- LOGIC ---
        connectBtn.addActionListener(e -> {
            connectBtn.setText("Connecting...");
            connectBtn.setEnabled(false);
            int choice = modeBox.getSelectedIndex() + 1;
            String ipOrPort = ipField.getText().trim();

            new Thread(() -> {
                try {
                    if (choice == 1) node.startHost(Integer.parseInt(ipOrPort));
                    else if (choice == 2) node.connectToPeer(ipOrPort, 8080);
                    else {
                        boolean isHost = (choice == 3 || choice == 5);
                        boolean isGroup = (choice == 5 || choice == 6);
                        node.connectToRelay(ipOrPort, 9000, isHost, isGroup);
                    }
                } catch (Exception ex) {
                    printToScreen("[Error] Connection failed: " + ex.getMessage());
                    connectBtn.setText("Failed");
                    connectBtn.setBackground(ACCENT_RED);
                }
            }).start();
        });

        // CLEAR LOGIC
        clearBtn.addActionListener(e -> clearChat());

        attachBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String uName = getUsername();
                new Thread(() -> {
                    node.sendFile(file.getAbsolutePath(), uName);
                    printToScreen("You sent a file: " + file.getName());
                    DatabaseManager.saveMessage("You", "[Sent a file: " + file.getName() + "]");
                }).start();
            }
        });

        Action sendAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText().trim();
                if (!text.isEmpty()) {
                    if (text.equalsIgnoreCase("/clear")) {
                        clearChat();
                    } else {
                        String uName = getUsername();
                        node.sendMessage(text, uName);
                        printToScreen("You: " + text);
                        DatabaseManager.saveMessage("You", text);
                    }
                    inputField.setText("");
                }
            }
        };
        sendBtn.addActionListener(sendAction);
        inputField.addActionListener(sendAction);
    }

    private void clearChat() {
        chatArea.setText("");
        DatabaseManager.clearHistory();
        printToScreen("[System]: Chat history cleared.");
    }

    private String getUsername() {
        String name = usernameField.getText().trim();
        return name.isEmpty() ? "Unknown" : name;
    }

    public static void printToScreen(String message) {
        if (instance != null) {
            SwingUtilities.invokeLater(() -> {
                instance.chatArea.append("\n" + message + "\n");
                instance.chatArea.setCaretPosition(instance.chatArea.getDocument().getLength());
            });
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_LIGHT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(BG_PANEL);
        field.setForeground(TEXT_LIGHT);
        field.setCaretColor(TEXT_LIGHT);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60, 60, 65), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 35));
    }
}