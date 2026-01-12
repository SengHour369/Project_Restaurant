package org.example.UI;


import org.example.DTO.Response.UserResponse;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    private final UserResponse currentAdmin;

    public AdminDashboard(UserResponse adminUser) {
        this.currentAdmin = adminUser;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("👑 Admin Dashboard - Food Order System");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#2c3e50"));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel welcomeLabel = new JLabel("👑 Welcome, " + currentAdmin.getName() + "!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JLabel roleLabel = new JLabel("Administrator");
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        roleLabel.setForeground(Color.LIGHT_GRAY);
        roleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        headerPanel.add(roleLabel, BorderLayout.EAST);

        // Tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));

        // Create panels
        UserPanel userPanel = new UserPanel(currentAdmin);
        RestaurantPanel restaurantPanel = new RestaurantPanel();
        MenuItemPanel menuItemPanel = new MenuItemPanel();
        AdminOrderPanel orderPanel = new AdminOrderPanel();
        PaymentPanel paymentPanel = new PaymentPanel();

        // Add tabs with icons
        tabbedPane.addTab("👥 Users", new ImageIcon("icons/user.png"), userPanel);
        tabbedPane.addTab("🏢 Restaurants", new ImageIcon("icons/restaurant.png"), restaurantPanel);
        tabbedPane.addTab("🍽️ Menu Items", new ImageIcon("icons/menu.png"), menuItemPanel);
        tabbedPane.addTab("📦 Orders", new ImageIcon("icons/order.png"), orderPanel);
        tabbedPane.addTab("💰 Payments", new ImageIcon("icons/payment.png"), paymentPanel);

        // Set layout
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(Color.decode("#34495e"));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        JLabel statusLabel = new JLabel("✅ System Ready | Admin ID: " + currentAdmin.getId());
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusPanel.add(statusLabel);

        add(statusPanel, BorderLayout.SOUTH);
    }
}