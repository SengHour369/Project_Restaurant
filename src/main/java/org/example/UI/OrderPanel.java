package org.example.UI;

import org.example.DTO.Request.OrderItemRequest;
import org.example.DTO.Request.OrderRequest;
import org.example.DTO.Response.MenuItemResponse;
import org.example.DTO.Response.OrderResponse;
import org.example.DTO.Response.RestaurantResponse;
import org.example.DTO.Response.UserResponse;
import org.example.Exception.MessageException;
import org.example.Model.OrderItem;
import org.example.Model.Payment;
import org.example.Service.ServiceImplement.ServiceMenuItemImp;
import org.example.Service.ServiceImplement.ServiceOrderImp;
import org.example.Service.ServiceImplement.ServiceRestaurantImp;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public class OrderPanel extends JPanel {
    private final ServiceMenuItemImp menuService = new ServiceMenuItemImp();
    private final ServiceOrderImp orderService = new ServiceOrderImp();
    private final ServiceRestaurantImp restaurantService = new ServiceRestaurantImp();

    private UserResponse currentUser;

    // UI Components
    private JTextField txtMenuItemSearch = new JTextField();
    private JComboBox<MenuItemResponse> cbMenuItem = new JComboBox<>();
    private JSpinner spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
    private JButton btnAddItem = new JButton("➕ Add to Order");
    private JButton btnRemoveItem = new JButton("🗑️ Remove");
    private JButton btnClearAll = new JButton("🧹 Clear All");
    private JButton btnCreateOrder = new JButton("✅ Create Order");

    // Tables
    private DefaultTableModel cartModel = new DefaultTableModel(
            new String[]{"ID", "Menu Item", "Quantity", "Price", "Subtotal"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Make table non-editable
        }
    };
    private JTable cartTable = new JTable(cartModel);

    private DefaultTableModel orderHistoryModel = new DefaultTableModel(
            new String[]{"Order ID", "Restaurant", "Total", "Date", "Status"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private JTable orderHistoryTable = new JTable(orderHistoryModel);

    // Data
    private List<OrderItemRequest> orderItems = new ArrayList<>();
    private Map<Integer, MenuItemResponse> menuItemCache = new HashMap<>();
    private JLabel lblRestaurant = new JLabel("🏢 Selected Restaurant: None");
    private JLabel lblTotal = new JLabel("💰 Total: $0.00");

    public OrderPanel(UserResponse user) {
        this.currentUser = user;
        initializeUI();
        setupEventListeners();
        loadMenuItems();
        loadOrderHistory();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Main content panel with split
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.5);

        // Top panel - Create Order
        JPanel topPanel = createOrderPanel();
        splitPane.setTopComponent(topPanel);

        // Bottom panel - Order History
        JPanel bottomPanel = createHistoryPanel();
        splitPane.setBottomComponent(bottomPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JLabel headerLabel = new JLabel("🛒 Create New Order");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerLabel.setForeground(Color.decode("#2c3e50"));
        panel.add(headerLabel, BorderLayout.NORTH);

        // Center panel with two columns
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setBackground(Color.WHITE);

        // Left panel - Menu items
        JPanel menuPanel = createMenuPanel();
        centerPanel.add(menuPanel);

        // Right panel - Cart
        JPanel cartPanel = createCartPanel();
        centerPanel.add(cartPanel);

        panel.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel - Restaurant and Total
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        lblRestaurant.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblRestaurant.setForeground(Color.decode("#3498db"));

        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTotal.setForeground(Color.decode("#27ae60"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnCreateOrder);

        bottomPanel.add(lblRestaurant, BorderLayout.WEST);
        bottomPanel.add(lblTotal, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "🍽️ Menu Items"));

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

        JLabel searchLabel = new JLabel("🔍 Search:");
        txtMenuItemSearch.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtMenuItemSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(txtMenuItemSearch, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Menu items list
        DefaultListModel<MenuItemResponse> listModel = new DefaultListModel<>();
        JList<MenuItemResponse> menuList = new JList<>(listModel);
        menuList.setCellRenderer(new MenuItemRenderer());
        menuList.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(menuList);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add item panel
        JPanel addItemPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        addItemPanel.setBackground(Color.WHITE);
        addItemPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));

        JLabel lblQuantity = new JLabel("Quantity:");
        spnQuantity.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JLabel lblItem = new JLabel("Selected Item:");
        cbMenuItem.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cbMenuItem.setRenderer(new MenuItemComboRenderer());

        addItemPanel.add(lblItem);
        addItemPanel.add(cbMenuItem);
        addItemPanel.add(lblQuantity);
        addItemPanel.add(spnQuantity);

        panel.add(addItemPanel, BorderLayout.SOUTH);

        // Add button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnAddItem);
        panel.add(buttonPanel, BorderLayout.EAST);

        // Update search filter
        txtMenuItemSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterMenuItems(listModel, menuList);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterMenuItems(listModel, menuList);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterMenuItems(listModel, menuList);
            }
        });

        // List selection listener
        menuList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                MenuItemResponse selected = menuList.getSelectedValue();
                if (selected != null) {
                    cbMenuItem.setSelectedItem(selected);
                }
            }
        });

        return panel;
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "📦 Your Cart"));

        // Cart table
        cartTable.setRowHeight(30);
        cartTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        cartTable.getTableHeader().setBackground(Color.decode("#34495e"));
        cartTable.getTableHeader().setForeground(Color.WHITE);
        cartTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Add alternating row colors
        cartTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(240, 248, 255));
                    }
                }

                // Right align price columns
                if (column == 3 || column == 4) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Cart buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);

        btnRemoveItem.setBackground(Color.decode("#e74c3c"));
        btnRemoveItem.setForeground(Color.WHITE);
        btnRemoveItem.setFont(new Font("SansSerif", Font.BOLD, 12));

        btnClearAll.setBackground(Color.decode("#95a5a6"));
        btnClearAll.setForeground(Color.WHITE);
        btnClearAll.setFont(new Font("SansSerif", Font.BOLD, 12));

        buttonPanel.add(btnRemoveItem);
        buttonPanel.add(btnClearAll);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JLabel headerLabel = new JLabel("📜 Your Order History");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerLabel.setForeground(Color.decode("#2c3e50"));

        // Refresh button
        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadOrderHistory());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Order history table
        orderHistoryTable.setRowHeight(35);
        orderHistoryTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        orderHistoryTable.getTableHeader().setBackground(Color.decode("#34495e"));
        orderHistoryTable.getTableHeader().setForeground(Color.WHITE);
        orderHistoryTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Add row sorter for history table
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(orderHistoryModel);
        orderHistoryTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(orderHistoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void setupEventListeners() {
        // Add item to cart
        btnAddItem.addActionListener(e -> addItemToCart());

        // Remove selected item from cart
        btnRemoveItem.addActionListener(e -> removeSelectedItem());

        // Clear all items from cart
        btnClearAll.addActionListener(e -> clearCart());

        // Create order
        btnCreateOrder.addActionListener(e -> createOrder());

        // Keyboard shortcuts
        setupKeyboardShortcuts();

        // Cart table selection
        cartTable.getSelectionModel().addListSelectionListener(e -> {
            btnRemoveItem.setEnabled(cartTable.getSelectedRow() != -1);
        });

        // Order history table double-click
        orderHistoryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewOrderDetails();
                }
            }
        });
    }

    private void setupKeyboardShortcuts() {
        // Ctrl+Enter to create order
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("control ENTER"), "createOrder");
        getActionMap().put("createOrder", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                createOrder();
            }
        });

        // Delete to remove item from cart
        cartTable.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("DELETE"), "removeItem");
        cartTable.getActionMap().put("removeItem", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                removeSelectedItem();
            }
        });

        // F5 to refresh
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F5"), "refresh");
        getActionMap().put("refresh", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                loadMenuItems();
                loadOrderHistory();
            }
        });
    }

    private void addItemToCart() {
        try {
            MenuItemResponse selectedItem = (MenuItemResponse) cbMenuItem.getSelectedItem();
            if (selectedItem == null) {
                JOptionPane.showMessageDialog(this, "Please select a menu item!",
                        "⚠️ No Item Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int quantity = (Integer) spnQuantity.getValue();
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be at least 1!",
                        "⚠️ Invalid Quantity", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check if item already exists in cart
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                if (cartModel.getValueAt(i, 0).equals(selectedItem.getId())) {
                    int currentQty = (int) cartModel.getValueAt(i, 2);
                    cartModel.setValueAt(currentQty + quantity, i, 2);
                    updateSubtotal(i);
                    updateTotalAndRestaurant();
                    return;
                }
            }

            // Add new item to cart
            double price = Double.parseDouble(selectedItem.getPrice());
            double subtotal = price * quantity;

            cartModel.addRow(new Object[]{
                    selectedItem.getId(),
                    selectedItem.getName(),
                    quantity,
                    String.format("$%.2f", price),
                    String.format("$%.2f", subtotal)
            });

            // Add to order items list
            OrderItemRequest orderItem = new OrderItemRequest();
            orderItem.setMenuItemId(selectedItem.getId());
            orderItem.setQuantity(quantity);
            orderItem.setPrice(price);
            orderItems.add(orderItem);

            updateTotalAndRestaurant();

            // Reset quantity
            spnQuantity.setValue(1);

            // Show success message
            JOptionPane.showMessageDialog(this,
                    String.format("✅ Added %d x %s to cart!", quantity, selectedItem.getName()),
                    "Item Added", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Error adding item: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove!",
                    "⚠️ No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove this item from cart?", "Confirm Removal",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            int itemId = (int) cartModel.getValueAt(selectedRow, 0);
            cartModel.removeRow(selectedRow);

            // Remove from order items list
            orderItems.removeIf(item -> item.getMenuItemId() == itemId);

            updateTotalAndRestaurant();
        }
    }

    private void clearCart() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is already empty!",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Clear all items from cart?", "Confirm Clear",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            cartModel.setRowCount(0);
            orderItems.clear();
            lblRestaurant.setText("🏢 Selected Restaurant: None");
            lblTotal.setText("💰 Total: $0.00");
        }
    }

    private void createOrder() {
        if (orderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Your cart is empty! Add some items first.",
                    "⚠️ Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Determine restaurant (all items should be from same restaurant)
            Set<Integer> restaurantIds = new HashSet<>();
            for (OrderItemRequest item : orderItems) {
                MenuItemResponse menuItem = menuItemCache.get(item.getMenuItemId());
                if (menuItem != null) {
                    restaurantIds.add(menuItem.getRestaurant());
                }
            }

            if (restaurantIds.size() != 1) {
                JOptionPane.showMessageDialog(this,
                        "All items must be from the same restaurant!",
                        "⚠️ Multiple Restaurants", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int restaurantId = restaurantIds.iterator().next();
            RestaurantResponse restaurant = findRestaurantById(restaurantId);

            if (restaurant == null) {
                JOptionPane.showMessageDialog(this,
                        "Restaurant not found!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double total = calculateTotal();

            // Show payment dialog
            JTextField txtPayment = new JTextField(String.format("%.2f", total));
            txtPayment.setFont(new Font("SansSerif", Font.BOLD, 14));

            Object[] message = {
                    "Restaurant: " + restaurant.getName(),
                    "Total Amount: $" + String.format("%.2f", total),
                    "Enter payment amount:",
                    txtPayment
            };

            int option = JOptionPane.showConfirmDialog(this, message,
                    "💳 Payment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (option != JOptionPane.OK_OPTION) return;

            double paymentAmount;
            try {
                paymentAmount = Double.parseDouble(txtPayment.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid payment amount!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (paymentAmount < total) {
                JOptionPane.showMessageDialog(this,
                        "Payment amount is less than total!",
                        "⚠️ Insufficient Payment", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create payment
            Payment payment = new Payment();
            payment.setAmount(paymentAmount);
            payment.setType(paymentAmount > total ? "CASH_WITH_CHANGE" : "CASH");

            // Create order request
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setOrderDate(LocalDateTime.now());
            orderRequest.setTotalPrice(total);
            orderRequest.setUser(currentUser);
            orderRequest.setRestaurant(restaurant);
            orderRequest.setOrderItems(orderItems);
            orderRequest.setPayment(payment);

            // Process order
            orderService.createOrder(orderRequest);

            JOptionPane.showMessageDialog(this,
                    String.format("✅ Order created successfully!\nTotal: $%.2f\nPayment: $%.2f\nChange: $%.2f",
                            total, paymentAmount, paymentAmount - total),
                    "🎉 Order Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear cart and refresh
            clearCart();
            loadOrderHistory();

        } catch (MessageException ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Order error: " + ex.getMessage(),
                    "Order Failed", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Unexpected error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewOrderDetails() {
        int selectedRow = orderHistoryTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = orderHistoryTable.convertRowIndexToModel(selectedRow);
        int orderId = Integer.parseInt(orderHistoryModel.getValueAt(modelRow, 0).toString());

        // Show order details dialog
        try {
            OrderResponse order = findOrderById(orderId);
            if (order != null) {
                showOrderDetailsDialog(order);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading order details: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showOrderDetailsDialog(OrderResponse order) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "📋 Order Details #" + order.getId(), true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        StringBuilder details = new StringBuilder();
        details.append("================================\n");
        details.append("         ORDER DETAILS         \n");
        details.append("================================\n\n");
        details.append("Order ID: #").append(order.getId()).append("\n");
        details.append("Date: ").append(order.getOrderDate()).append("\n");
        details.append("Customer: ").append(order.getUser().getName()).append("\n");
        details.append("Restaurant: ").append(order.getRestaurant().getName()).append("\n");
        details.append("--------------------------------\n");
        details.append("ITEMS:\n");

        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                MenuItemResponse menuItem = menuItemCache.get(item.getMenuItemId());
                String itemName = menuItem != null ? menuItem.getName() : "Item #" + item.getMenuItemId();
                details.append(String.format("  %s x%d @ $%.2f = $%.2f\n",
                        itemName, item.getQuantity(), item.getPrice(),
                        item.getQuantity() * item.getPrice()));
            }
        }

        details.append("--------------------------------\n");
        details.append(String.format("Total: $%.2f\n", order.getTotalPrice()));
        details.append(String.format("Payment: %s - $%.2f\n",
                order.getPayment().getType(), order.getPayment().getAmount()));
        details.append("================================\n");

        textArea.setText(details.toString());

        dialog.add(new JScrollPane(textArea));
        dialog.setVisible(true);
    }

    private void updateSubtotal(int row) {
        int quantity = (int) cartModel.getValueAt(row, 2);
        String priceStr = cartModel.getValueAt(row, 3).toString().replace("$", "");
        double price = Double.parseDouble(priceStr);
        double subtotal = quantity * price;
        cartModel.setValueAt(String.format("$%.2f", subtotal), row, 4);

        // Update order items list
        int itemId = (int) cartModel.getValueAt(row, 0);
        for (OrderItemRequest item : orderItems) {
            if (item.getMenuItemId() == itemId) {
                item.setQuantity(quantity);
                break;
            }
        }
    }

    private void updateTotalAndRestaurant() {
        double total = calculateTotal();
        lblTotal.setText(String.format("💰 Total: $%.2f", total));

        // Update restaurant info
        if (!orderItems.isEmpty()) {
            int firstItemId = orderItems.get(0).getMenuItemId();
            MenuItemResponse firstItem = menuItemCache.get(firstItemId);
            if (firstItem != null) {
                RestaurantResponse restaurant = findRestaurantById(firstItem.getRestaurant());
                if (restaurant != null) {
                    lblRestaurant.setText("🏢 Selected Restaurant: " + restaurant.getName());
                }
            }
        } else {
            lblRestaurant.setText("🏢 Selected Restaurant: None");
        }
    }

    private double calculateTotal() {
        double total = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String subtotalStr = cartModel.getValueAt(i, 4).toString().replace("$", "");
            total += Double.parseDouble(subtotalStr);
        }
        return total;
    }

    private void loadMenuItems() {
        try {
            List<MenuItemResponse> menuItems = menuService.getAllMenuItems();
            menuItemCache.clear();

            DefaultComboBoxModel<MenuItemResponse> comboModel =
                    new DefaultComboBoxModel<>();

            for (MenuItemResponse item : menuItems) {
                menuItemCache.put(item.getId(), item);
                comboModel.addElement(item);
            }

            cbMenuItem.setModel(comboModel);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading menu items: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadOrderHistory() {
        try {
            orderHistoryModel.setRowCount(0);
            List<OrderResponse> orders = orderService.findAllOrders();

            // Filter orders for current user
            for (OrderResponse order : orders) {
                if (order.getUser() != null && order.getUser().getId() == currentUser.getId()) {
                    orderHistoryModel.addRow(new Object[]{
                            order.getId(),
                            order.getRestaurant().getName(),
                            String.format("$%.2f", order.getTotalPrice()),
                            order.getOrderDate().toString(),
                            "✅ Completed"
                    });
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading order history: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterMenuItems(DefaultListModel<MenuItemResponse> model,
                                 JList<MenuItemResponse> list) {
        String searchText = txtMenuItemSearch.getText().toLowerCase();
        model.clear();

        for (MenuItemResponse item : menuItemCache.values()) {
            if (searchText.isEmpty() ||
                    item.getName().toLowerCase().contains(searchText) ||
                    String.valueOf(item.getId()).contains(searchText)) {
                model.addElement(item);
            }
        }
    }

    private RestaurantResponse findRestaurantById(int id) {
        try {
            List<RestaurantResponse> restaurants = restaurantService.findAllRestaurants();
            for (RestaurantResponse restaurant : restaurants) {
                if (restaurant.getId() == id) {
                    return restaurant;
                }
            }
        } catch (Exception ex) {
            // Ignore - return null
        }
        return null;
    }

    private OrderResponse findOrderById(int id) {
        try {
            List<OrderResponse> orders = orderService.findAllOrders();
            for (OrderResponse order : orders) {
                if (order.getId() == id) {
                    return order;
                }
            }
        } catch (Exception ex) {
            // Ignore - return null
        }
        return null;
    }

    // Custom renderers for better display
    class MenuItemRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof MenuItemResponse) {
                MenuItemResponse item = (MenuItemResponse) value;
                setText(String.format("🍽️ %s - $%s", item.getName(), item.getPrice()));

                if (isSelected) {
                    setBackground(Color.decode("#3498db"));
                    setForeground(Color.WHITE);
                } else {
                    if (index % 2 == 0) {
                        setBackground(Color.WHITE);
                    } else {
                        setBackground(new Color(240, 248, 255));
                    }
                    setForeground(Color.BLACK);
                }
            }

            return this;
        }
    }

    class MenuItemComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof MenuItemResponse) {
                MenuItemResponse item = (MenuItemResponse) value;
                setText(String.format("🍽️ %s - $%s", item.getName(), item.getPrice()));
            }

            return this;
        }
    }
}