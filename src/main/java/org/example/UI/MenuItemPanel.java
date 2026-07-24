package org.example.UI;

import org.example.DTO.Request.MenuItemRequest;
import org.example.DTO.Response.MenuItemResponse;
import org.example.DTO.Response.RestaurantResponse;
import org.example.Service.ServiceImplement.ServiceMenuItemImp;
import org.example.Service.ServiceImplement.ServiceRestaurantImp;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MenuItemPanel extends JPanel {
    private final ServiceMenuItemImp menuService = new ServiceMenuItemImp();
    private final ServiceRestaurantImp restaurantService = new ServiceRestaurantImp();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"", "ID", "Item Name", "Price", "Restaurant", "Veg", "Restaurant ID"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Make table non-editable
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) return Icon.class;
            if (column == 1 || column == 6) return Integer.class; // ID columns
            return String.class;
        }
    };
    private final JTable menuTable = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> tableSorter;

    // Form components
    private final JTextField txtName = new JTextField();
    private final JTextField txtPrice = new JTextField();
    private final JComboBox<RestaurantResponse> restaurantCombo = new JComboBox<>();
    private final JComboBox<String> cbVeg = new JComboBox<>(new String[]{"Veg", "Non-Veg"});

    // Search components
    private final JTextField txtSearch = new JTextField();
    private final JButton btnSearch = UITheme.createButton("Search", UITheme.PRIMARY);

    // Buttons
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnRefresh;
    private JButton btnChooseImage;

    // Image staged for the item currently in the form + id -> stored image path
    private String selectedImagePath;
    private final JLabel lblImagePreview = new JLabel("No Image", SwingConstants.CENTER);
    private final java.util.Map<Integer, String> imagePathById = new java.util.HashMap<>();

    public MenuItemPanel() {
        initializeUI();
        setupEventListeners();
        loadRestaurants();
        loadMenuItems();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.decode("#F5F6F8"));

        // Top panel - Search and filter
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center panel - Split between form and table
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.3);
        splitPane.setContinuousLayout(true);

        // Left panel - Form (scrollable so it never gets clipped on smaller windows)
        JScrollPane formScroll = new JScrollPane(createFormPanel());
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(Color.decode("#F5F6F8"));
        splitPane.setLeftComponent(formScroll);

        // Right panel - Table
        JPanel tablePanel = createTablePanel();
        splitPane.setRightComponent(tablePanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#F5F6F8"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Title
        JLabel titleLabel = new JLabel("🍔 Menu Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.decode("#111827"));

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(Color.decode("#F5F6F8"));

        JLabel searchLabel = new JLabel("Search:");
        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtSearch.setBorder(new UITheme.RoundedLineBorder(UITheme.NEUTRAL, 14, 5, 10));
        txtSearch.setPreferredSize(new Dimension(200, 30));

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(searchPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Color.decode("#F5F6F8"));
        outer.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());
        outer.add(card, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel("✏️  Add / Edit Menu Item");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_DARK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(22, 24, 8, 24));
        card.add(titleLabel, BorderLayout.NORTH);

        // ---- Image row: preview + choose button, side by side ----
        JPanel imageRow = new JPanel(new BorderLayout(16, 0));
        imageRow.setOpaque(false);
        imageRow.setBorder(BorderFactory.createEmptyBorder(4, 24, 20, 24));

        lblImagePreview.setPreferredSize(new Dimension(110, 90));
        lblImagePreview.setOpaque(true);
        lblImagePreview.setBackground(Color.WHITE);
        lblImagePreview.setBorder(new UITheme.RoundedLineBorder(UITheme.BORDER, 12, 0, 0));
        lblImagePreview.setFont(UITheme.FONT_MUTED);
        lblImagePreview.setForeground(UITheme.TEXT_MUTED);
        imageRow.add(lblImagePreview, BorderLayout.WEST);

        JPanel imageSide = new JPanel();
        imageSide.setOpaque(false);
        imageSide.setLayout(new BoxLayout(imageSide, BoxLayout.Y_AXIS));
        JLabel imgHint = new JLabel("<html>A photo makes this item stand out<br>in the customer's order screen.</html>");
        imgHint.setFont(UITheme.FONT_MUTED);
        imgHint.setForeground(UITheme.TEXT_MUTED);
        imgHint.setAlignmentX(LEFT_ALIGNMENT);
        btnChooseImage = createStyledButton("📷 Choose Image", UITheme.PRIMARY);
        btnChooseImage.setAlignmentX(LEFT_ALIGNMENT);
        btnChooseImage.addActionListener(e -> chooseImage());
        imageSide.add(imgHint);
        imageSide.add(Box.createVerticalStrut(10));
        imageSide.add(btnChooseImage);
        imageRow.add(imageSide, BorderLayout.CENTER);

        card.add(imageRow, BorderLayout.CENTER);

        // ---- Fields ----
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        fields.setBorder(BorderFactory.createEmptyBorder(0, 24, 8, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        gbc.gridy = 0;
        fields.add(fieldGroup("Item Name", txtName), gbc);
        gbc.gridy = 1;
        fields.add(fieldGroup("Price ($)", txtPrice), gbc);

        JLabel lblRestaurant = new JLabel("Restaurant");
        lblRestaurant.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        lblRestaurant.setForeground(UITheme.TEXT_DARK);
        lblRestaurant.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));

        restaurantCombo.setFont(UITheme.FONT_BODY);
        restaurantCombo.setRenderer(new RestaurantComboRenderer());
        restaurantCombo.setPreferredSize(new Dimension(10, 40));

        JPanel restaurantGroup = new JPanel();
        restaurantGroup.setOpaque(false);
        restaurantGroup.setLayout(new BoxLayout(restaurantGroup, BoxLayout.Y_AXIS));
        lblRestaurant.setAlignmentX(LEFT_ALIGNMENT);
        restaurantCombo.setAlignmentX(LEFT_ALIGNMENT);
        restaurantGroup.add(lblRestaurant);
        restaurantGroup.add(restaurantCombo);

        gbc.gridy = 2;
        fields.add(restaurantGroup, gbc);

        JLabel lblVeg = new JLabel("Type");
        lblVeg.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        lblVeg.setForeground(UITheme.TEXT_DARK);
        lblVeg.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));

        cbVeg.setFont(UITheme.FONT_BODY);
        cbVeg.setPreferredSize(new Dimension(10, 40));

        JPanel vegGroup = new JPanel();
        vegGroup.setOpaque(false);
        vegGroup.setLayout(new BoxLayout(vegGroup, BoxLayout.Y_AXIS));
        lblVeg.setAlignmentX(LEFT_ALIGNMENT);
        cbVeg.setAlignmentX(LEFT_ALIGNMENT);
        vegGroup.add(lblVeg);
        vegGroup.add(cbVeg);

        gbc.gridy = 3;
        fields.add(vegGroup, gbc);

        JLabel lblRestaurantInfo = new JLabel("Tip: click a restaurant cell in the table to auto-fill this");
        lblRestaurantInfo.setFont(UITheme.FONT_MUTED);
        lblRestaurantInfo.setForeground(UITheme.TEXT_MUTED);
        gbc.gridy = 4;
        gbc.insets = new Insets(2, 0, 8, 0);
        fields.add(lblRestaurantInfo, gbc);

        card.add(fields, BorderLayout.SOUTH);

        // ---- Actions ----
        JPanel actions = new JPanel(new GridLayout(3, 1, 0, 10));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(4, 24, 24, 24));

        JPanel row1 = new JPanel(new GridLayout(1, 2, 10, 0));
        row1.setOpaque(false);
        btnAdd = createStyledButton("Add Item", UITheme.SUCCESS);
        btnUpdate = createStyledButton("Update", UITheme.PRIMARY);
        row1.add(btnAdd);
        row1.add(btnUpdate);

        JPanel row2 = new JPanel(new GridLayout(1, 2, 10, 0));
        row2.setOpaque(false);
        btnDelete = createStyledButton("Delete", UITheme.SECONDARY);
        btnClear = createStyledButton("Clear", UITheme.NEUTRAL);
        row2.add(btnDelete);
        row2.add(btnClear);

        btnRefresh = createStyledButton("Refresh List", UITheme.PRIMARY);

        actions.add(row1);
        actions.add(row2);
        actions.add(btnRefresh);

        JPanel cardWithActions = new JPanel(new BorderLayout());
        cardWithActions.setOpaque(false);
        cardWithActions.add(card, BorderLayout.CENTER);
        cardWithActions.add(actions, BorderLayout.SOUTH);

        outer.removeAll();
        outer.add(cardWithActions, BorderLayout.NORTH);

        return outer;
    }

    private JPanel fieldGroup(String label, JTextField field) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        lbl.setForeground(UITheme.TEXT_DARK);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);

        field.setFont(UITheme.FONT_BODY);
        field.setBorder(new UITheme.RoundedLineBorder(UITheme.BORDER, 12, 10, 12));
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setPreferredSize(new Dimension(10, 40));

        group.add(lbl);
        group.add(field);
        return group;
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Menu Item Image");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File selected = chooser.getSelectedFile();
        try {
            java.io.File dir = new java.io.File("images/menu");
            dir.mkdirs();
            String ext = "";
            int dot = selected.getName().lastIndexOf('.');
            if (dot >= 0) ext = selected.getName().substring(dot);
            java.io.File dest = new java.io.File(dir, "menu_" + System.currentTimeMillis() + ext);
            java.nio.file.Files.copy(selected.toPath(), dest.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            selectedImagePath = "images/menu/" + dest.getName();
            showImagePreview(selectedImagePath);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showImagePreview(String path) {
        lblImagePreview.setIcon(null);
        if (path == null || path.isBlank()) {
            lblImagePreview.setText("No Image");
            return;
        }
        java.io.File file = new java.io.File(path);
        if (!file.exists()) {
            lblImagePreview.setText("Missing");
            return;
        }
        Image scaled = new ImageIcon(file.getAbsolutePath())
                .getImage().getScaledInstance(100, 80, Image.SCALE_SMOOTH);
        lblImagePreview.setText("");
        lblImagePreview.setIcon(new ImageIcon(scaled));
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#F5F6F8"));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 20));

        // Configure table
        menuTable.setRowHeight(52);
        menuTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        menuTable.getTableHeader().setBackground(Color.decode("#4F46E5"));
        menuTable.getTableHeader().setForeground(Color.WHITE);
        menuTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Thumbnail column: small, fixed width, no header text
        menuTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        menuTable.getColumnModel().getColumn(0).setMaxWidth(50);
        menuTable.getColumnModel().getColumn(0).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setIcon(value instanceof Icon ? (Icon) value : null);
                lbl.setBackground(isSelected ? new Color(224, 231, 255)
                        : (row % 2 == 0 ? Color.decode("#F5F6F8") : new Color(249, 250, 251)));
                lbl.setOpaque(true);
                return lbl;
            }
        });

        // Hide the Restaurant ID column (index 6) but keep it for reference
        menuTable.removeColumn(menuTable.getColumnModel().getColumn(6));

        // Set up table sorter
        tableSorter = new TableRowSorter<>(tableModel);
        menuTable.setRowSorter(tableSorter);

        // Custom cell renderer for better display
        menuTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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
                }

                // Color code price column
                if (column == 3) {
                    setForeground(Color.decode("#16A34A")); // Green for price
                    setFont(getFont().deriveFont(Font.BOLD));
                }

                // Center align ID column
                if (column == 1) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(menuTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Add table header with quick actions
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.decode("#F5F6F8"));

        JLabel tableTitle = new JLabel("📋 Menu Items List");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        tableTitle.setForeground(Color.decode("#111827"));

        JLabel countLabel = new JLabel("Total: 0 items");
        countLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        countLabel.setForeground(Color.GRAY);

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(countLabel, BorderLayout.EAST);
        tableHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        panel.add(tableHeader, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void setupEventListeners() {
        // Button actions
        btnAdd.addActionListener(e -> addMenuItem());
        btnUpdate.addActionListener(e -> updateMenuItem());
        btnDelete.addActionListener(e -> deleteMenuItem());
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> {
            loadMenuItems();
            loadRestaurants();
            JOptionPane.showMessageDialog(this, "Menu refreshed!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        // Search functionality
        btnSearch.addActionListener(e -> filterTable());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        // Table selection - fill form when row is selected
        menuTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelectedRow();
            }
        });

        // Double-click on restaurant cell to auto-fill combo box
        menuTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = menuTable.rowAtPoint(e.getPoint());
                int column = menuTable.columnAtPoint(e.getPoint());
                int modelColumn = menuTable.convertColumnIndexToModel(column);

                if (row >= 0 && modelColumn == 4) { // Restaurant column
                    String restaurantName = tableModel.getValueAt(
                            menuTable.convertRowIndexToModel(row), 4).toString();
                    int restaurantId = (int) tableModel.getValueAt(
                            menuTable.convertRowIndexToModel(row), 6);

                    // Find and select the restaurant in combo box
                    for (int i = 0; i < restaurantCombo.getItemCount(); i++) {
                        RestaurantResponse restaurant = restaurantCombo.getItemAt(i);
                        if (restaurant.getId() == restaurantId) {
                            restaurantCombo.setSelectedIndex(i);

                            // Highlight the restaurant selection
                            restaurantCombo.setBackground(new Color(224, 231, 255));
                            Timer timer = new Timer(1000, evt -> {
                                restaurantCombo.setBackground(Color.decode("#F5F6F8"));
                            });
                            timer.setRepeats(false);
                            timer.start();

                            // Show confirmation
                            JOptionPane.showMessageDialog(MenuItemPanel.this,
                                    String.format("Restaurant auto-filled:\n%s", restaurantName),
                                    "Auto-Fill", JOptionPane.INFORMATION_MESSAGE);
                            break;
                        }
                    }
                }
            }
        });

        // Keyboard shortcuts
        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        // Enter key in form fields
        txtName.addActionListener(e -> txtPrice.requestFocus());
        txtPrice.addActionListener(e -> restaurantCombo.requestFocus());

        // Ctrl+F to focus search
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("control F"), "focusSearch");
        getActionMap().put("focusSearch", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtSearch.requestFocus();
                txtSearch.selectAll();
            }
        });

        // Delete key on table
        menuTable.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("DELETE"), "deleteItem");
        menuTable.getActionMap().put("deleteItem", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                deleteMenuItem();
            }
        });

        // F5 to refresh
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F5"), "refresh");
        getActionMap().put("refresh", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                loadMenuItems();
                loadRestaurants();
            }
        });
    }

    private void filterTable() {
        String searchText = txtSearch.getText().trim().toLowerCase();

        if (searchText.isEmpty()) {
            tableSorter.setRowFilter(null);
        } else {
            List<RowFilter<Object, Object>> filters = new ArrayList<>();

            // Search in all text columns (skip the thumbnail icon column and the hidden Restaurant ID column)
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                if (i != 0 && i != 6) {
                    filters.add(RowFilter.regexFilter("(?i)" + searchText, i));
                }
            }

            RowFilter<Object, Object> orFilter = RowFilter.orFilter(filters);
            tableSorter.setRowFilter(orFilter);
        }
    }

    private void loadRestaurants() {
        restaurantCombo.removeAllItems();
        try {
            List<RestaurantResponse> restaurants = restaurantService.findAllRestaurants();
            for (RestaurantResponse restaurant : restaurants) {
                restaurantCombo.addItem(restaurant);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading restaurants: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMenuItems() {
        tableModel.setRowCount(0);
        imagePathById.clear();
        try {
            List<MenuItemResponse> menuItems = menuService.getAllMenuItems();

            // Fetch restaurants ONCE and index by id, instead of hitting the
            // remote database once per menu item (which made the dashboard take
            // minutes to open with a few hundred items).
            java.util.Map<Integer, String> restaurantNames = new java.util.HashMap<>();
            for (RestaurantResponse restaurant : restaurantService.findAllRestaurants()) {
                restaurantNames.put(restaurant.getId(), restaurant.getName());
            }

            for (MenuItemResponse item : menuItems) {
                imagePathById.put(item.getId(), item.getImagePath());
                String restaurantName = restaurantNames.getOrDefault(item.getRestaurant(), "Unknown Restaurant");
                tableModel.addRow(new Object[]{
                        rowThumbnail(item.getImagePath(), item.getName()),
                        item.getId(),
                        item.getName(),
                        String.format("$%.2f", parsePrice(item.getPrice())),
                        restaurantName,
                        Boolean.TRUE.equals(item.getIsVeg()) ? "Veg" : "Non-Veg",
                        item.getRestaurant() // Store ID for auto-fill
                });
            }

            // Update count label
            updateItemCount();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading menu items: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateItemCount() {
        int count = tableModel.getRowCount();
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                for (Component c : ((JPanel) comp).getComponents()) {
                    if (c instanceof JLabel && ((JLabel) c).getText().startsWith("Total:")) {
                        ((JLabel) c).setText("Total: " + count + " item" + (count != 1 ? "s" : ""));
                        break;
                    }
                }
            }
        }
    }

    private void fillFormFromSelectedRow() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = menuTable.convertRowIndexToModel(selectedRow);

        txtName.setText(tableModel.getValueAt(modelRow, 2).toString());

        String price = tableModel.getValueAt(modelRow, 3).toString();
        txtPrice.setText(price.replace("$", "").trim());

        int restaurantId = (int) tableModel.getValueAt(modelRow, 6);
        for (int i = 0; i < restaurantCombo.getItemCount(); i++) {
            RestaurantResponse restaurant = restaurantCombo.getItemAt(i);
            if (restaurant.getId() == restaurantId) {
                restaurantCombo.setSelectedIndex(i);
                break;
            }
        }

        cbVeg.setSelectedItem(tableModel.getValueAt(modelRow, 5).toString());

        selectedImagePath = imagePathById.get((int) tableModel.getValueAt(modelRow, 1));
        showImagePreview(selectedImagePath);
    }

    private void addMenuItem() {
        try {
            RestaurantResponse selectedRestaurant = (RestaurantResponse) restaurantCombo.getSelectedItem();
            if (selectedRestaurant == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select a restaurant", "Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter item name", "Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                txtName.requestFocus();
                return;
            }

            if (txtPrice.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter price", "Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                txtPrice.requestFocus();
                return;
            }

            MenuItemRequest request = new MenuItemRequest();
            request.setName(txtName.getText().trim());
            request.setPrice(txtPrice.getText().trim());
            request.setRestaurant(selectedRestaurant.getId());
            request.setActive(true);
            request.setImagePath(selectedImagePath);
            request.setIsVeg("Veg".equals(cbVeg.getSelectedItem()));

            menuService.createMenuItem(request);
            JOptionPane.showMessageDialog(this,
                    "Menu item added successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadMenuItems();
            clearForm();
            txtName.requestFocus();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMenuItem() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a menu item to update", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            RestaurantResponse selectedRestaurant = (RestaurantResponse) restaurantCombo.getSelectedItem();
            if (selectedRestaurant == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select a restaurant", "Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int modelRow = menuTable.convertRowIndexToModel(selectedRow);
            MenuItemRequest request = new MenuItemRequest();
            request.setId((int) tableModel.getValueAt(modelRow, 1));
            request.setName(txtName.getText().trim());
            request.setPrice(txtPrice.getText().trim());
            request.setRestaurant(selectedRestaurant.getId());
            request.setActive(true);
            request.setImagePath(selectedImagePath);
            request.setIsVeg("Veg".equals(cbVeg.getSelectedItem()));

            menuService.updateMenuItem(request);
            JOptionPane.showMessageDialog(this,
                    "Menu item updated successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadMenuItems();
            clearForm();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteMenuItem() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a menu item to delete", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this menu item?\nThis action cannot be undone!",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int modelRow = menuTable.convertRowIndexToModel(selectedRow);
                MenuItemRequest request = new MenuItemRequest();
                request.setId((int) tableModel.getValueAt(modelRow, 1));

                menuService.deleteMenuItem(request);
                JOptionPane.showMessageDialog(this,
                        "Menu item deleted successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadMenuItems();
                clearForm();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtPrice.setText("");
        txtSearch.setText("");
        if (restaurantCombo.getItemCount() > 0) {
            restaurantCombo.setSelectedIndex(0);
        }
        cbVeg.setSelectedIndex(0);
        selectedImagePath = null;
        showImagePreview(null);
        menuTable.clearSelection();
        tableSorter.setRowFilter(null);
    }

    /** Small 32x32 table thumbnail: the item's photo if set, else a lettered placeholder. */
    private Icon rowThumbnail(String imagePath, String name) {
        if (imagePath != null && !imagePath.isBlank()) {
            java.io.File f = new java.io.File(imagePath);
            if (f.exists()) {
                return new ImageIcon(new ImageIcon(f.getAbsolutePath())
                        .getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
            }
        }
        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(32, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(UITheme.PRIMARY_SOFT);
        g.fillRoundRect(0, 0, 32, 32, 8, 8);
        g.setColor(UITheme.PRIMARY);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        String ch = (name != null && !name.isBlank()) ? name.trim().substring(0, 1).toUpperCase() : "?";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(ch, (32 - fm.stringWidth(ch)) / 2, (32 + fm.getAscent()) / 2 - 3);
        g.dispose();
        return new ImageIcon(img);
    }

    private double parsePrice(String price) {
        try {
            return Double.parseDouble(price);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // UI Helper methods
    private JButton createStyledButton(String text, Color color) {
        return UITheme.createButton(text, color);
    }

    // Custom combo box renderer for restaurants
    class RestaurantComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof RestaurantResponse) {
                RestaurantResponse restaurant = (RestaurantResponse) value;
                setText(String.format("%s (ID: %d)",
                        restaurant.getName(), restaurant.getId()));
            }

            return this;
        }
    }
}