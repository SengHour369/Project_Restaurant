package org.example.UI;


import org.example.DTO.Response.UserResponse;
import org.example.UI.OrderPanel;

import javax.swing.*;

public class MainAppUI extends JFrame {

    public MainAppUI(UserResponse user) {
        initializeUI(user);
    }

    private void initializeUI(UserResponse user) {
        setTitle("🍽️ Food Ordering System");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabPane = new JTabbedPane();
        tabPane.addTab("🛒 Order", new OrderPanel(user));

        add(tabPane);
    }
}