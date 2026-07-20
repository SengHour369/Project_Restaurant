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

    private JTextField txtMenuItemSearch = new JTextField();
    private JComboBox<MenuItemResponse> cbMenuItem = new JComboBox<>();
    private JSpinner spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
    private JButton btnAddItem = UITheme.createButton("Add to Order", UITheme.PRIMARY);
    private JButton btnRemoveItem = UITheme.createButton("Remove", UITheme.SECONDARY);
    private JButton btnClearAll = UITheme.createButton("Clear All", UITheme.NEUTRAL);
    private JButton btnCreateOrder = UITheme.createButton("Create Order", UITheme.SUCCESS);
    // FIX: ComboBox values exactly match PostgreSQL enum payment_type
    private JComboBox<String> cbPaymentType = new JComboBox<>(new String[]{
            "Cash", "Credit Card", "Debit Card", "Bank Transfer", "Mobile Wallet"
    });

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

    private List<OrderItemRequest> orderItems = new ArrayList<>();
    private Map<Integer, MenuItemResponse> menuItemCache = new HashMap<>();
    private JLabel lblRestaurant = new JLabel("Selected Restaurant: None");
    private JLabel lblTotal = new JLabel("Total: $0.00");
    private JSplitPane splitPane;

    private int selectedRestaurantId = -1;

    public OrderPanel(UserResponse user) {
        this.currentUser = user;
        initializeUI();
        setupEventListeners();
        loadMenuItems();
        loadOrderHistory();
    }

    // ======================== SAFE PRICE PARSING ========================
    private double parsePriceSafely(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new NumberFormatException("Price is null or empty");
        }
        String cleaned = raw.replaceAll("[^\\d.]", "");
        if (cleaned.isEmpty()) {
            throw new NumberFormatException("No numeric value found in: " + raw);
        }
        int firstDot = cleaned.indexOf('.');
        if (firstDot != -1) {
            String before = cleaned.substring(0, firstDot);
            String after = cleaned.substring(firstDot + 1).replace(".", "");
            cleaned = before + "." + after;
        }
        return Double.parseDouble(cleaned);
    }

    // ======================== UI INITIALIZATION ========================
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.decode("#FFF6EC"));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(screenSize);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);

        JPanel topPanel = createOrderPanel();
        splitPane.setTopComponent(topPanel);

        JPanel bottomPanel = createHistoryPanel();
        splitPane.setBottomComponent(bottomPanel);

        add(splitPane, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeComponents();
            }
            @Override
            public void componentShown(ComponentEvent e) {
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
        panel.setBackground(Color.decode("#FFF6EC"));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("🍔 Create New Order");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerLabel.setForeground(Color.decode("#6B4226"));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#FFF6EC"));
        headerPanel.add(headerLabel, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplitPane.setResizeWeight(0.5);
        centerSplitPane.setBorder(null);

        JPanel menuPanel = createMenuPanel();
        centerSplitPane.setLeftComponent(menuPanel);

        JPanel cartPanel = createCartPanel();
        centerSplitPane.setRightComponent(cartPanel);

        panel.add(centerSplitPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.decode("#FFF6EC"));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        lblRestaurant.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblRestaurant.setForeground(Color.decode("#FF9F45"));

        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTotal.setForeground(Color.decode("#5FAD56"));

        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        paymentPanel.setBackground(Color.decode("#FFF6EC"));
        JLabel lblPayment = new JLabel("Payment Type:");
        lblPayment.setFont(new Font("SansSerif", Font.BOLD, 14));
        cbPaymentType.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cbPaymentType.setPreferredSize(new Dimension(150, 30));
        paymentPanel.add(lblPayment);
        paymentPanel.add(cbPaymentType);
        paymentPanel.add(Box.createHorizontalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.decode("#FFF6EC"));
        buttonPanel.add(btnCreateOrder);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.decode("#FFF6EC"));
        leftPanel.add(lblRestaurant, BorderLayout.NORTH);
        leftPanel.add(paymentPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.decode("#FFF6EC"));
        rightPanel.add(lblTotal, BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.EAST);

        bottomPanel.add(leftPanel, BorderLayout.WEST);
        bottomPanel.add(rightPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.decode("#FFF6EC"));
        panel.setBorder(new UITheme.RoundedLineBorder(UITheme.PRIMARY, 20, 15, 15));

        JLabel panelTitle = new JLabel("🍜 Menu Items");
        panelTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        panelTitle.setForeground(Color.decode("#6B4226"));
        panelTitle.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(panelTitle, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.decode("#FFF6EC"));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        txtMenuItemSearch.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtMenuItemSearch.setBorder(new UITheme.RoundedLineBorder(UITheme.NEUTRAL, 14, 8, 15));
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(txtMenuItemSearch, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.SOUTH);

        DefaultListModel<MenuItemResponse> listModel = new DefaultListModel<>();
        JList<MenuItemResponse> menuList = new JList<>(listModel);
        menuList.setCellRenderer(new MenuItemRenderer());
        menuList.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(menuList);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.decode("#FFF6EC"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel addItemPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        addItemPanel.setBackground(Color.decode("#FFF6EC"));
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

        addItemPanel.add(new JLabel());
        JPanel addButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addButtonPanel.setBackground(Color.decode("#FFF6EC"));
        addButtonPanel.add(btnAddItem);
        addItemPanel.add(addButtonPanel);
        panel.add(addItemPanel, BorderLayout.NORTH);

        txtMenuItemSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterMenuItems(listModel, menuList); }
            @Override public void removeUpdate(DocumentEvent e) { filterMenuItems(listModel, menuList); }
            @Override public void changedUpdate(DocumentEvent e) { filterMenuItems(listModel, menuList); }
        });

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
        panel.setBackground(Color.decode("#FFF6EC"));
        panel.setBorder(new UITheme.RoundedLineBorder(UITheme.SECONDARY, 20, 15, 15));

        JLabel panelTitle = new JLabel("🛒 Your Cart");
        panelTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        panelTitle.setForeground(Color.decode("#6B4226"));
        panelTitle.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(panelTitle, BorderLayout.NORTH);

        cartTable.setRowHeight(35);
        cartTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        cartTable.getTableHeader().setBackground(Color.decode("#8B5E3C"));
        cartTable.getTableHeader().setForeground(Color.WHITE);
        cartTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        cartTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        cartTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.decode("#FFF6EC"));
                    } else {
                        c.setBackground(new Color(255, 244, 230));
                    }
                } else {
                    c.setBackground(new Color(255, 224, 178));
                    c.setForeground(Color.BLACK);
                }
                if (column == 3 || column == 4) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                }
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.decode("#FFF6EC"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(Color.decode("#FFF6EC"));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        btnRemoveItem.setEnabled(false);

        buttonPanel.add(btnRemoveItem);
        buttonPanel.add(btnClearAll);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#FFF6EC"));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("📋 Your Order History");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerLabel.setForeground(Color.decode("#6B4226"));

        JButton btnRefresh = createIconButton("Refresh", Color.decode("#FF9F45"), 14);
        btnRefresh.addActionListener(e -> loadOrderHistory());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#FFF6EC"));
        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        orderHistoryTable.setRowHeight(40);
        orderHistoryTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        orderHistoryTable.getTableHeader().setBackground(Color.decode("#8B5E3C"));
        orderHistoryTable.getTableHeader().setForeground(Color.WHITE);
        orderHistoryTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        orderHistoryTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        orderHistoryTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.decode("#FFF6EC"));
                    } else {
                        c.setBackground(new Color(255, 244, 230));
                    }
                } else {
                    c.setBackground(new Color(255, 224, 178));
                    c.setForeground(Color.BLACK);
                }
                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (column == 2) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                }
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return c;
            }
        });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(orderHistoryModel);
        orderHistoryTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(orderHistoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void setupEventListeners() {
        btnAddItem.addActionListener(e -> addItemToCart());
        btnRemoveItem.addActionListener(e -> removeSelectedItem());
        btnClearAll.addActionListener(e -> clearCart());
        btnCreateOrder.addActionListener(e -> createOrder());

        setupKeyboardShortcuts();

        cartTable.getSelectionModel().addListSelectionListener(e -> {
            btnRemoveItem.setEnabled(cartTable.getSelectedRow() != -1);
        });

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
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("control ENTER"), "createOrder");
        getActionMap().put("createOrder", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createOrder();
            }
        });

        cartTable.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("DELETE"), "removeItem");
        cartTable.getActionMap().put("removeItem", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedItem();
            }
        });

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

    // ======================== ADD ITEM TO CART ========================
    private void addItemToCart() {
        try {
            MenuItemResponse selectedItem = (MenuItemResponse) cbMenuItem.getSelectedItem();
            if (selectedItem == null) {
                JOptionPane.showMessageDialog(this, "Please select a menu item!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int quantity = (Integer) spnQuantity.getValue();
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be at least 1!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int restaurantId = selectedItem.getRestaurant();
            if (restaurantId <= 0) {
                JOptionPane.showMessageDialog(this,
                        "This menu item doesn't have a valid restaurant assigned!\nPlease contact administrator.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if items are from same restaurant
            if (!orderItems.isEmpty()) {
                int existingRestaurantId = getCurrentRestaurantId();
                if (existingRestaurantId != restaurantId) {
                    int response = JOptionPane.showConfirmDialog(this,
                            "<html><div style='font-size:12pt;'>" +
                                    "You're trying to add items from a different restaurant.<br>" +
                                    "Your cart already has items from another restaurant.<br><br>" +
                                    "<b>Would you like to clear the cart and start over?</b>" +
                                    "</div></html>",
                            "Different Restaurant",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (response == JOptionPane.YES_OPTION) {
                        clearCartSilently();
                    } else {
                        return;
                    }
                }
            }

            // ---------- SAFE PRICE PARSING ----------
            double price;
            try {
                price = parsePriceSafely(selectedItem.getPrice());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid price format for item: " + selectedItem.getName() +
                                "\nPrice value: " + selectedItem.getPrice() +
                                "\n\nPlease contact administrator to fix this item's price.",
                        "Price Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if already in cart
            Integer itemId = selectedItem.getId();
            boolean found = false;
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                Object idObj = cartModel.getValueAt(i, 0);
                if (idObj instanceof Integer && ((Integer) idObj).equals(itemId)) {
                    int currentQty = (int) cartModel.getValueAt(i, 2);
                    int newQty = currentQty + quantity;
                    cartModel.setValueAt(newQty, i, 2);
                    updateSubtotal(i);
                    updateTotalAndRestaurant();

                    for (OrderItemRequest item : orderItems) {
                        if (item.getMenuItemId() == itemId) {
                            item.setQuantity(newQty);
                            break;
                        }
                    }
                    found = true;
                    JOptionPane.showMessageDialog(this,
                            String.format("Updated quantity to %d x %s", newQty, selectedItem.getName()),
                            "Item Updated", JOptionPane.INFORMATION_MESSAGE);
                    break;
                }
            }

            if (!found) {
                double subtotal = price * quantity;
                cartModel.addRow(new Object[]{
                        selectedItem.getId(),
                        selectedItem.getName(),
                        quantity,
                        String.format("$%.2f", price),
                        String.format("$%.2f", subtotal)
                });

                OrderItemRequest orderItem = new OrderItemRequest();
                orderItem.setMenuItemId(selectedItem.getId());
                orderItem.setQuantity(quantity);
                orderItem.setPrice(price);
                orderItems.add(orderItem);

                selectedRestaurantId = restaurantId;

                updateTotalAndRestaurant();
                spnQuantity.setValue(1);
                JOptionPane.showMessageDialog(this,
                        String.format("Added %d x %s to cart!", quantity, selectedItem.getName()),
                        "Item Added", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error adding item: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper method to get current restaurant ID from cart
    private int getCurrentRestaurantId() {
        if (orderItems.isEmpty()) return -1;
        int firstItemId = orderItems.get(0).getMenuItemId();
        MenuItemResponse item = menuItemCache.get(firstItemId);
        return item != null ? item.getRestaurant() : -1;
    }

    // Clear cart without confirmation (used when switching restaurants)
    private void clearCartSilently() {
        cartModel.setRowCount(0);
        orderItems.clear();
        selectedRestaurantId = -1;
        lblRestaurant.setText("Selected Restaurant: None");
        lblTotal.setText("Total: $0.00");
    }

    // ======================== REMOVE SELECTED ITEM ========================
    private void removeSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><div style='font-size:12pt;'>Remove this item from cart?</div></html>",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            int itemId = (int) cartModel.getValueAt(selectedRow, 0);
            cartModel.removeRow(selectedRow);
            orderItems.removeIf(item -> item.getMenuItemId() == itemId);

            if (orderItems.isEmpty()) {
                selectedRestaurantId = -1;
                lblRestaurant.setText("Selected Restaurant: None");
            }
            updateTotalAndRestaurant();
        }
    }

    // ======================== CLEAR CART ========================
    private void clearCart() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is already empty!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><div style='font-size:12pt;'>Clear all items from cart?</div></html>",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            clearCartSilently();
        }
    }

    private void updateSubtotal(int row) {
        int quantity = (int) cartModel.getValueAt(row, 2);
        String priceStr = cartModel.getValueAt(row, 3).toString().replace("$", "");
        double price = Double.parseDouble(priceStr);
        double subtotal = quantity * price;
        cartModel.setValueAt(String.format("$%.2f", subtotal), row, 4);
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

        if (!orderItems.isEmpty() && selectedRestaurantId > 0) {
            RestaurantResponse restaurant = findRestaurantById(selectedRestaurantId);
            if (restaurant != null) {
                lblRestaurant.setText("Selected Restaurant: " + restaurant.getName());
            }
        } else if (!orderItems.isEmpty()) {
            int firstItemId = orderItems.get(0).getMenuItemId();
            MenuItemResponse firstItem = menuItemCache.get(firstItemId);
            if (firstItem != null) {
                RestaurantResponse restaurant = findRestaurantById(firstItem.getRestaurant());
                if (restaurant != null) {
                    lblRestaurant.setText("Selected Restaurant: " + restaurant.getName());
                    selectedRestaurantId = firstItem.getRestaurant();
                }
            }
        } else {
            lblRestaurant.setText("Selected Restaurant: None");
            btnRemoveItem.setEnabled(false);
            selectedRestaurantId = -1;
        }
    }

    private double calculateTotal() {
        double total = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String subtotalStr = cartModel.getValueAt(i, 4).toString().replace("$", "");
            try {
                total += Double.parseDouble(subtotalStr);
            } catch (NumberFormatException e) {
                // Skip invalid entries
            }
        }
        return total;
    }

    // ======================== CREATE ORDER (FIXED) ========================
    private void createOrder() {
        if (orderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Your cart is empty! Add some items first.",
                    "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentUser == null || currentUser.getId() <= 0) {
            JOptionPane.showMessageDialog(this,
                    "You must be logged in to place an order.\nPlease log out and log in again.",
                    "User Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (selectedRestaurantId <= 0) {
                JOptionPane.showMessageDialog(this,
                        "No restaurant selected. Please add items from a valid restaurant.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            RestaurantResponse restaurant = findRestaurantById(selectedRestaurantId);
            if (restaurant == null) {
                JOptionPane.showMessageDialog(this,
                        "Restaurant not found in the database.\nRestaurant ID: " + selectedRestaurantId,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (OrderItemRequest item : orderItems) {
                if (item.getMenuItemId() <= 0 || item.getQuantity() <= 0 || item.getPrice() <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid item data in cart. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            double total = calculateTotal();

            StringBuilder orderSummary = new StringBuilder();
            orderSummary.append("<html><div style='font-size:12pt;'>");
            orderSummary.append("<b>Order Summary</b><br><br>");
            orderSummary.append("<b>Restaurant:</b> ").append(restaurant.getName()).append("<br>");
            orderSummary.append("<b>Items:</b><br>");

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                orderSummary.append("  - ").append(cartModel.getValueAt(i, 1))
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

            // ---------- FIX: Use payment type as-is (no transformation) ----------
            String paymentType = (String) cbPaymentType.getSelectedItem();
            if (paymentType == null || paymentType.trim().isEmpty()) {
                paymentType = "Credit Card";  // fallback – must be a valid enum value
            }

            Payment payment = new Payment();
            payment.setAmount(total);
            payment.setType(paymentType);  // keep exactly as selected
            // --------------------------------------------------------------------

            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setOrderDate(LocalDateTime.now());
            orderRequest.setTotalPrice(total);
            orderRequest.setUser(currentUser);
            orderRequest.setRestaurant(restaurant);
            orderRequest.setOrderItems(orderItems);
            orderRequest.setPayment(payment);

            orderService.createOrder(orderRequest);

            JOptionPane.showMessageDialog(this,
                    "<html><div style='font-size:14pt;'>" +
                            "✅ Order created successfully!<br><br>" +
                            "Total: " + lblTotal.getText() + "<br>" +
                            "Payment: " + payment.getType() +
                            "</div></html>",
                    "Order Success", JOptionPane.INFORMATION_MESSAGE);

            clearCartSilently();
            loadOrderHistory();

        } catch (MessageException ex) {
            JOptionPane.showMessageDialog(this,
                    "Order error: " + ex.getMessage(),
                    "Order Failed", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Unexpected error: " + ex.getMessage() + "\n\nPlease check the console for details.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ======================== OTHER METHODS ========================
    private void loadMenuItems() {
        try {
            List<MenuItemResponse> menuItems = menuService.getAllMenuItems();
            menuItemCache.clear();
            DefaultComboBoxModel<MenuItemResponse> comboModel = new DefaultComboBoxModel<>();
            for (MenuItemResponse item : menuItems) {
                menuItemCache.put(item.getId(), item);
                comboModel.addElement(item);
            }
            cbMenuItem.setModel(comboModel);
            SwingUtilities.invokeLater(() -> {
                if (comboModel.getSize() > 0) cbMenuItem.setSelectedIndex(0);
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
            if (currentUser == null || currentUser.getId() <= 0) {
                return;
            }
            List<OrderResponse> orders = orderService.findOrdersByUserId(currentUser.getId());
            for (OrderResponse order : orders) {
                if (order != null && order.getUser() != null && order.getRestaurant() != null) {
                    String status = "Completed";
                    if (order.getOrderDate() != null &&
                            order.getOrderDate().isAfter(LocalDateTime.now().minusHours(1))) {
                        status = "Processing";
                    }
                    orderHistoryModel.addRow(new Object[]{
                            order.getId(),
                            order.getRestaurant().getName(),
                            String.format("$%.2f", order.getTotalPrice()),
                            order.getOrderDate() != null ? order.getOrderDate().toString() : "N/A",
                            status
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading order history: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterMenuItems(DefaultListModel<MenuItemResponse> model, JList<MenuItemResponse> list) {
        String searchText = txtMenuItemSearch.getText().toLowerCase();
        model.clear();
        for (MenuItemResponse item : menuItemCache.values()) {
            if (searchText.isEmpty() ||
                    item.getName().toLowerCase().contains(searchText) ||
                    String.valueOf(item.getId()).contains(searchText) ||
                    (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchText))) {
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
            // ignore
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
            // ignore
        }
        return null;
    }

    private void viewOrderDetails() {
        int selectedRow = orderHistoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order first!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = orderHistoryTable.convertRowIndexToModel(selectedRow);
        int orderId = Integer.parseInt(orderHistoryModel.getValueAt(modelRow, 0).toString());
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
        textArea.setBackground(Color.decode("#FFF6EC"));

        StringBuilder details = new StringBuilder();
        details.append("╔══════════════════════════════════════╗\n");
        details.append("║         ORDER DETAILS                ║\n");
        details.append("╠══════════════════════════════════════╣\n\n");
        details.append("  Order ID: #").append(order.getId()).append("\n");
        details.append("  Date: ").append(order.getOrderDate()).append("\n");
        details.append("  Customer: ").append(order.getUser().getName()).append("\n");
        details.append("  Restaurant: ").append(order.getRestaurant().getName()).append("\n");
        details.append("  Status: Completed\n");
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
        JButton btnClose = UITheme.createButton("Close", UITheme.NEUTRAL);
        btnClose.addActionListener(e -> dialog.dispose());
        buttonPanel.add(btnClose);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    private JButton createIconButton(String text, Color color, int fontSize) {
        return UITheme.createButton(text, color);
    }

    // Custom renderers
    class MenuItemRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof MenuItemResponse) {
                MenuItemResponse item = (MenuItemResponse) value;
                setText(String.format("%s - $%s", item.getName(), item.getPrice()));
                if (isSelected) {
                    setBackground(Color.decode("#FF9F45"));
                    setForeground(Color.WHITE);
                } else {
                    if (index % 2 == 0) setBackground(Color.decode("#FFF6EC"));
                    else setBackground(new Color(255, 244, 230));
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