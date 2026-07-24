package org.example.UI;

import org.example.DTO.Response.OrderResponse;
import org.example.Service.ServiceImplement.ServiceMenuItemImp;
import org.example.Service.ServiceImplement.ServiceOrderImp;
import org.example.Service.ServiceImplement.ServiceRestaurantImp;
import org.example.Service.ServiceImplement.ServiceUserImp;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Admin "My Dashboard" landing screen: an at-a-glance overview styled after the
 * Dainty Food reference layout — headline stat cards with icons and delta chips,
 * a monthly revenue bar chart, a performance gauge, a station summary, and a
 * recent-orders list. Every figure is derived from the live services.
 */
public class AdminHomePanel extends JPanel {
    private final ServiceUserImp userService = new ServiceUserImp();
    private final ServiceRestaurantImp restaurantService = new ServiceRestaurantImp();
    private final ServiceMenuItemImp menuService = new ServiceMenuItemImp();
    private final ServiceOrderImp orderService = new ServiceOrderImp();

    // Warm dashboard accents (kept local so the rest of the app stays on-theme).
    private static final Color CORAL      = Color.decode("#E86C4F");
    private static final Color CORAL_SOFT = Color.decode("#FBE3DB");
    private static final Color BAR_SOFT   = Color.decode("#F6D2C6");

    private final JPanel content = new JPanel(new GridBagLayout());

    public AdminHomePanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        add(buildHeader(), BorderLayout.NORTH);

