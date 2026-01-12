package org.example.UI;

import org.example.DTO.Response.OrderResponse;
import org.example.DTO.Response.UserResponse;
import org.example.Service.ServiceImplement.ServiceOrderImp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderHistoryPanel extends JPanel {
    private final ServiceOrderImp orderService = new ServiceOrderImp();
    private final UserResponse currentUser;
    private final DefaultTableModel tableModel;
    private final JTable orderTable;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // UI Components
    private final JLabel lblTotalOrders = new JLabel("📊 Total Orders: 0");
    private final JLabel lblTotalSpent = new JLabel("💰 Total Spent: $0.00");
    private final JComboBox<String> filterCombo = new JComboBox<>(new String[]{
            "All Orders", "Last 7 Days", "Last 30 Days", "This Month", "High Value (>$50)", "Low Value (<$20)"
    });

    public OrderHistoryPanel(UserResponse user) {
        this.currentUser = user;

        // Create table model with emoji icons in headers
        tableModel = new DefaultTableModel(
                new String[]{"📦 Order ID", "📅 Date", "🏢 Restaurant", "🍽️ Items", "💰 Total", "✅ Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        orderTable = new JTable(tableModel);
        initializeUI();
        loadOrderHistory();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);

        // Bottom Panel with statistics
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#f8f9fa"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Title
        JLabel titleLabel = new JLabel("📜 Your Order History");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.decode("#2c3e50"));

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(Color.decode("#f8f9fa"));

        JLabel filterLabel = new JLabel("🔍 Filter:");
        filterLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        filterCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        filterCombo.setBackground(Color.WHITE);
        filterCombo.addActionListener(e -> filterOrders());

        JButton btnRefresh = createStyledButton("🔄 Refresh", Color.decode("#3498db"), 14);
        btnRefresh.addActionListener(e -> loadOrderHistory());

        filterPanel.add(filterLabel);
        filterPanel.add(filterCombo);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(btnRefresh);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(filterPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Style the table
        orderTable.setRowHeight(40);
        orderTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        orderTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        orderTable.getTableHeader().setBackground(Color.decode("#34495e"));
        orderTable.getTableHeader().setForeground(Color.WHITE);

        // Add alternating row colors
        orderTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(240, 248, 255)); // Light blue
                    }
                }

                // Style for total column
                if (column == 4 && value != null) {
                    setForeground(Color.decode("#27ae60")); // Green color for money
                    setFont(getFont().deriveFont(Font.BOLD));
                }

                // Center align ID and Date columns
                if (column == 0 || column == 1) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                }

                return c;
            }
        });

        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Empty state label
        JLabel emptyLabel = new JLabel("📭 No orders found. Start ordering!", SwingConstants.CENTER);
        emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 16));
        emptyLabel.setForeground(Color.GRAY);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(emptyLabel, BorderLayout.SOUTH);

        // Show/hide empty label based on data
        tableModel.addTableModelListener(e -> {
            emptyLabel.setVisible(tableModel.getRowCount() == 0);
        });

        // Add double-click listener to view order details
        orderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = orderTable.getSelectedRow();
                    if (row != -1) {
                        showOrderDetails(row);
                    }
                }
            }
        });

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 15));
        panel.setBackground(Color.decode("#f1f8ff"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Style statistics labels
        lblTotalOrders.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTotalOrders.setForeground(Color.decode("#2c3e50"));

        lblTotalSpent.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTotalSpent.setForeground(Color.decode("#27ae60"));

        // Average order value
        JLabel lblAvgOrder = new JLabel("📈 Average Order: $0.00");
        lblAvgOrder.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblAvgOrder.setForeground(Color.decode("#7f8c8d"));

        panel.add(lblTotalOrders);
        panel.add(lblTotalSpent);
        panel.add(lblAvgOrder);

        return panel;
    }

    private void loadOrderHistory() {
        tableModel.setRowCount(0);

        List<OrderResponse> orders = orderService.findAllOrders();

        if (orders == null || orders.isEmpty()) {
            updateStatistics(0, 0.0);
            return;
        }

        // Filter orders for current user
        List<OrderResponse> userOrders = orders.stream()
                .filter(order -> order.getUser() != null && order.getUser().getId() == currentUser.getId())
                .toList();

        double totalSpent = 0.0;

        for (OrderResponse order : userOrders) {
            // Format date
            String formattedDate = order.getOrderDate() != null
                    ? order.getOrderDate().format(dateFormatter)
                    : "N/A";

            // Count items
            int itemCount = order.getOrderItems() != null ? order.getOrderItems().size() : 0;

            // Get restaurant name
            String restaurantName = order.getRestaurant() != null
                    ? order.getRestaurant().getName()
                    : "Unknown Restaurant";

            // Determine status with emoji
            String status = getOrderStatusEmoji(order);

            tableModel.addRow(new Object[]{
                    String.format("#%04d", order.getId()), // Format as #0001, #0002, etc.
                    formattedDate,
                    restaurantName,
                    itemCount + " item" + (itemCount != 1 ? "s" : ""),
                    String.format("$%.2f", order.getTotalPrice()),
                    status
            });

            totalSpent += order.getTotalPrice();
        }

        updateStatistics(userOrders.size(), totalSpent);
    }

    private String getOrderStatusEmoji(OrderResponse order) {
        // This is a simplified version - you might have actual status fields
        // Add your own logic based on your OrderResponse model
        if (order.getPayment() != null) {
            return "✅ Paid";
        }
        return "⏳ Processing";
    }

    private void filterOrders() {
        String selectedFilter = (String) filterCombo.getSelectedItem();
        // Implement filtering logic here
        JOptionPane.showMessageDialog(this,
                "Filtering by: " + selectedFilter + "\n(Filter implementation depends on your data structure)",
                "Filter Applied", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStatistics(int totalOrders, double totalSpent) {
        lblTotalOrders.setText("📊 Total Orders: " + totalOrders);
        lblTotalSpent.setText("💰 Total Spent: $" + String.format("%.2f", totalSpent));

        // You could add more statistics here
    }

    private void showOrderDetails(int rowIndex) {
        String orderId = tableModel.getValueAt(rowIndex, 0).toString();
        String restaurant = tableModel.getValueAt(rowIndex, 2).toString();
        String items = tableModel.getValueAt(rowIndex, 3).toString();
        String total = tableModel.getValueAt(rowIndex, 4).toString();
        String date = tableModel.getValueAt(rowIndex, 1).toString();

        // Create a detailed view dialog
        JDialog detailDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "📋 Order Details", true);
        detailDialog.setLayout(new BorderLayout());
        detailDialog.setSize(500, 400);
        detailDialog.setLocationRelativeTo(this);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#3498db"));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel headerLabel = new JLabel(orderId + " - " + restaurant);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        contentPanel.setBackground(Color.WHITE);

        addDetailRow("📅 Order Date:", date, contentPanel);
        addDetailRow("🏢 Restaurant:", restaurant, contentPanel);
        addDetailRow("🍽️ Items:", items, contentPanel);
        addDetailRow("💰 Total Amount:", total, contentPanel);
        addDetailRow("✅ Status:", "Delivered successfully!", contentPanel);

        // Add some spacing
        contentPanel.add(Box.createVerticalStrut(20));

        // Item list (simplified - you would populate this with actual items)
        JTextArea itemDetails = new JTextArea();
        itemDetails.setText("🍔 Cheeseburger x2 - $18.00\n🍟 French Fries x1 - $4.50\n🥤 Cola x1 - $2.50");
        itemDetails.setFont(new Font("Monospaced", Font.PLAIN, 14));
        itemDetails.setEditable(false);
        itemDetails.setBorder(BorderFactory.createTitledBorder("📝 Order Items"));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnReorder = createStyledButton("🔄 Reorder This", Color.decode("#9b59b6"), 14);
        JButton btnClose = createStyledButton("✖️ Close", Color.decode("#95a5a6"), 14);

        btnClose.addActionListener(e -> detailDialog.dispose());

        buttonPanel.add(btnReorder);
        buttonPanel.add(btnClose);

        // Assemble dialog
        detailDialog.add(headerPanel, BorderLayout.NORTH);
        detailDialog.add(new JScrollPane(contentPanel), BorderLayout.CENTER);
        detailDialog.add(buttonPanel, BorderLayout.SOUTH);

        detailDialog.setVisible(true);
    }

    private void addDetailRow(String label, String value, JPanel panel) {
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(Color.decode("#2c3e50"));

        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.PLAIN, 14));
        val.setForeground(Color.decode("#7f8c8d"));

        rowPanel.add(lbl, BorderLayout.WEST);
        rowPanel.add(val, BorderLayout.EAST);
        panel.add(rowPanel);
        panel.add(Box.createVerticalStrut(10));
    }

    private JButton createStyledButton(String text, Color color, int fontSize) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    // Custom cell renderer class
    class DefaultTableCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}