package org.example.UI;

import javax.swing.*;
import java.awt.*;

/** Shared blue header bar: brand on the left, welcome text + avatar + actions on the right. */
public class TopBar extends JPanel {

    public TopBar(String brand, String userName, String userImagePath, JButton... trailingActions) {
        setLayout(new BorderLayout());
        setBackground(UITheme.BRAND_BLUE);
        setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        JLabel brandLabel = new JLabel(brand);
        brandLabel.setFont(UITheme.FONT_TITLE);
        brandLabel.setForeground(Color.WHITE);
        add(brandLabel, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        JLabel welcome = new JLabel("Welcome, " + userName);
        welcome.setFont(UITheme.FONT_BODY);
        welcome.setForeground(Color.WHITE);
        right.add(welcome);

        right.add(UITheme.createAvatar(userImagePath, userName, 38));

        for (JButton action : trailingActions) {
            right.add(action);
        }

        add(right, BorderLayout.EAST);
    }
}