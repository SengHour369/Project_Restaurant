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
        setTitle("Food Order System — Customer");
        setSize(1180, 760);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);

        JButton btnLogout = UITheme.createOutlineButton("Logout");
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        TopBar topBar = new TopBar("Cafeteria", currentUser.getName(), currentUser.getImage_path(), btnLogout);

        CardLayout cardLayout = new CardLayout();
        JPanel content = new JPanel(cardLayout);
        content.setBackground(UITheme.BACKGROUND);

        content.add(new OrderPanel(currentUser), "order");
        content.add(new OrderHistoryPanel(currentUser), "history");
        content.add(new ProfilePanel(currentUser), "profile");

        SidebarNav sidebar = new SidebarNav();
        sidebar.addItem("🍕", "Place Order", () -> cardLayout.show(content, "order"));
        sidebar.addItem("🧾", "Order History", () -> cardLayout.show(content, "history"));
        sidebar.addItem("👤", "My Profile", () -> cardLayout.show(content, "profile"));

        JPanel body = new JPanel(new BorderLayout());
        body.add(sidebar, BorderLayout.WEST);
        body.add(content, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }
}