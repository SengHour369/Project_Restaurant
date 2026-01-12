package org.example.UI;


import org.example.DTO.Response.UserResponse;

import javax.swing.*;
import java.awt.*;

public class CustomerApp extends JFrame {
    private final UserResponse currentUser;

    public CustomerApp(UserResponse user) {
        this.currentUser = user;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("🍽️ Food Order System - Customer");
        setSize(1100, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#3498db"));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel welcomeLabel = new JLabel("😊 Welcome, " + currentUser.getName() + "!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton btnLogout = new JButton("🚪 Logout");
        btnLogout.setBackground(Color.decode("#e74c3c"));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        headerPanel.add(btnLogout, BorderLayout.EAST);

        // Main content
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));

        tabbedPane.addTab("🛒 Place Order", new OrderPanel(currentUser));
        tabbedPane.addTab("📜 Order History", new OrderHistoryPanel(currentUser));
        tabbedPane.addTab("👤 My Profile", new UserPanel(currentUser));

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }
}