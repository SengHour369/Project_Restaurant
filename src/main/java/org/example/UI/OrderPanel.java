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
import java.awt.event.*;
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
    private JButton btnAddItem = new JButton("Add to Order");
    private JButton btnRemoveItem = new JButton("Remove");
    private JButton btnClearAll = new JButton("Clear All");
    private JButton btnCreateOrder = new JButton("Create Order");

    // Payment type combo box
    private JComboBox<String> cbPaymentType = new JComboBox<>(new String[]{"Credit Card", "Cash", "Mobile Payment", "Debit Card"});

    // Tables
    private DefaultTableModel cartModel = new DefaultTableModel(
            new String[]{"ID", "Menu Item", "Quantity", "Price", "Subtotal"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
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
    private JLabel lblRestaurant = new JLabel("Selected Restaurant: None");
    private JLabel lblTotal = new JLabel("Total: $0.00");

    // Split pane for responsive layout
    private JSplitPane splitPane;

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

        // Make it full screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(screenSize);

        // Main content panel with split
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);

        // Top panel - Create Order
        JPanel topPanel = createOrderPanel();
        splitPane.setTopComponent(topPanel);

        // Bottom panel - Order History
        JPanel bottomPanel = createHistoryPanel();
        splitPane.setBottomComponent(bottomPanel);

        add(splitPane, BorderLayout.CENTER);

        // Add component listener for resizing
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeComponents();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                // Set divider location after component is shown
                SwingUtilities.invokeLater(() -> {
                    if (splitPane != null && splitPane.getHeight() > 0) {
                        splitPane.setDividerLocation(0.5);
                    }
                });
            }
        });
    }

    private void resizeComponents() {
        if (splitPane != null && splitPane.getHeight() > 0) {
            splitPane.setDividerLocation(0.5);
        }
    }

    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel headerLabel = new JLabel("Create New Order");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerLabel.setForeground(Color.decode("#2c3e50"));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Center panel with two columns
        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplitPane.setResizeWeight(0.5);
        centerSplitPane.setBorder(null);

        // Left panel - Menu items
        JPanel menuPanel = createMenuPanel();
        centerSplitPane.setLeftComponent(menuPanel);

        // Right panel - Cart
        JPanel cartPanel = createCartPanel();
        centerSplitPane.setRightComponent(cartPanel);

        panel.add(centerSplitPane, BorderLayout.CENTER);

        // Bottom panel - Restaurant, Total and Payment
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        lblRestaurant.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblRestaurant.setForeground(Color.decode("#3498db"));

        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTotal.setForeground(Color.decode("#27ae60"));

        // Payment panel
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        paymentPanel.setBackground(Color.WHITE);

        JLabel lblPayment = new JLabel("Payment Type:");
        lblPayment.setFont(new Font("SansSerif", Font.BOLD, 14));

        cbPaymentType.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cbPaymentType.setPreferredSize(new Dimension(150, 30));

        paymentPanel.add(lblPayment);
        paymentPanel.add(cbPaymentType);
        paymentPanel.add(Box.createHorizontalStrut(20));

        // Create order button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnCreateOrder.setBackground(Color.decode("#2ecc71"));
        btnCreateOrder.setForeground(Color.WHITE);
        btnCreateOrder.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCreateOrder.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#27ae60"), 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        buttonPanel.add(btnCreateOrder);

        // Left side panel
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.add(lblRestaurant, BorderLayout.NORTH);
        leftPanel.add(paymentPanel, BorderLayout.SOUTH);

        // Right side panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(lblTotal, BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.EAST);

        bottomPanel.add(leftPanel, BorderLayout.WEST);
        bottomPanel.add(rightPanel, BorderLayout.CENTER);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#3498db"), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Panel title
        JLabel panelTitle = new JLabel("Menu Items");
        panelTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        panelTitle.setForeground(Color.decode("#2c3e50"));
        panelTitle.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(panelTitle, BorderLayout.NORTH);

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        txtMenuItemSearch.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtMenuItemSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(txtMenuItemSearch, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.SOUTH);

        // Menu items list
        DefaultListModel<MenuItemResponse> listModel = new DefaultListModel<>();
        JList<MenuItemResponse> menuList = new JList<>(listModel);
        menuList.setCellRenderer(new MenuItemRenderer());
        menuList.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(menuList);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel for adding items
        JPanel addItemPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        addItemPanel.setBackground(Color.WHITE);
        addItemPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel lblItem = new JLabel("Selected Item:");
        lblItem.setFont(new Font("SansSerif", Font.BOLD, 12));
        cbMenuItem.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cbMenuItem.setRenderer(new MenuItemComboRenderer());

        JLabel lblQuantity = new JLabel("Quantity:");
        lblQuantity.setFont(new Font("SansSerif", Font.BOLD, 12));
        spnQuantity.setFont(new Font("SansSerif", Font.PLAIN, 14));

        addItemPanel.add(lblItem);
        addItemPanel.add(cbMenuItem);
        addItemPanel.add(lblQuantity);
        addItemPanel.add(spnQuantity);

        // Empty cells for spacing
        addItemPanel.add(new JLabel());
        JPanel addButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addButtonPanel.setBackground(Color.WHITE);

        btnAddItem.setBackground(Color.decode("#3498db"));
        btnAddItem.setForeground(Color.WHITE);
        btnAddItem.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnAddItem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#2980b9"), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        addButtonPanel.add(btnAddItem);

        addItemPanel.add(addButtonPanel);

        panel.add(addItemPanel, BorderLayout.NORTH);

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
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#e74c3c"), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Panel title
        JLabel panelTitle = new JLabel("Your Cart");
        panelTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        panelTitle.setForeground(Color.decode("#2c3e50"));
        panelTitle.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(panelTitle, BorderLayout.NORTH);

        // Cart table
        cartTable.setRowHeight(35);
        cartTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        cartTable.getTableHeader().setBackground(Color.decode("#34495e"));
        cartTable.getTableHeader().setForeground(Color.WHITE);
        cartTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        cartTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
                } else {
                    c.setBackground(new Color(220, 240, 255));
                    c.setForeground(Color.BLACK);
                }

                // Right align price columns
                if (column == 3 || column == 4) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                }

                // Add padding
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Cart buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        btnRemoveItem.setBackground(Color.decode("#e74c3c"));
        btnRemoveItem.setForeground(Color.WHITE);
        btnRemoveItem.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnRemoveItem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#c0392b"), 1),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btnRemoveItem.setEnabled(false);

        btnClearAll.setBackground(Color.decode("#95a5a6"));
        btnClearAll.setForeground(Color.WHITE);
        btnClearAll.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnClearAll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#7f8c8d"), 1),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));

        buttonPanel.add(btnRemoveItem);
        buttonPanel.add(btnClearAll);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel headerLabel = new JLabel("Your Order History");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerLabel.setForeground(Color.decode("#2c3e50"));

        // Refresh button
        JButton btnRefresh = createIconButton("Refresh", Color.decode("#3498db"), 14);
        btnRefresh.addActionListener(e -> loadOrderHistory());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Order history table
        orderHistoryTable.setRowHeight(40);
        orderHistoryTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        orderHistoryTable.getTableHeader().setBackground(Color.decode("#34495e"));
        orderHistoryTable.getTableHeader().setForeground(Color.WHITE);
        orderHistoryTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        orderHistoryTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Custom renderer for history table
        orderHistoryTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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
                } else {
                    c.setBackground(new Color(220, 240, 255));
                    c.setForeground(Color.BLACK);
                }

                // Center align order ID
                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                }
                // Right align price
                else if (column == 2) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                }

                // Add padding
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                return c;
            }
        });

        // Add row sorter for history table
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(orderHistoryModel);
        orderHistoryTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(orderHistoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
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
        getActionMap().put("createOrder", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createOrder();
            }
        });

        // Delete to remove item from cart
        cartTable.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("DELETE"), "removeItem");
        cartTable.getActionMap().put("removeItem", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedItem();
            }
        });

        // F5 to refresh
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F5"), "refresh");
        getActionMap().put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadMenuItems();
                loadOrderHistory();
            }
        });
    }

    private void addItemToCart() {
        try {
            MenuItemResponse selectedItem = (MenuItemResponse) cbMenuItem.getSelectedItem();
            if (selectedItem == null) {
                showValidationError("Please select a menu item!", cbMenuItem);
                return;
            }

            int quantity = (Integer) spnQuantity.getValue();
            if (quantity <= 0) {
                showValidationError("Quantity must be at least 1!", spnQuantity);
                return;
            }

            // Check if item already exists in cart
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                if (cartModel.getValueAt(i, 0).equals(selectedItem.getId())) {
                    int currentQty = (int) cartModel.getValueAt(i, 2);
                    int newQty = currentQty + quantity;
                    cartModel.setValueAt(newQty, i, 2);
                    updateSubtotal(i);
                    updateTotalAndRestaurant();

                    // Update order item quantity
                    for (OrderItemRequest item : orderItems) {
                        if (item.getMenuItemId() == selectedItem.getId()) {
                            item.setQuantity(newQty);
                            break;
                        }
                    }

                    JOptionPane.showMessageDialog(this,
                            String.format("Updated quantity to %d x %s", newQty, selectedItem.getName()),
                            "Item Updated", JOptionPane.INFORMATION_MESSAGE);
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
                    String.format("Added %d x %s to cart!", quantity, selectedItem.getName()),
                    "Item Added", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error adding item: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showValidationError(String message, JComponent field) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
        field.requestFocus();
        if (field instanceof JTextField) {
            ((JTextField) field).selectAll();
        }
    }

    private void removeSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove!",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><div style='font-size:12pt;'>Remove this item from cart?</div></html>",
                "Confirm Removal",
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
                "<html><div style='font-size:12pt;'>Clear all items from cart?</div></html>",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            cartModel.setRowCount(0);
            orderItems.clear();
            lblRestaurant.setText("Selected Restaurant: None");
            lblTotal.setText("Total: $0.00");
        }
    }

    private void createOrder() {
        if (orderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Your cart is empty! Add some items first.",
                    "Empty Cart", JOptionPane.WARNING_MESSAGE);
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
                        "Multiple Restaurants", JOptionPane.WARNING_MESSAGE);
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

            // Show confirmation dialog
            StringBuilder orderSummary = new StringBuilder();
            orderSummary.append("<html><div style='font-size:12pt;'>");
            orderSummary.append("<b>Order Summary</b><br><br>");
            orderSummary.append("<b>Restaurant:</b> ").append(restaurant.getName()).append("<br>");
            orderSummary.append("<b>Items:</b><br>");

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                orderSummary.append("  • ").append(cartModel.getValueAt(i, 1))
                        .append(" x").append(cartModel.getValueAt(i, 2))
                        .append(" = ").append(cartModel.getValueAt(i, 4)).append("<br>");
            }

            orderSummary.append("<br><b>Total:</b> ").append(lblTotal.getText()).append("<br><br>");
            orderSummary.append("Confirm this order?</div></html>");

            int confirm = JOptionPane.showConfirmDialog(this,
                    orderSummary.toString(),
                    "Confirm Order",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) return;

            // Get payment type (no emoji)
            String paymentType = (String) cbPaymentType.getSelectedItem();

            // Create payment
            Payment payment = new Payment();
            payment.setAmount(total);

            // Set payment type based on selection
            if (paymentType != null) {
                if (paymentType.equals("Credit Card")) {
                    payment.setType("CREDIT_CARD");
                } else if (paymentType.equals("Cash")) {
                    payment.setType("CASH");
                } else if (paymentType.equals("Mobile Payment")) {
                    payment.setType("MOBILE");
                } else if (paymentType.equals("Debit Card")) {
                    payment.setType("DEBIT_CARD");
                } else {
                    payment.setType("CASH");
                }
            } else {
                payment.setType("CASH");
            }

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
                    "<html><div style='font-size:14pt;'>" +
                            "Order created successfully!<br><br>" +
                            "Order # will be generated by database<br>" +
                            "Total: " + lblTotal.getText() + "<br>" +
                            "Payment: " + payment.getAmount() +
                            "</div></html>",
                    "Order Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear cart and refresh
            clearCart();
            loadOrderHistory();

        } catch (MessageException ex) {
            JOptionPane.showMessageDialog(this,
                    "Order error: " + ex.getMessage(),
                    "Order Failed", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Unexpected error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewOrderDetails() {
        int selectedRow = orderHistoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order first!",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

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
                "Order Details #" + order.getId(), true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);

        StringBuilder details = new StringBuilder();
        details.append("╔══════════════════════════════════════╗\n");
        details.append("║         ORDER DETAILS                ║\n");
        details.append("╠══════════════════════════════════════╣\n\n");
        details.append("  Order ID: #").append(order.getId()).append("\n");
        details.append("  Date: ").append(order.getOrderDate()).append("\n");
        details.append("  Customer: ").append(order.getUser().getName()).append("\n");
        details.append("  Restaurant: ").append(order.getRestaurant().getName()).append("\n");
        details.append("  Status: ").append("Completed").append("\n");
        details.append("\n  ╔══════════════════════════════════╗\n");
        details.append("  ║              ITEMS                ║\n");
        details.append("  ╠══════════════════════════════════╣\n");

        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                MenuItemResponse menuItem = menuItemCache.get(item.getMenuItemId());
                String itemName = menuItem != null ? menuItem.getName() : "Item #" + item.getMenuItemId();
                details.append(String.format("  ║  %-20s x%d @ $%.2f ║\n",
                        truncateString(itemName, 20), item.getQuantity(), item.getPrice()));
                details.append(String.format("  ║              Subtotal: $%.2f       ║\n",
                        item.getQuantity() * item.getPrice()));
                details.append("  ╠──────────────────────────────╣\n");
            }
        }

        details.append("  ║                                  ║\n");
        details.append(String.format("  ║        TOTAL: $%.2f              ║\n", order.getTotalPrice()));
        details.append("  ╚══════════════════════════════════╝\n\n");

        if (order.getPayment() != null) {
            details.append("  Payment Information:\n");
            details.append(String.format("  Type: %s\n", order.getPayment().getType()));
            details.append(String.format("  Amount: $%.2f\n", order.getPayment().getAmount()));
        }

        details.append("\n╚══════════════════════════════════════╝\n");

        textArea.setText(details.toString());

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());
        buttonPanel.add(btnClose);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
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
        lblTotal.setText(String.format("Total: $%.2f", total));

        // Update restaurant info
        if (!orderItems.isEmpty()) {
            int firstItemId = orderItems.get(0).getMenuItemId();
            MenuItemResponse firstItem = menuItemCache.get(firstItemId);
            if (firstItem != null) {
                RestaurantResponse restaurant = findRestaurantById(firstItem.getRestaurant());
                if (restaurant != null) {
                    lblRestaurant.setText("Selected Restaurant: " + restaurant.getName());
                }
            }
        } else {
            lblRestaurant.setText("Selected Restaurant: None");
            btnRemoveItem.setEnabled(false);
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

            // Update UI if needed
            SwingUtilities.invokeLater(() -> {
                if (comboModel.getSize() > 0) {
                    cbMenuItem.setSelectedIndex(0);
                }
            });

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
            int userOrderCount = 0;
            for (OrderResponse order : orders) {
                if (order.getUser() != null && order.getUser().getId() == currentUser.getId()) {
                    String status = "Completed";
                    if (order.getOrderDate() != null &&
                            order.getOrderDate().isAfter(LocalDateTime.now().minusHours(1))) {
                        status = "Processing";
                    }

                    orderHistoryModel.addRow(new Object[]{
                            order.getId(),
                            order.getRestaurant().getName(),
                            String.format("$%.2f", order.getTotalPrice()),
                            order.getOrderDate().toString(),
                            status
                    });
                    userOrderCount++;
                }
            }

            // Show message if no orders found
            if (userOrderCount == 0) {
                JOptionPane.showMessageDialog(this,
                        "<html><div style='font-size:12pt;'>No order history found.<br>Create your first order above!</div></html>",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
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
                    String.valueOf(item.getId()).contains(searchText) ||
                    (item.getDescription() != null &&
                            item.getDescription().toLowerCase().contains(searchText))) {
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

    private JButton createIconButton(String text, Color color, int fontSize) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    // Custom renderers for better display
    class MenuItemRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof MenuItemResponse) {
                MenuItemResponse item = (MenuItemResponse) value;
                setText(String.format("%s - $%s", item.getName(), item.getPrice()));

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
                setText(String.format("%s - $%s", item.getName(), item.getPrice()));
            }

            return this;
        }
    }
}