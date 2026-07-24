package org.example.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/** Left-hand nav rail: a vertical list of icon+label rows, one highlighted as active. */
public class SidebarNav extends JPanel {
    private final List<NavRow> rows = new ArrayList<>();

    public SidebarNav() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UITheme.CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER),
                BorderFactory.createEmptyBorder(16, 0, 16, 0)));
        setPreferredSize(new Dimension(220, 0));
    }

    /** Adds a nav row; the first row added is selected by default. */
    public void addItem(String icon, String label, Runnable onSelect) {
        NavRow row = new NavRow(icon, label, onSelect);
        rows.add(row);
        add(row);
        if (rows.size() == 1) {
            row.setActive(true);
        }
    }

    private void select(NavRow selected) {
        for (NavRow row : rows) {
            row.setActive(row == selected);
        }
        selected.onSelect.run();
    }

    private class NavRow extends JPanel {
        private final Runnable onSelect;
        private boolean active;
        private boolean hover;

        NavRow(String icon, String label, Runnable onSelect) {
            this.onSelect = onSelect;
            setLayout(new BorderLayout(10, 0));
            setOpaque(true);
            setBackground(UITheme.CARD);
            setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 16));
            setAlignmentX(LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
            add(iconLabel, BorderLayout.WEST);

            JLabel textLabel = new JLabel(label);
            textLabel.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 14));
            textLabel.setForeground(UITheme.TEXT_DARK);
            add(textLabel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { select(NavRow.this); }
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            setBackground(active ? UITheme.BRAND_BLUE_SOFT : UITheme.CARD);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (hover && !active) {
                g.setColor(UITheme.ROW_ALT);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            super.paintComponent(g);
            if (active) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.BRAND_BLUE);
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        }
    }
}