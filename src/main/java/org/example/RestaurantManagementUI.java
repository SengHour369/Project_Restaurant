package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RestaurantManagementUI extends JFrame {

    public RestaurantManagementUI() {
        setTitle("Restaurant Management System");
        setSize(1100, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // ===== Title =====
        JLabel title = new JLabel("Restaurant Management System", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(title, BorderLayout.NORTH);

        // ===== Main Panel =====
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== User Orders =====
        String[] orderColumns = {"Order ID", "Customer", "Status", "Total"};
        Object[][] orderData = {
                {"1024", "John", "Completed", "$45.20"},
                {"1025", "Sara", "Pending", "$32.50"},
                {"1026", "Mike", "In Progress", "$28.90"}
        };

        JTable orderTable = new JTable(new DefaultTableModel(orderData, orderColumns));
        JScrollPane orderScroll = new JScrollPane(orderTable);
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.add(new JLabel("User Orders", JLabel.CENTER), BorderLayout.NORTH);
        orderPanel.add(orderScroll, BorderLayout.CENTER);

        // ===== Order Items =====
        String[] itemColumns = {"Item", "Qty", "Price"};
        Object[][] itemData = {
                {"Burger", 2, "$14.00"},
                {"Fries", 1, "$5.00"},
                {"Soda", 2, "$6.00"}
        };

        JTable itemTable = new JTable(new DefaultTableModel(itemData, itemColumns));
        JScrollPane itemScroll = new JScrollPane(itemTable);
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.add(new JLabel("Order Items", JLabel.CENTER), BorderLayout.NORTH);
        itemPanel.add(itemScroll, BorderLayout.CENTER);

        // ===== Menu Items =====
        String[] menuColumns = {"Name", "Category", "Price", "Status"};
        Object[][] menuData = {
                {"Burger", "Entree", "$7.00", "Available"},
                {"Pizza", "Entree", "$11.50", "Available"},
                {"Cake", "Dessert", "$5.50", "Out of Stock"}
        };

        JTable menuTable = new JTable(new DefaultTableModel(menuData, menuColumns));
        JScrollPane menuScroll = new JScrollPane(menuTable);
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.add(new JLabel("Menu Items", JLabel.CENTER), BorderLayout.NORTH);
        menuPanel.add(menuScroll, BorderLayout.CENTER);

        // ===== Add Panels =====
        mainPanel.add(orderPanel);
        mainPanel.add(itemPanel);
        mainPanel.add(menuPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RestaurantManagementUI().setVisible(true);
        });
    }
}
