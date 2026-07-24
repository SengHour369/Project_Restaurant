package org.example.UI;

import org.example.DTO.Response.OrderResponse;
import org.example.Service.ServiceImplement.ServiceMenuItemImp;
import org.example.Service.ServiceImplement.ServiceOrderImp;
import org.example.Service.ServiceImplement.ServiceRestaurantImp;
import org.example.Service.ServiceImplement.ServiceUserImp;

import javax.swing.*;
import java.awt.*;

/** Admin "Dashboard" landing screen: a quick at-a-glance stat overview. */
public class AdminHomePanel extends JPanel {
    private final ServiceUserImp userService = new ServiceUserImp();
    private final ServiceRestaurantImp restaurantService = new ServiceRestaurantImp();
    private final ServiceMenuItemImp menuService = new ServiceMenuItemImp();
    private final ServiceOrderImp orderService = new ServiceOrderImp();

    private final JPanel cardsRow = new JPanel(new GridLayout(1, 5, 16, 0));

    public AdminHomePanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Dashboard");
        title.setFont(UITheme.FONT_H1);
        title.setForeground(UITheme.TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        cardsRow.setOpaque(false);
        add(cardsRow, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        cardsRow.removeAll();
        try {
            int users = userService.findAll().size();
            int restaurants = restaurantService.findAllRestaurants().size();
            int menuItems = menuService.getAllMenuItems().size();
            java.util.List<OrderResponse> orders = orderService.findAllOrders();
            double revenue = 0;
            for (OrderResponse o : orders) {
                revenue += o.getTotalPrice() != null ? o.getTotalPrice() : 0;
            }

            cardsRow.add(statCard("Users", String.valueOf(users), UITheme.PRIMARY));
            cardsRow.add(statCard("Restaurants", String.valueOf(restaurants), UITheme.ACCENT));
            cardsRow.add(statCard("Menu Items", String.valueOf(menuItems), UITheme.SUCCESS));
            cardsRow.add(statCard("Orders", String.valueOf(orders.size()), UITheme.BRAND_BLUE));
            cardsRow.add(statCard("Revenue", String.format("$%.2f", revenue), UITheme.SECONDARY));
        } catch (Exception ex) {
            cardsRow.add(new JLabel("Could not load dashboard stats: " + ex.getMessage()));
        }
        cardsRow.revalidate();
        cardsRow.repaint();
    }

    private JPanel statCard(String label, String value, Color accent) {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(card.getBorder(),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font(UITheme.FONT_H1.getFamily(), Font.BOLD, 26));
        valueLabel.setForeground(accent);

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(UITheme.FONT_MUTED);
        textLabel.setForeground(UITheme.TEXT_MUTED);

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(textLabel, BorderLayout.SOUTH);
        return card;
    }
}