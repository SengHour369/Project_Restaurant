package org.example.UI;

import org.example.DTO.Response.UserResponse;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminDashboard extends JFrame {
    private final UserResponse currentAdmin;
    private AdminHomePanel homePanel;

    public AdminDashboard(UserResponse adminUser) {
        this.currentAdmin = adminUser;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Admin Dashboard — Food Order System");
        setSize(1280, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);

        JButton btnLogout = UITheme.createOutlineButton("Logout");
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        TopBar topBar = new TopBar("Cafeteria", currentAdmin.getName(), currentAdmin.getImage_path(), btnLogout);

        // ---- Sidebar + card-swapped content area ----
        CardLayout cardLayout = new CardLayout();
        JPanel content = new JPanel(cardLayout);
        content.setBackground(UITheme.BACKGROUND);

        homePanel = new AdminHomePanel();
        UserPanel userPanel = new UserPanel(currentAdmin);
        RestaurantPanel restaurantPanel = new RestaurantPanel();
        MenuItemPanel menuItemPanel = new MenuItemPanel();
        AdminOrderPanel orderPanel = new AdminOrderPanel();
        PaymentPanel paymentPanel = new PaymentPanel();

        content.add(homePanel, "dashboard");
        content.add(userPanel, "users");
        content.add(restaurantPanel, "restaurants");
        content.add(menuItemPanel, "menu");
        content.add(orderPanel, "orders");
        content.add(paymentPanel, "payments");

        SidebarNav sidebar = new SidebarNav();
        sidebar.addItem("🏠", "Dashboard", () -> { homePanel.refresh(); cardLayout.show(content, "dashboard"); });
        sidebar.addItem("👤", "Users", () -> cardLayout.show(content, "users"));
        sidebar.addItem("🏪", "Restaurants", () -> cardLayout.show(content, "restaurants"));
        sidebar.addItem("🍔", "Food Menu List", () -> cardLayout.show(content, "menu"));
        sidebar.addItem("🧾", "Order Detail", () -> cardLayout.show(content, "orders"));
        sidebar.addItem("💳", "Payment Detail", () -> cardLayout.show(content, "payments"));

        JPanel body = new JPanel(new BorderLayout());
        body.add(sidebar, BorderLayout.WEST);
        body.add(content, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        // ---- Status bar: subtle light footer ----
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(UITheme.CARD);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(8, 28, 8, 28)));

        JLabel statusLabel = new JLabel("System Ready · Admin ID " + currentAdmin.getId());
        statusLabel.setForeground(UITheme.TEXT_MUTED);
        statusLabel.setFont(UITheme.FONT_MUTED);

        JLabel timeLabel = new JLabel();
        timeLabel.setForeground(UITheme.TEXT_MUTED);
        timeLabel.setFont(UITheme.FONT_MUTED);
        updateTimeLabel(timeLabel);

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

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                confirmExit();
            }
        });

        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage("icons/admin.png"));
        } catch (Exception e) {
            // ignore
        }

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
            dispose();
            SwingUtilities.invokeLater(() -> {
                try {
                    LoginFrame loginFrame = new LoginFrame();
                    loginFrame.setVisible(true);
                } catch (Exception e) {
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

    private void setupKeyboardShortcuts() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("control L"), "logout");
        getRootPane().getActionMap().put("logout", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("alt F4"), "exit");
        getRootPane().getActionMap().put("exit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmExit();
            }
        });

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
                "- Ctrl+L: Logout<br>" +
                "- F5: Refresh current tab<br>" +
                "- Ctrl+F: Search in current tab<br>" +
                "- F1: Show this help<br><br>" +
                "<b>Tabs:</b><br>" +
                "- Users: Manage user accounts<br>" +
                "- Restaurants: Manage restaurants<br>" +
                "- Menu Items: Manage menu items<br>" +
                "- Orders: View all orders<br>" +
                "- Payments: View payment history<br><br>" +
                "<b>Tip:</b> Double-click items in tables to edit them.</div></html>";
        JOptionPane.showMessageDialog(this, helpText,
                "Help - Admin Dashboard", JOptionPane.INFORMATION_MESSAGE);
    }
}