        content.setOpaque(false);
        JScrollPane scroll = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 2, 20, 2));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("My Dashboard");
        title.setFont(UITheme.FONT_H1);
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Your recent transaction activity and overview");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        titles.add(title);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);

        JLabel today = new JLabel(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")));
        today.setFont(UITheme.FONT_MUTED);
        today.setForeground(UITheme.TEXT_MUTED);
        today.setVerticalAlignment(SwingConstants.BOTTOM);
        header.add(today, BorderLayout.EAST);

        return header;
    }

    public void refresh() {
        content.removeAll();
        try {
            populate();
        } catch (Exception ex) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = 0; c.weightx = 1; c.anchor = GridBagConstraints.NORTHWEST;
            content.add(new JLabel("Could not load dashboard stats: " + ex.getMessage()), c);
        }
        content.revalidate();
        content.repaint();
    }

    private void populate() {
        int menuItems = menuService.getAllMenuItems().size();
        int newDishes = 0;
        try {
            newDishes = (int) menuService.getAllMenuItems().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getActive())).count();
        } catch (Exception ignored) { }

        List<OrderResponse> orders = orderService.findAllOrders();
        int totalOrders = orders.size();

        double revenue = 0;
        int paidOrders = 0;
        int todaysOrders = 0;
        double[] monthlyRevenue = new double[12];
        LocalDate today = LocalDate.now();

        for (OrderResponse o : orders) {
            double amount = o.getTotalPrice() != null ? o.getTotalPrice() : 0;
            revenue += amount;
            if (o.getPayment() != null) paidOrders++;
            LocalDateTime dt = o.getOrderDate();
            if (dt != null) {
                monthlyRevenue[dt.getMonthValue() - 1] += amount;
                if (dt.toLocalDate().isEqual(today)) todaysOrders++;
            }
        }
        int pendingOrders = totalOrders - paidOrders;
        double profit = revenue * 0.30; // 30% estimated margin
        int performancePct = totalOrders == 0 ? 0 : Math.round(paidOrders * 100f / totalOrders);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 20, 0);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;

        // ---- Row 1: headline stat cards ----
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 20, 0));
        statsRow.setOpaque(false);
        statsRow.add(statCard("🍽", "Available Dish", String.valueOf(menuItems),
                "+" + newDishes + " active", UITheme.SUCCESS, CORAL, CORAL_SOFT));
        statsRow.add(statCard("📦", "Total Order", String.valueOf(totalOrders),
                performancePct + "% paid", UITheme.SUCCESS, UITheme.PRIMARY, UITheme.PRIMARY_SOFT));
        statsRow.add(statCard("💰", "Total Sale", money(revenue),
                todaysOrders + " today", UITheme.SUCCESS, UITheme.BRAND_BLUE, UITheme.BRAND_BLUE_SOFT));
        statsRow.add(statCard("📈", "Total Profit", money(profit),
                "~30% margin", UITheme.SUCCESS, UITheme.ACCENT, Color.decode("#FEF3E2")));
        c.gridx = 0; c.gridy = 0;
        content.add(statsRow, c);

        // ---- Row 2: revenue chart | performance gauge | station summary ----
        JPanel midRow = new JPanel(new GridBagLayout());
        midRow.setOpaque(false);
        GridBagConstraints m = new GridBagConstraints();
        m.fill = GridBagConstraints.BOTH;
        m.gridy = 0;
        m.insets = new Insets(0, 0, 0, 20);

        m.gridx = 0; m.weightx = 0.46;
        midRow.add(cardWithTitle("Total Revenue", new RevenueChart(monthlyRevenue), 260), m);

        m.gridx = 1; m.weightx = 0.27;
        midRow.add(cardWithTitle("Performance",
                new GaugePanel(performancePct, revenue), 260), m);

        m.gridx = 2; m.weightx = 0.27; m.insets = new Insets(0, 0, 0, 0);
        midRow.add(stationCard(todaysOrders, paidOrders, pendingOrders), m);

        c.gridx = 0; c.gridy = 1;
        content.add(midRow, c);

        // ---- Row 3: recent orders ----
        c.gridx = 0; c.gridy = 2; c.weighty = 1; c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.NORTHWEST;
        content.add(recentOrdersCard(orders), c);
    }

    // ---------- Card builders ----------

    private JPanel statCard(String glyph, String label, String value, String delta,
                            Color deltaColor, Color iconColor, Color iconBg) {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(BorderFactory.createCompoundBorder(card.getBorder(),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JComponent icon = new IconBadge(glyph, iconColor, iconBg);
        card.add(icon, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(UITheme.FONT_MUTED);
        labelLbl.setForeground(UITheme.TEXT_MUTED);
        labelLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font(UITheme.FONT_H1.getFamily(), Font.BOLD, 24));
        valueLbl.setForeground(UITheme.TEXT_DARK);
        valueLbl.setAlignmentX(LEFT_ALIGNMENT);
        valueLbl.setBorder(BorderFactory.createEmptyBorder(2, 0, 4, 0));

        JLabel deltaLbl = new JLabel("▲ " + delta);
        deltaLbl.setFont(new Font(UITheme.FONT_MUTED.getFamily(), Font.BOLD, 11));
        deltaLbl.setForeground(deltaColor);
        deltaLbl.setAlignmentX(LEFT_ALIGNMENT);

        text.add(labelLbl);
        text.add(valueLbl);
        text.add(deltaLbl);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    /** A titled white card wrapping an arbitrary body component at a fixed height. */
    private JPanel cardWithTitle(String title, JComponent body, int height) {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createCompoundBorder(card.getBorder(),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        card.setPreferredSize(new Dimension(10, height));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 16));
        titleLbl.setForeground(UITheme.TEXT_DARK);
        card.add(titleLbl, BorderLayout.NORTH);

        body.setOpaque(false);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel stationCard(int todays, int completed, int pending) {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createCompoundBorder(card.getBorder(),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        card.setPreferredSize(new Dimension(10, 260));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Station Overview");
        title.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 16));
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Live order queue status");
        sub.setFont(UITheme.FONT_MUTED);
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        head.add(title);
        head.add(sub);
        card.add(head, BorderLayout.NORTH);

        JPanel boxes = new JPanel(new GridLayout(1, 3, 12, 0));
        boxes.setOpaque(false);
        boxes.add(miniStat(String.format("%02d", todays), "Today's", CORAL));
        boxes.add(miniStat(String.format("%02d", completed), "Completed", UITheme.SUCCESS));
        boxes.add(miniStat(String.format("%02d", pending), "Pending", UITheme.ACCENT));
        card.add(boxes, BorderLayout.CENTER);
        return card;
    }

    private JPanel miniStat(String value, String label, Color color) {
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(UITheme.blend(color, Color.WHITE, 0.88f));
        box.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 8));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        JLabel v = new JLabel(value);
        v.setFont(new Font(UITheme.FONT_H1.getFamily(), Font.BOLD, 26));
        v.setForeground(color.darker());
        v.setAlignmentX(CENTER_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_MUTED);
        l.setForeground(UITheme.TEXT_MUTED);
        l.setAlignmentX(CENTER_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        inner.add(v);
        inner.add(l);
        box.add(inner);
        return box;
    }

    private JPanel recentOrdersCard(List<OrderResponse> orders) {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createCompoundBorder(card.getBorder(),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));

        JLabel title = new JLabel("Recent Orders");
        title.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 16));
        title.setForeground(UITheme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        List<OrderResponse> recent = new ArrayList<>(orders);
        recent.sort(Comparator.comparing(
                        (OrderResponse o) -> o.getOrderDate() == null ? LocalDateTime.MIN : o.getOrderDate())
                .reversed());
        int limit = Math.min(6, recent.size());

        if (limit == 0) {
            JLabel empty = new JLabel("No orders yet.");
            empty.setFont(UITheme.FONT_BODY);
            empty.setForeground(UITheme.TEXT_MUTED);
            empty.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
            list.add(empty);
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        for (int i = 0; i < limit; i++) {
            OrderResponse o = recent.get(i);
            String name = o.getRestaurant() != null && o.getRestaurant().getName() != null
                    ? o.getRestaurant().getName() : "Order";
            String customer = o.getUser() != null && o.getUser().getName() != null
                    ? o.getUser().getName() : "Guest";
            String date = o.getOrderDate() != null ? o.getOrderDate().format(fmt) : "—";
            boolean paid = o.getPayment() != null;
            list.add(orderRow(name, customer, date, money(o.getTotalPrice()), paid));
            if (i < limit - 1) list.add(rowDivider());
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel orderRow(String name, String customer, String date, String price, boolean paid) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JComponent icon = new IconBadge("🍜", CORAL, CORAL_SOFT);
        row.add(icon, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 14));
        nameLbl.setForeground(UITheme.TEXT_DARK);
        nameLbl.setAlignmentX(LEFT_ALIGNMENT);
        JLabel subLbl = new JLabel(customer + "  ·  " + date);
        subLbl.setFont(UITheme.FONT_MUTED);
        subLbl.setForeground(UITheme.TEXT_MUTED);
        subLbl.setAlignmentX(LEFT_ALIGNMENT);
        info.add(nameLbl);
        info.add(subLbl);
        row.add(info, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        JLabel priceLbl = new JLabel(price);
        priceLbl.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 14));
        priceLbl.setForeground(UITheme.TEXT_DARK);
        JLabel pill = paid
                ? UITheme.createBadge("Complete", UITheme.SUCCESS.darker(),
                UITheme.blend(UITheme.SUCCESS, Color.WHITE, 0.85f))
                : UITheme.createBadge("Pending", UITheme.ACCENT.darker(),
                UITheme.blend(UITheme.ACCENT, Color.WHITE, 0.85f));
        right.add(priceLbl);
        right.add(pill);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JComponent rowDivider() {
        JPanel d = new JPanel();
        d.setBackground(UITheme.BORDER);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        d.setPreferredSize(new Dimension(10, 1));
        return d;
    }

    // ---------- Helpers ----------

    private static String money(Double v) {
        double d = v == null ? 0 : v;
        return String.format("$%,.2f", d);
    }

    private static String money(double d) {
        return String.format("$%,.2f", d);
    }

    /** A rounded square icon badge with a tinted background and a centered glyph. */
    private static class IconBadge extends JComponent {
        private final String glyph;
        private final Color fg;
        private final Color bg;

        IconBadge(String glyph, Color fg, Color bg) {
            this.glyph = glyph;
            this.fg = fg;
            this.bg = bg;
            setPreferredSize(new Dimension(48, 48));
            setMaximumSize(new Dimension(48, 48));
            setMinimumSize(new Dimension(48, 48));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = Math.min(getWidth(), getHeight());
            int x = (getWidth() - s) / 2, y = (getHeight() - s) / 2;
            g2.setColor(bg);
            g2.fillRoundRect(x, y, s, s, 14, 14);
            g2.setColor(fg);
            g2.setFont(new Font("SansSerif", Font.PLAIN, Math.round(s * 0.5f)));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(glyph)) / 2;
            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(glyph, tx, ty);
            g2.dispose();
        }
    }

    /** A monthly revenue bar chart with gridlines and month labels. */
    private class RevenueChart extends JComponent {
        private final double[] monthly;
        private final String[] labels = {"Ja", "Fb", "Mr", "Ap", "My", "Jn",
                "Jl", "Au", "Sp", "Oc", "Nv", "Dc"};

        RevenueChart(double[] monthly) { this.monthly = monthly; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int leftPad = 46, bottomPad = 22, topPad = 8, rightPad = 6;
            int plotW = w - leftPad - rightPad;
            int plotH = h - topPad - bottomPad;

            double max = 0;
            for (double v : monthly) max = Math.max(max, v);
            double axisMax = niceMax(max);

            // Gridlines + y labels
            g2.setFont(new Font(UITheme.FONT_MUTED.getFamily(), Font.PLAIN, 10));
            int gridLines = 4;
            for (int i = 0; i <= gridLines; i++) {
                int y = topPad + (int) (plotH * (i / (double) gridLines));
                g2.setColor(UITheme.BORDER);
                g2.drawLine(leftPad, y, w - rightPad, y);
                double val = axisMax * (1 - i / (double) gridLines);
                g2.setColor(UITheme.TEXT_MUTED);
                String lbl = val >= 1000 ? String.format("$%.0fk", val / 1000) : String.format("$%.0f", val);
                g2.drawString(lbl, 4, y + 4);
            }

            // Bars
            int n = monthly.length;
            double slot = plotW / (double) n;
            int barW = (int) Math.max(6, slot * 0.5);
            int currentMonth = LocalDate.now().getMonthValue() - 1;
            for (int i = 0; i < n; i++) {
                double frac = axisMax == 0 ? 0 : monthly[i] / axisMax;
                int barH = (int) (plotH * frac);
                int bx = (int) (leftPad + slot * i + (slot - barW) / 2);
                int by = topPad + plotH - barH;
                g2.setColor(i == currentMonth ? CORAL : BAR_SOFT);
                g2.fillRoundRect(bx, by, barW, Math.max(barH, 2), 6, 6);
                g2.setColor(UITheme.TEXT_MUTED);
                g2.setFont(new Font(UITheme.FONT_MUTED.getFamily(), Font.PLAIN, 10));
                int lw = g2.getFontMetrics().stringWidth(labels[i]);
                g2.drawString(labels[i], (int) (leftPad + slot * i + (slot - lw) / 2), h - 6);
            }
            g2.dispose();
        }

        private double niceMax(double max) {
            if (max <= 0) return 100;
            double mag = Math.pow(10, Math.floor(Math.log10(max)));
            double norm = max / mag;
            double nice;
            if (norm <= 1) nice = 1;
            else if (norm <= 2) nice = 2;
            else if (norm <= 5) nice = 5;
            else nice = 10;
            return nice * mag;
        }
    }

    /** A semicircular performance gauge with a colored arc and centered readout. */
    private static class GaugePanel extends JComponent {
        private final int pct;
        private final double revenue;

        GaugePanel(int pct, double revenue) {
            this.pct = Math.max(0, Math.min(100, pct));
            this.revenue = revenue;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int size = Math.min(w, (h - 30) * 2);
            int arcX = (w - size) / 2;
            int arcY = (h - 30) - size / 2;
            int stroke = Math.max(14, size / 12);

            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Track
            g2.setColor(UITheme.BORDER);
            g2.draw(new Arc2D.Double(arcX + stroke / 2.0, arcY + stroke / 2.0,
                    size - stroke, size - stroke, 180, -180, Arc2D.OPEN));

            // Value arc
            g2.setColor(pct >= 60 ? UITheme.SUCCESS : pct >= 30 ? UITheme.ACCENT : UITheme.SECONDARY);
            g2.draw(new Arc2D.Double(arcX + stroke / 2.0, arcY + stroke / 2.0,
                    size - stroke, size - stroke, 180, -(180.0 * pct / 100.0), Arc2D.OPEN));

            // Center readout
            int cy = arcY + size / 2;
            g2.setColor(UITheme.TEXT_DARK);
            g2.setFont(new Font(UITheme.FONT_H1.getFamily(), Font.BOLD, 30));
            String val = pct + "%";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(val, (w - fm.stringWidth(val)) / 2, cy - 6);

            g2.setColor(UITheme.TEXT_MUTED);
            g2.setFont(new Font(UITheme.FONT_MUTED.getFamily(), Font.PLAIN, 12));
            String sub = "orders paid";
            g2.drawString(sub, (w - g2.getFontMetrics().stringWidth(sub)) / 2, cy + 14);

            g2.setFont(new Font(UITheme.FONT_MUTED.getFamily(), Font.PLAIN, 11));
            String rev = String.format("Total sale %s", money(revenue));
            g2.setColor(UITheme.TEXT_MUTED);
            g2.drawString(rev, (w - g2.getFontMetrics().stringWidth(rev)) / 2, h - 4);
            g2.dispose();
        }
    }
}