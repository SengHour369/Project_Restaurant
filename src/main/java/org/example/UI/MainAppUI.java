package org.example.UI;

import org.example.DTO.Response.UserResponse;

import javax.swing.*;

public class MainAppUI extends JFrame {

    public MainAppUI(UserResponse user) {
        setTitle("Order System");
        setSize(1000,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tab = new JTabbedPane();
        tab.addTab("Order", new OrderPanel(user));
        add(tab);
    }
}
