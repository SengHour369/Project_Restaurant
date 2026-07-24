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
import org.example.Service.ServiceExcel;
import org.example.Service.ServiceImplement.ServiceExcelImp;
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
    private final ServiceExcel excelService = new ServiceExcelImp();

    private static final String ORDERS_EXCEL_PATH = "exports/orders_export.xlsx";
    private static final String ORDERS_EXCEL_SHEET = "Orders";
    private static final List<String> ORDERS_EXCEL_HEADERS = List.of(
            "Order ID", "Order Date", "Customer", "Restaurant",
            "Item", "Quantity", "Unit Price", "Subtotal",
            "Order Total", "Payment Type"
    );

    private UserResponse currentUser;

    private final JTextField txtMenuItemSearch = new JTextField();
    private JToggleButton btnVegFilter;
    private JToggleButton btnNonVegFilter;

    private final JPanel menuGridPanel = new JPanel(new GridLayout(0, 3, 14, 14));

    private final JPanel cartListPanel = new JPanel();
    private final JLabel lblSubTotal = new JLabel("Sub Total: $0.00");
    private final JLabel lblTax = new JLabel("Tax: $0.00");
    private final JLabel lblGrandTotal = new JLabel("Total: $0.00");

    private JButton btnClearAll = UITheme.createButton("Clear All", UITheme.NEUTRAL);
    private JButton btnCheckout = UITheme.createButton("Check Out", UITheme.SUCCESS);
    // FIX: ComboBox values exactly match PostgreSQL enum payment_type
    private JComboBox<String> cbPaymentType = new JComboBox<>(new String[]{
            "Cash", "Credit Card", "Debit Card", "Bank Transfer", "Mobile Wallet"
    });

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
    private JSplitPane splitPane;

    // Restaurant photo picker
    private final DefaultListModel<RestaurantResponse> restaurantListModel = new DefaultListModel<>();
    private final JList<RestaurantResponse> restaurantList = new JList<>(restaurantListModel);

    // Restaurant id -> image path, used as a fallback thumbnail for menu items
    private final Map<Integer, String> restaurantImageById = new HashMap<>();

    private int selectedRestaurantId = -1;

    public OrderPanel(UserResponse user) {
        this.currentUser = user;
        initializeUI();
        setupEventListeners();
        loadRestaurants();
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
        setBackground(Color.decode("#F5F6F8"));
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
        panel.setBackground(Color.decode("#F5F6F8"));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("🍜 Menu Category");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerLabel.setForeground(Color.decode("#111827"));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#F5F6F8"));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.decode("#F5F6F8"));
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(createRestaurantStripPanel(), BorderLayout.SOUTH);
        panel.add(topContainer, BorderLayout.NORTH);

        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplitPane.setResizeWeight(0.65);
        centerSplitPane.setBorder(null);

        JPanel menuPanel = createMenuPanel();
        centerSplitPane.setLeftComponent(menuPanel);

        JPanel cartPanel = createCartPanel();
        centerSplitPane.setRightComponent(cartPanel);

        panel.add(centerSplitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRestaurantStripPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.decode("#F5F6F8"));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        JLabel label = new JLabel("🏪 Choose a Restaurant (click a photo):");
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(Color.decode("#111827"));
        panel.add(label, BorderLayout.NORTH);

        restaurantList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        restaurantList.setVisibleRowCount(1);
        restaurantList.setCellRenderer(new RestaurantPhotoRenderer());
        restaurantList.setFixedCellWidth(100);
        restaurantList.setFixedCellHeight(100);
        restaurantList.setBackground(Color.decode("#F5F6F8"));
        restaurantList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(restaurantList,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(100, 110));
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPane, BorderLayout.CENTER);

        restaurantList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                RestaurantResponse selected = restaurantList.getSelectedValue();
                if (selected != null) onRestaurantChosen(selected);
            }
        });

        return panel;
    }

    private void onRestaurantChosen(RestaurantResponse restaurant) {
        if (restaurant.getId() == -1) {
            selectedRestaurantId = -1;
            lblRestaurant.setText("Selected Restaurant: None");
            refreshMenuItemsList();
            return;
        }

        if (!orderItems.isEmpty() && selectedRestaurantId > 0 && selectedRestaurantId != restaurant.getId()) {
            int response = JOptionPane.showConfirmDialog(this,
                    "<html><div style='font-size:12pt;'>" +
                            "Switching restaurants will clear your current cart.<br><br>" +
                            "<b>Continue?</b></div></html>",
                    "Switch Restaurant", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response != JOptionPane.YES_OPTION) {
                return;
            }
            clearCartSilently();
        }

        selectedRestaurantId = restaurant.getId();
        lblRestaurant.setText("Selected Restaurant: " + restaurant.getName());
        refreshMenuItemsList();
    }

    private void loadRestaurants() {
        try {
            restaurantListModel.clear();

            RestaurantResponse allEntry = new RestaurantResponse(-1);
            allEntry.setName("All Restaurants");
            restaurantListModel.addElement(allEntry);

            List<RestaurantResponse> restaurants = restaurantService.findAllRestaurants();
            restaurantImageById.clear();
            for (RestaurantResponse restaurant : restaurants) {
                restaurantListModel.addElement(restaurant);
                restaurantImageById.put(restaurant.getId(), restaurant.getImage_path());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading restaurants: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ======================== MENU GRID (left side) ========================
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.decode("#F5F6F8"));
        panel.setBorder(new UITheme.RoundedLineBorder(UITheme.PRIMARY, 20, 15, 15));

        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JLabel panelTitle = new JLabel("🍜 Menu");
        panelTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        panelTitle.setForeground(Color.decode("#111827"));

        txtMenuItemSearch.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtMenuItemSearch.setBorder(new UITheme.RoundedLineBorder(UITheme.NEUTRAL, 14, 8, 15));
        txtMenuItemSearch.setPreferredSize(new Dimension(220, 36));

        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setOpaque(false);
        searchRow.add(panelTitle, BorderLayout.WEST);
        searchRow.add(txtMenuItemSearch, BorderLayout.EAST);
        topBar.add(searchRow, BorderLayout.NORTH);

        btnVegFilter = createFilterChip("🟢 Veg", UITheme.SUCCESS);
        btnNonVegFilter = createFilterChip("🔴 Non-Veg", UITheme.SECONDARY);
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterRow.setOpaque(false);
        filterRow.add(btnVegFilter);
        filterRow.add(btnNonVegFilter);
        topBar.add(filterRow, BorderLayout.SOUTH);

        panel.add(topBar, BorderLayout.NORTH);

        menuGridPanel.setOpaque(false);
        menuGridPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(menuGridPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(gridWrapper);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.decode("#F5F6F8"));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        txtMenuItemSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshMenuItemsList(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshMenuItemsList(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshMenuItemsList(); }
        });

        return panel;
    }

    private JToggleButton createFilterChip(String text, Color color) {
        JToggleButton btn = new JToggleButton(text);
        btn.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setOpaque(true);
        styleFilterChip(btn, color);
        btn.addItemListener(e -> {
            styleFilterChip(btn, color);
            refreshMenuItemsList();
        });
        return btn;
    }

    private void styleFilterChip(JToggleButton btn, Color color) {
        if (btn.isSelected()) {
            btn.setBackground(color);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(UITheme.blend(color, Color.WHITE, 0.85f));
            btn.setForeground(color.darker());
        }
    }

    /** One clickable card in the menu grid: thumbnail, name, price, and a small "+" to add to cart. */
    private JPanel createMenuCard(MenuItemResponse item) {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createCompoundBorder(card.getBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        card.setPreferredSize(new Dimension(170, 190));

        JLabel thumb = new JLabel(thumbFor(item, 140, 90));
        thumb.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(thumb, BorderLayout.NORTH);

        JLabel name = new JLabel("<html>" + item.getName() + "</html>");
        name.setFont(new Font("SansSerif", Font.BOLD, 14));
        name.setForeground(UITheme.TEXT_DARK);
        card.add(name, BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);

        JLabel price = new JLabel("$" + item.getPrice());
        price.setFont(new Font("SansSerif", Font.BOLD, 14));
        price.setForeground(UITheme.SUCCESS);
        bottomRow.add(price, BorderLayout.WEST);

        JButton btnAdd = UITheme.createButton("+", UITheme.PRIMARY);
        btnAdd.setPreferredSize(new Dimension(34, 30));
        btnAdd.addActionListener(e -> addItemToCart(item, 1));
        bottomRow.add(btnAdd, BorderLayout.EAST);

        card.add(bottomRow, BorderLayout.SOUTH);

        return card;
    }

    // ======================== CART (right side) ========================
    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.decode("#F5F6F8"));
        panel.setBorder(new UITheme.RoundedLineBorder(UITheme.SECONDARY, 20, 15, 15));

        JLabel panelTitle = new JLabel("🛒 My Cart");
        panelTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        panelTitle.setForeground(Color.decode("#111827"));
        panelTitle.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(panelTitle, BorderLayout.NORTH);

        cartListPanel.setLayout(new BoxLayout(cartListPanel, BoxLayout.Y_AXIS));
        cartListPanel.setOpaque(false);

        JPanel cartWrapper = new JPanel(new BorderLayout());
        cartWrapper.setOpaque(false);
        cartWrapper.add(cartListPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(cartWrapper);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.decode("#F5F6F8"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        lblRestaurant.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblRestaurant.setForeground(Color.decode("#4F46E5"));
        lblRestaurant.setAlignmentX(LEFT_ALIGNMENT);
        footer.add(lblRestaurant);
        footer.add(Box.createVerticalStrut(8));

        JPanel totalsPanel = new JPanel();
        totalsPanel.setOpaque(false);
        totalsPanel.setLayout(new BoxLayout(totalsPanel, BoxLayout.Y_AXIS));

        lblSubTotal.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblTax.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblGrandTotal.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblGrandTotal.setForeground(Color.decode("#16A34A"));
        lblSubTotal.setAlignmentX(LEFT_ALIGNMENT);
        lblTax.setAlignmentX(LEFT_ALIGNMENT);
        lblGrandTotal.setAlignmentX(LEFT_ALIGNMENT);

        totalsPanel.add(lblSubTotal);
        totalsPanel.add(Box.createVerticalStrut(4));
        totalsPanel.add(lblTax);
        totalsPanel.add(Box.createVerticalStrut(4));
        totalsPanel.add(new JSeparator());
        totalsPanel.add(Box.createVerticalStrut(4));
        totalsPanel.add(lblGrandTotal);
        totalsPanel.setAlignmentX(LEFT_ALIGNMENT);
        footer.add(totalsPanel);
        footer.add(Box.createVerticalStrut(12));

        JPanel paymentRow = new JPanel(new BorderLayout(10, 0));
        paymentRow.setOpaque(false);
        paymentRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lblPayment = new JLabel("Payment:");
        lblPayment.setFont(new Font("SansSerif", Font.BOLD, 13));
        cbPaymentType.setFont(new Font("SansSerif", Font.PLAIN, 13));
        paymentRow.add(lblPayment, BorderLayout.WEST);
        paymentRow.add(cbPaymentType, BorderLayout.CENTER);
        footer.add(paymentRow);
        footer.add(Box.createVerticalStrut(12));

        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(LEFT_ALIGNMENT);
        buttonRow.add(btnClearAll);
        buttonRow.add(btnCheckout);
        footer.add(buttonRow);

        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    /** One row in the cart list: thumbnail, name, qty stepper, line price, remove control. */
    private JPanel createCartRow(OrderItemRequest orderItem) {
        MenuItemResponse menuItem = menuItemCache.get(orderItem.getMenuItemId());
        String name = menuItem != null ? menuItem.getName() : "Item #" + orderItem.getMenuItemId();

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(8, 4, 8, 4)));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel thumb = new JLabel(menuItem != null ? thumbFor(menuItem, 44, 44) : null);
        row.add(thumb, BorderLayout.WEST);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel priceLabel = new JLabel(String.format("$%.2f x %d = $%.2f",
                orderItem.getPrice(), orderItem.getQuantity(), orderItem.getPrice() * orderItem.getQuantity()));
        priceLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        priceLabel.setForeground(UITheme.TEXT_MUTED);
        priceLabel.setAlignmentX(LEFT_ALIGNMENT);
        center.add(nameLabel);
        center.add(priceLabel);
        row.add(center, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        controls.setOpaque(false);
        JButton btnMinus = smallCartButton("-");
        JLabel qty = new JLabel(String.valueOf(orderItem.getQuantity()));
        qty.setFont(new Font("SansSerif", Font.BOLD, 13));
        JButton btnPlus = smallCartButton("+");
        JButton btnRemove = smallCartButton("×");
        btnRemove.setForeground(UITheme.SECONDARY);

        btnMinus.addActionListener(e -> changeCartQuantity(orderItem.getMenuItemId(), -1));
        btnPlus.addActionListener(e -> changeCartQuantity(orderItem.getMenuItemId(), 1));
        btnRemove.addActionListener(e -> removeCartItem(orderItem.getMenuItemId()));

        controls.add(btnMinus);
        controls.add(qty);
        controls.add(btnPlus);
        controls.add(btnRemove);
        row.add(controls, BorderLayout.EAST);

        return row;
    }

    private JButton smallCartButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(26, 26));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ======================== ORDER HISTORY (bottom half) ========================
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#F5F6F8"));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("📋 Your Order History");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerLabel.setForeground(Color.decode("#111827"));

        JButton btnRefresh = createIconButton("Refresh", Color.decode("#4F46E5"), 14);
        btnRefresh.addActionListener(e -> loadOrderHistory());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#F5F6F8"));
        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        orderHistoryTable.setRowHeight(40);
        orderHistoryTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        orderHistoryTable.getTableHeader().setBackground(Color.decode("#4F46E5"));
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
                        c.setBackground(Color.decode("#F5F6F8"));
                    } else {
                        c.setBackground(new Color(249, 250, 251));
                    }
                } else {
                    c.setBackground(new Color(224, 231, 255));
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
        btnClearAll.addActionListener(e -> clearCart());
        btnCheckout.addActionListener(e -> createOrder());

        setupKeyboardShortcuts();

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

    // ======================== CART MUTATIONS ========================
    private void addItemToCart(MenuItemResponse selectedItem, int quantity) {
        try {
            int restaurantId = selectedItem.getRestaurant();
            if (restaurantId <= 0) {
                JOptionPane.showMessageDialog(this,
                        "This menu item doesn't have a valid restaurant assigned!\nPlease contact administrator.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

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

            Integer itemId = selectedItem.getId();
            for (OrderItemRequest item : orderItems) {
                if (item.getMenuItemId() == itemId) {
                    item.setQuantity(item.getQuantity() + quantity);
                    selectedRestaurantId = restaurantId;
                    refreshCart();
                    return;
                }
            }

            OrderItemRequest orderItem = new OrderItemRequest();
            orderItem.setMenuItemId(itemId);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(price);
            orderItems.add(orderItem);

            selectedRestaurantId = restaurantId;
            refreshCart();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error adding item: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changeCartQuantity(int menuItemId, int delta) {
        for (Iterator<OrderItemRequest> it = orderItems.iterator(); it.hasNext(); ) {
            OrderItemRequest item = it.next();
            if (item.getMenuItemId() == menuItemId) {
                int newQty = item.getQuantity() + delta;
                if (newQty <= 0) {
                    it.remove();
                } else {
                    item.setQuantity(newQty);
                }
                break;
            }
        }
        if (orderItems.isEmpty()) {
            selectedRestaurantId = -1;
            lblRestaurant.setText("Selected Restaurant: None");
        }
        refreshCart();
    }

    private void removeCartItem(int menuItemId) {
        orderItems.removeIf(item -> item.getMenuItemId() == menuItemId);
        if (orderItems.isEmpty()) {
            selectedRestaurantId = -1;
            lblRestaurant.setText("Selected Restaurant: None");
        }
        refreshCart();
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
        orderItems.clear();
        selectedRestaurantId = -1;
        lblRestaurant.setText("Selected Restaurant: None");
        refreshCart();
    }

    // ======================== CLEAR CART ========================
    private void clearCart() {
        if (orderItems.isEmpty()) {
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

    /** Rebuilds the cart row list, the restaurant label and the sub total/tax/total labels. */
    private void refreshCart() {
        cartListPanel.removeAll();
        for (OrderItemRequest item : orderItems) {
            cartListPanel.add(createCartRow(item));
        }
        cartListPanel.revalidate();
        cartListPanel.repaint();

        if (!orderItems.isEmpty() && selectedRestaurantId > 0) {
            RestaurantResponse restaurant = findRestaurantById(selectedRestaurantId);
            if (restaurant != null) {
                lblRestaurant.setText("Selected Restaurant: " + restaurant.getName());
            }
        } else if (orderItems.isEmpty()) {
            lblRestaurant.setText("Selected Restaurant: None");
        }

        double subTotal = calculateTotal();
        // No tax model exists anywhere in Order/Payment/DB, so this stays $0.00 rather
        // than fabricating a rate that isn't actually charged.
        double tax = 0;
        lblSubTotal.setText(String.format("Sub Total: $%.2f", subTotal));
        lblTax.setText(String.format("Tax: $%.2f", tax));
        lblGrandTotal.setText(String.format("Total: $%.2f", subTotal + tax));
    }

    private double calculateTotal() {
        double total = 0;
        for (OrderItemRequest item : orderItems) {
            total += item.getPrice() * item.getQuantity();
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

            for (OrderItemRequest item : orderItems) {
                MenuItemResponse menuItem = menuItemCache.get(item.getMenuItemId());
                String itemName = menuItem != null ? menuItem.getName() : "Item #" + item.getMenuItemId();
                orderSummary.append("  - ").append(itemName)
                        .append(" x").append(item.getQuantity())
                        .append(" = $").append(String.format("%.2f", item.getPrice() * item.getQuantity()))
                        .append("<br>");
            }

            orderSummary.append("<br><b>Total:</b> $").append(String.format("%.2f", total)).append("<br><br>");
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

            OrderResponse createdOrder = orderService.createOrder(orderRequest);

            String excelNote = "";
            try {
                java.io.File excelFile = exportOrderToExcel(createdOrder);
                excelNote = "<br>Saved to: " + excelFile.getPath();
            } catch (Exception exportEx) {
                exportEx.printStackTrace();
                excelNote = "<br><font color='#B91C1C'>Warning: could not save order to Excel (" + exportEx.getMessage() + ")</font>";
            }

            JOptionPane.showMessageDialog(this,
                    "<html><div style='font-size:14pt;'>" +
                            "✅ Order created successfully!<br><br>" +
                            "Total: " + lblGrandTotal.getText().replace("Total: ", "") + "<br>" +
                            "Payment: " + payment.getType() +
                            excelNote +
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

    // ======================== EXCEL EXPORT ========================
    // Builds headers/rows dynamically from the order and hands them to the generic ServiceExcel.
    private java.io.File exportOrderToExcel(OrderResponse order) throws org.example.Exception.MessageException {
        String customer = order.getUser() != null ? order.getUser().getName() : "";
        String restaurant = order.getRestaurant() != null ? order.getRestaurant().getName() : "";
        String orderDate = order.getOrderDate() != null ? order.getOrderDate().toString() : "";
        String paymentType = (order.getPayment() != null && order.getPayment().getType() != null)
                ? order.getPayment().getType() : "";
        double orderTotal = order.getTotalPrice() != null ? order.getTotalPrice() : 0;

        List<List<Object>> rows = new ArrayList<>();
        List<OrderItem> items = order.getOrderItems();
        if (items == null || items.isEmpty()) {
            rows.add(Arrays.asList(order.getId(), orderDate, customer, restaurant,
                    "", 0, 0, 0, orderTotal, paymentType));
        } else {
            for (OrderItem item : items) {
                MenuItemResponse menuItem = menuItemCache.get(item.getMenuItemId());
                String itemName = menuItem != null ? menuItem.getName() : "Item #" + item.getMenuItemId();
                double price = item.getPrice() != null ? item.getPrice() : 0;
                int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                rows.add(Arrays.asList(order.getId(), orderDate, customer, restaurant,
                        itemName, quantity, price, quantity * price,
                        orderTotal, paymentType));
            }
        }

        return excelService.appendToExcel(ORDERS_EXCEL_PATH, ORDERS_EXCEL_SHEET, ORDERS_EXCEL_HEADERS, rows);
    }

    // ======================== OTHER METHODS ========================
    private void loadMenuItems() {
        try {
            List<MenuItemResponse> menuItems = menuService.getAllMenuItems();
            menuItemCache.clear();
            for (MenuItemResponse item : menuItems) {
                menuItemCache.put(item.getId(), item);
            }
            refreshMenuItemsList();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading menu items: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Rebuilds the menu card grid from the cache, applying the search text, the
    // restaurant chosen from the photo strip, and the Veg/Non-Veg filter chips.
    private void refreshMenuItemsList() {
        String searchText = txtMenuItemSearch.getText().trim().toLowerCase();
        boolean vegOnly = btnVegFilter != null && btnVegFilter.isSelected();
        boolean nonVegOnly = btnNonVegFilter != null && btnNonVegFilter.isSelected();

        menuGridPanel.removeAll();

        for (MenuItemResponse item : menuItemCache.values()) {
            if (selectedRestaurantId > 0 && item.getRestaurant() != selectedRestaurantId) {
                continue;
            }
            if (!searchText.isEmpty() &&
                    !(item.getName().toLowerCase().contains(searchText) ||
                            String.valueOf(item.getId()).contains(searchText) ||
                            (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchText)))) {
                continue;
            }
            if (vegOnly && !nonVegOnly && !Boolean.TRUE.equals(item.getIsVeg())) {
                continue;
            }
            if (nonVegOnly && !vegOnly && Boolean.TRUE.equals(item.getIsVeg())) {
                continue;
            }
            menuGridPanel.add(createMenuCard(item));
        }

        menuGridPanel.revalidate();
        menuGridPanel.repaint();
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
        textArea.setBackground(Color.decode("#F5F6F8"));

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

    /** Menu-item image if set, else the restaurant's image, else a drawn placeholder. */
    private Icon thumbFor(MenuItemResponse item, int width, int height) {
        String path = item.getImagePath();
        if (path == null || path.isBlank()) {
            path = restaurantImageById.get(item.getRestaurant());
        }
        if (path != null && !path.isBlank()) {
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                return new ImageIcon(new ImageIcon(f.getAbsolutePath())
                        .getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }
        }
        return placeholderIcon(item.getName(), width, height);
    }

    private Icon placeholderIcon(String label, int width, int height) {
        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(UITheme.PRIMARY_SOFT);
        g.fillRoundRect(0, 0, width, height, 12, 12);
        g.setColor(UITheme.PRIMARY);
        g.setFont(new Font("SansSerif", Font.BOLD, Math.round(height * 0.4f)));
        String ch = (label != null && !label.isBlank())
                ? label.trim().substring(0, 1).toUpperCase() : "🍽";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(ch, (width - fm.stringWidth(ch)) / 2, (height + fm.getAscent()) / 2 - 4);
        g.dispose();
        return new ImageIcon(img);
    }

    // Custom renderer for the restaurant photo strip
    class RestaurantPhotoRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setHorizontalTextPosition(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.BOTTOM);
            setHorizontalAlignment(SwingConstants.CENTER);
            setPreferredSize(new Dimension(95, 95));
            setFont(new Font("SansSerif", Font.PLAIN, 11));
            setIcon(null);

            if (value instanceof RestaurantResponse) {
                RestaurantResponse restaurant = (RestaurantResponse) value;
                setText(restaurant.getName());

                String imagePath = restaurant.getImage_path();
                if (imagePath != null && !imagePath.isBlank()) {
                    java.io.File file = new java.io.File(imagePath);
                    if (file.exists()) {
                        Image scaled = new ImageIcon(file.getAbsolutePath())
                                .getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                        setIcon(new ImageIcon(scaled));
                    }
                }
            }

            if (isSelected) {
                setBackground(Color.decode("#4F46E5"));
                setForeground(Color.WHITE);
            } else {
                setBackground(Color.decode("#F5F6F8"));
                setForeground(Color.decode("#111827"));
            }
            return this;
        }
    }
}