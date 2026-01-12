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
            new String[]{"ID", "🍽️ Item Name", "💰 Price", "🏢 Restaurant", "📍 Restaurant ID"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Make table non-editable
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0 || column == 4) return Integer.class; // ID columns
            return String.class;
        }
    };
    private final JTable menuTable = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> tableSorter;

    // Form components
    private final JTextField txtName = new JTextField();
    private final JTextField txtPrice = new JTextField();
    private final JComboBox<RestaurantResponse> restaurantCombo = new JComboBox<>();

    // Search components
    private final JTextField txtSearch = new JTextField();
    private final JButton btnSearch = new JButton("🔍");

    // Buttons
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnRefresh;

    public MenuItemPanel() {
        initializeUI();
        setupEventListeners();
        loadRestaurants();
        loadMenuItems();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top panel - Search and filter
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center panel - Split between form and table
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.3);
        splitPane.setContinuousLayout(true);

        // Left panel - Form
        JPanel formPanel = createFormPanel();
        splitPane.setLeftComponent(formPanel);

        // Right panel - Table
        JPanel tablePanel = createTablePanel();
        splitPane.setRightComponent(tablePanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#f8f9fa"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Title
        JLabel titleLabel = new JLabel("📋 Menu Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.decode("#2c3e50"));

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(Color.decode("#f8f9fa"));

        JLabel searchLabel = new JLabel("Search:");
        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        txtSearch.setPreferredSize(new Dimension(200, 30));

        btnSearch.setBackground(Color.decode("#3498db"));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnSearch.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(searchPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("➕ Add/Edit Menu Item");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.decode("#2c3e50"));
        titleLabel.setBounds(20, 20, 350, 30);
        panel.add(titleLabel);

        int yPos = 70;
        addFormField("Item Name:", txtName, 20, yPos, panel);
        yPos += 50;
        addFormField("Price ($):", txtPrice, 20, yPos, panel);
        yPos += 50;

        // Restaurant selection with auto-fill info
        JLabel lblRestaurant = new JLabel("🏢 Restaurant:");
        lblRestaurant.setBounds(20, yPos, 100, 25);
        lblRestaurant.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblRestaurant.setForeground(Color.decode("#2c3e50"));
        panel.add(lblRestaurant);

        restaurantCombo.setBounds(130, yPos, 220, 30);
        restaurantCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        restaurantCombo.setRenderer(new RestaurantComboRenderer());
        panel.add(restaurantCombo);

        // Restaurant info display
        JLabel lblRestaurantInfo = new JLabel("ℹ️ Select a restaurant or click one in the table");
        lblRestaurantInfo.setBounds(20, yPos + 35, 330, 20);
        lblRestaurantInfo.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblRestaurantInfo.setForeground(Color.GRAY);
        panel.add(lblRestaurantInfo);

        yPos += 70;

        // Buttons
        btnAdd = createStyledButton("➕ Add Item", Color.decode("#4CAF50"));
        btnUpdate = createStyledButton("✏️ Update", Color.decode("#2196F3"));
        btnDelete = createStyledButton("🗑️ Delete", Color.decode("#F44336"));
        btnClear = createStyledButton("🧹 Clear", Color.decode("#9E9E9E"));
        btnRefresh = createStyledButton("🔄 Refresh", Color.decode("#FF9800"));

        btnAdd.setBounds(20, yPos, 160, 35);
        btnUpdate.setBounds(190, yPos, 160, 35);
        yPos += 50;
        btnDelete.setBounds(20, yPos, 160, 35);
        btnClear.setBounds(190, yPos, 160, 35);
        yPos += 50;
        btnRefresh.setBounds(20, yPos, 330, 35);

        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(btnClear);
        panel.add(btnRefresh);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 20));

        // Configure table
        menuTable.setRowHeight(35);
        menuTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        menuTable.getTableHeader().setBackground(Color.decode("#34495e"));
        menuTable.getTableHeader().setForeground(Color.WHITE);
        menuTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Hide the Restaurant ID column (index 4) but keep it for reference
        menuTable.removeColumn(menuTable.getColumnModel().getColumn(4));

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
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(240, 248, 255));
                    }
                }

                // Color code price column
                if (column == 2) {
                    setForeground(Color.decode("#27ae60")); // Green for price
                    setFont(getFont().deriveFont(Font.BOLD));
                }

                // Center align ID column
                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(menuTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Add table header with quick actions
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("📋 Menu Items List");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        tableTitle.setForeground(Color.decode("#2c3e50"));

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
            JOptionPane.showMessageDialog(this, "✅ Menu refreshed!",
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

                if (row >= 0 && modelColumn == 3) { // Restaurant column
                    String restaurantName = tableModel.getValueAt(
                            menuTable.convertRowIndexToModel(row), 3).toString();
                    int restaurantId = (int) tableModel.getValueAt(
                            menuTable.convertRowIndexToModel(row), 4);

                    // Find and select the restaurant in combo box
                    for (int i = 0; i < restaurantCombo.getItemCount(); i++) {
                        RestaurantResponse restaurant = restaurantCombo.getItemAt(i);
                        if (restaurant.getId() == restaurantId) {
                            restaurantCombo.setSelectedIndex(i);

                            // Highlight the restaurant selection
                            restaurantCombo.setBackground(new Color(220, 237, 255));
                            Timer timer = new Timer(1000, evt -> {
                                restaurantCombo.setBackground(Color.WHITE);
                            });
                            timer.setRepeats(false);
                            timer.start();

                            // Show confirmation
                            JOptionPane.showMessageDialog(MenuItemPanel.this,
                                    String.format("✅ Restaurant auto-filled:\n%s", restaurantName),
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

            // Search in all columns
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                if (i != 4) { // Skip hidden Restaurant ID column
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
                    "❌ Error loading restaurants: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMenuItems() {
        tableModel.setRowCount(0);
        try {
            List<MenuItemResponse> menuItems = menuService.getAllMenuItems();

            for (MenuItemResponse item : menuItems) {
                String restaurantName = getRestaurantName(item.getRestaurant());
                tableModel.addRow(new Object[]{
                        item.getId(),
                        item.getName(),
                        String.format("$%.2f", parsePrice(item.getPrice())),
                        restaurantName,
                        item.getRestaurant() // Store ID for auto-fill
                });
            }

            // Update count label
            updateItemCount();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Error loading menu items: " + ex.getMessage(),
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

    private String getRestaurantName(int restaurantId) {
        try {
            List<RestaurantResponse> restaurants = restaurantService.findAllRestaurants();
            for (RestaurantResponse restaurant : restaurants) {
                if (restaurant.getId() == restaurantId) {
                    return restaurant.getName();
                }
            }
        } catch (Exception ex) {
            // Ignore - return unknown
        }
        return "Unknown Restaurant";
    }

    private void fillFormFromSelectedRow() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = menuTable.convertRowIndexToModel(selectedRow);

        txtName.setText(tableModel.getValueAt(modelRow, 1).toString());

        String price = tableModel.getValueAt(modelRow, 2).toString();
        txtPrice.setText(price.replace("$", "").trim());

        int restaurantId = (int) tableModel.getValueAt(modelRow, 4);
        for (int i = 0; i < restaurantCombo.getItemCount(); i++) {
            RestaurantResponse restaurant = restaurantCombo.getItemAt(i);
            if (restaurant.getId() == restaurantId) {
                restaurantCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void addMenuItem() {
        try {
            RestaurantResponse selectedRestaurant = (RestaurantResponse) restaurantCombo.getSelectedItem();
            if (selectedRestaurant == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select a restaurant", "⚠️ Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter item name", "⚠️ Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                txtName.requestFocus();
                return;
            }

            if (txtPrice.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter price", "⚠️ Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                txtPrice.requestFocus();
                return;
            }

            MenuItemRequest request = new MenuItemRequest();
            request.setName(txtName.getText().trim());
            request.setPrice(txtPrice.getText().trim());
            request.setRestaurant(selectedRestaurant.getId());
            request.setActive(true);

            menuService.createMenuItem(request);
            JOptionPane.showMessageDialog(this,
                    "✅ Menu item added successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadMenuItems();
            clearForm();
            txtName.requestFocus();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMenuItem() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a menu item to update", "⚠️ No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            RestaurantResponse selectedRestaurant = (RestaurantResponse) restaurantCombo.getSelectedItem();
            if (selectedRestaurant == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select a restaurant", "⚠️ Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int modelRow = menuTable.convertRowIndexToModel(selectedRow);
            MenuItemRequest request = new MenuItemRequest();
            request.setId((int) tableModel.getValueAt(modelRow, 0));
            request.setName(txtName.getText().trim());
            request.setPrice(txtPrice.getText().trim());
            request.setRestaurant(selectedRestaurant.getId());
            request.setActive(true);

            menuService.updateMenuItem(request);
            JOptionPane.showMessageDialog(this,
                    "✅ Menu item updated successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadMenuItems();
            clearForm();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteMenuItem() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a menu item to delete", "⚠️ No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this menu item?\nThis action cannot be undone!",
                "⚠️ Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int modelRow = menuTable.convertRowIndexToModel(selectedRow);
                MenuItemRequest request = new MenuItemRequest();
                request.setId((int) tableModel.getValueAt(modelRow, 0));

                menuService.deleteMenuItem(request);
                JOptionPane.showMessageDialog(this,
                        "✅ Menu item deleted successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadMenuItems();
                clearForm();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "❌ Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        menuTable.clearSelection();
        tableSorter.setRowFilter(null);
    }

    private double parsePrice(String price) {
        try {
            return Double.parseDouble(price);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // UI Helper methods
    private void addFormField(String label, JTextField field, int x, int y, JPanel panel) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(x, y, 100, 25);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(Color.decode("#2c3e50"));
        panel.add(lbl);

        field.setBounds(x + 110, y, 220, 30);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panel.add(field);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
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

    // Custom combo box renderer for restaurants
    class RestaurantComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof RestaurantResponse) {
                RestaurantResponse restaurant = (RestaurantResponse) value;
                setText(String.format("🏢 %s (ID: %d)",
                        restaurant.getName(), restaurant.getId()));
            }

            return this;
        }
    }
}