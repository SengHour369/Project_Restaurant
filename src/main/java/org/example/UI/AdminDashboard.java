package org.example.UI;

import org.example.DTO.Response.UserResponse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminDashboard extends JFrame {
    private final UserResponse currentAdmin;

    public AdminDashboard(UserResponse adminUser) {
        this.currentAdmin = adminUser;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Admin Dashboard - Food Order System");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#2c3e50"));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel welcomeLabel = new JLabel("Welcome, " + currentAdmin.getName() + "!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        // User info panel
        JPanel userInfoPanel = new JPanel(new BorderLayout());
        userInfoPanel.setBackground(Color.decode("#2c3e50"));
        userInfoPanel.setOpaque(false);

        JLabel roleLabel = new JLabel("Administrator");
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        roleLabel.setForeground(Color.LIGHT_GRAY);
        roleLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // Logout button
        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnLogout.setBackground(Color.decode("#e74c3c"));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#c0392b"), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        btnLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLogout.setBackground(Color.decode("#c0392b"));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLogout.setBackground(Color.decode("#e74c3c"));
            }
        });

        // Logout action
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        userInfoPanel.add(roleLabel, BorderLayout.NORTH);
        userInfoPanel.add(btnLogout, BorderLayout.SOUTH);
        headerPanel.add(userInfoPanel, BorderLayout.EAST);

        // Tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(Color.decode("#2c3e50"));

        // Create panels
        UserPanel userPanel = new UserPanel(currentAdmin);
        RestaurantPanel restaurantPanel = new RestaurantPanel();
        MenuItemPanel menuItemPanel = new MenuItemPanel();
        AdminOrderPanel orderPanel = new AdminOrderPanel();
        PaymentPanel paymentPanel = new PaymentPanel();

        // Add tabs with plain text
        tabbedPane.addTab("Users", userPanel);
        tabbedPane.addTab("Restaurants", restaurantPanel);
        tabbedPane.addTab("Menu Items", menuItemPanel);
        tabbedPane.addTab("Orders", orderPanel);
        tabbedPane.addTab("Payments", paymentPanel);

        // Customize tab colors
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            JLabel tabLabel = new JLabel(tabbedPane.getTitleAt(i));
            tabLabel.setHorizontalAlignment(SwingConstants.CENTER);
            tabLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            tabbedPane.setTabComponentAt(i, tabLabel);
        }

        // Set layout
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(Color.decode("#34495e"));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        JLabel statusLabel = new JLabel("System Ready | Admin ID: " + currentAdmin.getId());
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // Current time label
        JLabel timeLabel = new JLabel();
        timeLabel.setForeground(Color.LIGHT_GRAY);
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        updateTimeLabel(timeLabel);

        // Timer to update time
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTimeLabel(timeLabel);
            }
        });
        timer.start();

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(timeLabel, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);

        // Add window listener for confirmation on close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                confirmExit();
            }
        });

        // Set application icon (no emoji)
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage("icons/admin.png"));
        } catch (Exception e) {
            // Icon not found, continue without it
        }

        // Make full screen optional
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void updateTimeLabel(JLabel label) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        label.setText(sdf.format(new java.util.Date()));
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><div style='font-size:14pt;'>Are you sure you want to logout?<br><br>" +
                        "You will be redirected to the login screen.</div></html>",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose(); // Close current window

            // Show login frame
            SwingUtilities.invokeLater(() -> {
                try {
                    LoginFrame loginFrame = new LoginFrame();
                    loginFrame.setVisible(true);
                } catch (Exception e) {
                    // If LoginFrame doesn't exist, show message and exit
                    JOptionPane.showMessageDialog(null,
                            "Logout successful!\nPlease restart the application.",
                            "Logout Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
            });
        }
    }

    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><div style='font-size:14pt;'>Are you sure you want to exit?<br><br>" +
                        "Any unsaved changes will be lost.</div></html>",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        } else {
            setDefaultCloseOperation(EXIT_ON_CLOSE);
        }
    }

    // Optional: Add keyboard shortcuts
    private void setupKeyboardShortcuts() {
        // Ctrl+L for logout
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("control L"), "logout");
        getRootPane().getActionMap().put("logout", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        // Alt+F4 for exit with confirmation
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("alt F4"), "exit");
        getRootPane().getActionMap().put("exit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmExit();
            }
        });

        // F1 for help
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F1"), "help");
        getRootPane().getActionMap().put("help", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHelp();
            }
        });
    }

    private void showHelp() {
        String helpText = "<html><div style='font-size:12pt;'>" +
                "<b>Admin Dashboard Help</b><br><br>" +
                "<b>Available Shortcuts:</b><br>" +
                "• Ctrl+L: Logout<br>" +
                "• F5: Refresh current tab<br>" +
                "• Ctrl+F: Search in current tab<br>" +
                "• F1: Show this help<br><br>" +
                "<b>Tabs:</b><br>" +
                "• Users: Manage user accounts<br>" +
                "• Restaurants: Manage restaurants<br>" +
                "• Menu Items: Manage menu items<br>" +
                "• Orders: View all orders<br>" +
                "• Payments: View payment history<br><br>" +
                "<b>Tip:</b> Double-click items in tables to edit them.</div></html>";

        JOptionPane.showMessageDialog(this, helpText,
                "Help - Admin Dashboard", JOptionPane.INFORMATION_MESSAGE);
    }

    // Main method for testing
    public static void main(String[] args) {
        // For testing only
        SwingUtilities.invokeLater(() -> {
            // Create a dummy admin user for testing
            UserResponse testAdmin = new UserResponse();
            testAdmin.setId(1);
            testAdmin.setName("Admin User");
            testAdmin.setEmail("admin@example.com");
            testAdmin.setStatus("ADMIN");

            AdminDashboard dashboard = new AdminDashboard(testAdmin);
            dashboard.setVisible(true);
        });
    }
}