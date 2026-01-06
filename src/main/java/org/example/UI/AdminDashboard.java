package org.example.UI;

import org.example.DTO.Response.UserResponse;

import javax.swing.*;

public class AdminDashboard extends JFrame {

    public AdminDashboard(UserResponse user){
        setTitle("Admin Dashboard");
        setSize(1100,650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tab = new JTabbedPane();

        tab.addTab("Users", new UserPanel(user));
        tab.addTab("Restaurants", new RestaurantPanel());
        tab.addTab("Menu Items", new MenuItemPanel());
        tab.addTab("Orders", new AdminOrderPanel());
        tab.addTab("Payments", new PaymentPanel());

        add(tab);
    }
}
