package org.example.UI;

import javax.swing.*;

public class MainAppUI extends JFrame {

    public MainAppUI() {
        setTitle("Management System");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // center the window

        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Add tabs
        tabbedPane.addTab("Users", new UserPanel());
        tabbedPane.addTab("Restaurants", new RestaurantPanel());
        tabbedPane.addTab("Payments", new PaymentPanel());
        tabbedPane.addTab("Orders", new OrderPanel());

        add(tabbedPane);

    }
}
