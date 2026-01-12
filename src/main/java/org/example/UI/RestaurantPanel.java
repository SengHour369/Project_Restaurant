package org.example.UI;

import org.example.DTO.Request.RestaurantRequest;
import org.example.DTO.Response.RestaurantResponse;
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

public class RestaurantPanel extends JPanel {
    private final ServiceRestaurantImp restaurantService = new ServiceRestaurantImp();

    // Form fields
    private final JTextField txtId = createTextField(false);
    private final JTextField txtName = createTextField(true);
    private final JComboBox<String> txtCategory = new JComboBox<>(new String[]{
            "🍕 Italian", "🍣 Japanese", "🍔 American", "🌮 Mexican",
            "🥘 Chinese", "🍝 Korean", "🍜 Vietnamese", "🥗 Healthy",
            "☕ Cafe", "🍰 Dessert", "🍺 Pub", "🥩 Steakhouse"
    });
    private final JSlider txtRating = new JSlider(0, 5, 0);
    private final JLabel lblRatingValue = new JLabel("⭐ 0/5");
    private final JTextField txtPhone = createTextField(true);
    private final JTextField txtLocation = createTextField(true);

    // Search field
    private final JTextField txtSearch = new JTextField();

    // Table
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Category", "⭐ Rating", "📞 Phone", "📍 Location", "🆔 Internal ID"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0 || column == 6) return Integer.class;
            return String.class;
        }
    };
    private final JTable restaurantTable = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> tableSorter;

    // Statistics labels
    private final JLabel lblTotalRestaurants = new JLabel("🏢 Total: 0");
    private final JLabel lblAvgRating = new JLabel("⭐ Avg Rating: 0.0");
    private final JLabel lblSelectedInfo = new JLabel("👆 Select a restaurant");

    public RestaurantPanel() {
        initializeUI();
        setupEventListeners();
        loadAllRestaurants();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top panel with title and search
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center panel with split view
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.35);
        splitPane.setContinuousLayout(true);

        // Left panel - Form
        JPanel formPanel = createFormPanel();
        splitPane.setLeftComponent(formPanel);

        // Right panel - Table
        JPanel tablePanel = createTablePanel();
        splitPane.setRightComponent(tablePanel);

        add(splitPane, BorderLayout.CENTER);

        // Bottom panel - Statistics
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#f8f9fa"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#3498db")),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Title with icon
        JLabel titleLabel = new JLabel("🏢 Restaurant Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.decode("#2c3e50"));

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.decode("#f8f9fa"));

        JLabel searchLabel = new JLabel("🔍 Search:");
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        searchLabel.setForeground(Color.decode("#2c3e50"));

        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#3498db"), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        txtSearch.setPreferredSize(new Dimension(250, 35));
        txtSearch.setToolTipText("Search by name, category, location...");

        JButton btnClearSearch = createIconButton("🗑️", Color.decode("#e74c3c"), 14);
        btnClearSearch.setToolTipText("Clear search");
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            txtSearch.requestFocus();
        });

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnClearSearch, BorderLayout.EAST);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(searchPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Form title
        JLabel formTitle = new JLabel("➕ Add/Edit Restaurant");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        formTitle.setForeground(Color.decode("#2c3e50"));
        formTitle.setBounds(20, 20, 350, 30);
        panel.add(formTitle);

        int yPos = 70;
        int fieldWidth = 320;

        // Restaurant ID (read-only)
        addFormField("🆔 Restaurant ID:", txtId, 20, yPos, fieldWidth, panel);
        yPos += 50;

        // Name field
        addFormField("🏷️ Restaurant Name:", txtName, 20, yPos, fieldWidth, panel);
        yPos += 50;

        // Category combo with icon
        JLabel lblCategory = new JLabel("📁 Category:");
        lblCategory.setBounds(20, yPos, 120, 25);
        lblCategory.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblCategory.setForeground(Color.decode("#2c3e50"));
        panel.add(lblCategory);

        txtCategory.setBounds(150, yPos, fieldWidth - 130, 30);
        txtCategory.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtCategory.setBackground(Color.WHITE);
        panel.add(txtCategory);
        yPos += 50;

        // Rating slider
        JLabel lblRating = new JLabel("⭐ Rating:");
        lblRating.setBounds(20, yPos, 120, 25);
        lblRating.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblRating.setForeground(Color.decode("#2c3e50"));
        panel.add(lblRating);

        txtRating.setBounds(150, yPos, 180, 30);
        txtRating.setMajorTickSpacing(1);
        txtRating.setPaintTicks(true);
        txtRating.setPaintLabels(true);
        txtRating.setSnapToTicks(true);
        panel.add(txtRating);

        lblRatingValue.setBounds(340, yPos, 60, 25);
        lblRatingValue.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblRatingValue.setForeground(Color.decode("#f39c12"));
        panel.add(lblRatingValue);
        yPos += 50;

        // Phone field
        addFormField("📞 Phone Number:", txtPhone, 20, yPos, fieldWidth, panel);
        yPos += 50;

        // Location field
        addFormField("📍 Location:", txtLocation, 20, yPos, fieldWidth, panel);
        yPos += 60;

        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setBounds(20, yPos, fieldWidth, 90);
        buttonPanel.setBackground(Color.WHITE);

        JButton btnAdd = createStyledButton("➕ Add", Color.decode("#2ecc71"));
        JButton btnUpdate = createStyledButton("✏️ Update", Color.decode("#3498db"));
        JButton btnDelete = createStyledButton("🗑️ Delete", Color.decode("#e74c3c"));
        JButton btnClear = createStyledButton("🧹 Clear", Color.decode("#95a5a6"));

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        panel.add(buttonPanel);

        yPos += 100;

        // Quick actions
        JLabel quickActions = new JLabel("⚡ Quick Actions:");
        quickActions.setBounds(20, yPos, 150, 25);
        quickActions.setFont(new Font("SansSerif", Font.BOLD, 14));
        quickActions.setForeground(Color.decode("#2c3e50"));
        panel.add(quickActions);

        JPanel quickButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        quickButtons.setBounds(150, yPos, fieldWidth - 130, 30);
        quickButtons.setBackground(Color.WHITE);

        JButton btnRefresh = createIconButton("🔄", Color.decode("#9b59b6"), 12);
        btnRefresh.setToolTipText("Refresh List");
        JButton btnViewDetails = createIconButton("👁️", Color.decode("#1abc9c"), 12);
        btnViewDetails.setToolTipText("View Details");
        JButton btnCopyPhone = createIconButton("📋", Color.decode("#f39c12"), 12);
        btnCopyPhone.setToolTipText("Copy Phone");

        btnRefresh.addActionListener(e -> refreshRestaurants());
        btnViewDetails.addActionListener(e -> showSelectedDetails());
        btnCopyPhone.addActionListener(e -> copyToClipboard(txtPhone.getText()));

        quickButtons.add(btnRefresh);
        quickButtons.add(btnViewDetails);
        quickButtons.add(btnCopyPhone);
        panel.add(quickButtons);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 20));

        // Table header
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);
        tableHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel tableTitle = new JLabel("📋 Restaurants List");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        tableTitle.setForeground(Color.decode("#2c3e50"));

        lblSelectedInfo.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblSelectedInfo.setForeground(Color.decode("#7f8c8d"));

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(lblSelectedInfo, BorderLayout.EAST);

        // Configure table
        restaurantTable.setRowHeight(40);
        restaurantTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        restaurantTable.getTableHeader().setBackground(Color.decode("#34495e"));
        restaurantTable.getTableHeader().setForeground(Color.WHITE);
        restaurantTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Hide internal ID column
        restaurantTable.removeColumn(restaurantTable.getColumnModel().getColumn(6));

        // Set up table sorter
        tableSorter = new TableRowSorter<>(tableModel);
        restaurantTable.setRowSorter(tableSorter);

        // Custom cell renderer
        restaurantTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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

                // Style rating column
                if (column == 3 && value != null) {
                    String ratingText = value.toString();
                    if (ratingText.contains("★")) {
                        setForeground(Color.decode("#f39c12")); // Orange for stars
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                }

                // Center align ID column
                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(restaurantTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(tableHeader, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panel.setBackground(Color.decode("#f1f8ff"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, Color.decode("#3498db")),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        lblTotalRestaurants.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTotalRestaurants.setForeground(Color.decode("#2c3e50"));

        lblAvgRating.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblAvgRating.setForeground(Color.decode("#f39c12"));

        JLabel lblActions = new JLabel("💡 Double-click row to edit | Click category to filter");
        lblActions.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblActions.setForeground(Color.decode("#7f8c8d"));

        panel.add(lblTotalRestaurants);
        panel.add(lblAvgRating);
        panel.add(Box.createHorizontalStrut(50));
        panel.add(lblActions);

        return panel;
    }

    private void setupEventListeners() {
        // Search functionality
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        // Rating slider listener
        txtRating.addChangeListener(e -> {
            int rating = txtRating.getValue();
            lblRatingValue.setText("⭐ " + rating + "/5");
            lblRatingValue.setForeground(getRatingColor(rating));
        });

        // Table selection listener
        restaurantTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelectedRow();
            }
        });

        // Double-click to edit
        restaurantTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = restaurantTable.rowAtPoint(e.getPoint());
                    int column = restaurantTable.columnAtPoint(e.getPoint());

                    if (row >= 0) {
                        fillFormFromSelectedRow();

                        // If clicked on category cell, set combo box
                        if (column == 2) {
                            String category = tableModel.getValueAt(
                                    restaurantTable.convertRowIndexToModel(row), 2).toString();
                            txtCategory.setSelectedItem(category);

                            // Show animation
                            txtCategory.setBackground(new Color(220, 237, 255));
                            Timer timer = new Timer(1000, evt -> {
                                txtCategory.setBackground(Color.WHITE);
                            });
                            timer.setRepeats(false);
                            timer.start();
                        }
                    }
                }
            }
        });

        // Setup button actions (will be set by external controller)
        // For now, we'll add them here for completeness
        setupButtonActions();

        // Keyboard shortcuts
        setupKeyboardShortcuts();
    }

    private void setupButtonActions() {
        // Find buttons and add action listeners
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                setupPanelButtons((JPanel) comp);
            }
        }
    }

    private void setupPanelButtons(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                String text = btn.getText();

                if (text.contains("Add")) {
                    btn.addActionListener(e -> addRestaurant());
                } else if (text.contains("Update")) {
                    btn.addActionListener(e -> updateRestaurant());
                } else if (text.contains("Delete")) {
                    btn.addActionListener(e -> deleteRestaurant());
                } else if (text.contains("Clear")) {
                    btn.addActionListener(e -> clearForm());
                }
            } else if (comp instanceof JPanel) {
                setupPanelButtons((JPanel) comp);
            }
        }
    }

    private void setupKeyboardShortcuts() {
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

        // Delete key to delete selected restaurant
        restaurantTable.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("DELETE"), "deleteRestaurant");
        restaurantTable.getActionMap().put("deleteRestaurant", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                deleteRestaurant();
            }
        });

        // F5 to refresh
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F5"), "refresh");
        getActionMap().put("refresh", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                refreshRestaurants();
            }
        });

        // Ctrl+N for new restaurant
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("control N"), "newRestaurant");
        getActionMap().put("newRestaurant", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearForm();
                txtName.requestFocus();
            }
        });
    }

    private void filterTable() {
        String searchText = txtSearch.getText().trim().toLowerCase();

        if (searchText.isEmpty()) {
            tableSorter.setRowFilter(null);
        } else {
            List<javax.swing.RowFilter<Object, Object>> filters = new ArrayList<>();

            // Search in all visible columns except ID
            for (int i = 0; i < tableModel.getColumnCount() - 1; i++) {
                if (i != 0) { // Skip ID column from search
                    filters.add(javax.swing.RowFilter.regexFilter("(?i)" + searchText, i));
                }
            }

            javax.swing.RowFilter<Object, Object> orFilter = javax.swing.RowFilter.orFilter(filters);
            tableSorter.setRowFilter(orFilter);
        }
    }

    private void showSelectedDetails() {
        int selectedRow = restaurantTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a restaurant first!",
                    "⚠️ No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = restaurantTable.convertRowIndexToModel(selectedRow);
        String details = String.format(
                "🏢 Restaurant Details\n\n" +
                        "🆔 ID: %s\n" +
                        "🏷️ Name: %s\n" +
                        "📁 Category: %s\n" +
                        "⭐ Rating: %s\n" +
                        "📞 Phone: %s\n" +
                        "📍 Location: %s",
                tableModel.getValueAt(modelRow, 0),
                tableModel.getValueAt(modelRow, 1),
                tableModel.getValueAt(modelRow, 2),
                tableModel.getValueAt(modelRow, 3),
                tableModel.getValueAt(modelRow, 4),
                tableModel.getValueAt(modelRow, 5)
        );

        JOptionPane.showMessageDialog(this, details,
                "📋 Restaurant Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void copyToClipboard(String text) {
        if (text == null || text.trim().isEmpty()) return;

        java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(text);
        java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);

        JOptionPane.showMessageDialog(this,
                "✅ Copied to clipboard: " + text,
                "📋 Copied", JOptionPane.INFORMATION_MESSAGE);
    }

    public void addRestaurant() {
        try {
            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter restaurant name!", "⚠️ Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                txtName.requestFocus();
                return;
            }

            RestaurantRequest request = collectFormData();
            restaurantService.CreateRestaurant(request);
            JOptionPane.showMessageDialog(this,
                    "✅ Restaurant added successfully!\n\n" +
                            "🏷️ Name: " + request.getName() + "\n" +
                            "📁 Category: " + request.getCategory() + "\n" +
                            "⭐ Rating: " + request.getRating() + "/5",
                    "🎉 Success", JOptionPane.INFORMATION_MESSAGE);
            loadAllRestaurants();
            clearForm();
            txtName.requestFocus();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateRestaurant() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a restaurant to update", "⚠️ No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Update this restaurant's information?", "✏️ Confirm Update",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(txtId.getText());
                RestaurantRequest request = collectFormData();
                restaurantService.updateRestaurant(request, id);
                JOptionPane.showMessageDialog(this,
                        "✅ Restaurant updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadAllRestaurants();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "❌ Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void deleteRestaurant() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a restaurant to delete", "⚠️ No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this restaurant?\n" +
                        "This action cannot be undone!\n\n" +
                        "⚠️ All menu items from this restaurant will also be deleted.",
                "🗑️ Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(txtId.getText());
                restaurantService.deleteRestaurant(id);
                JOptionPane.showMessageDialog(this,
                        "✅ Restaurant deleted successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadAllRestaurants();
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "❌ Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshRestaurants() {
        loadAllRestaurants();
        clearForm();
        txtSearch.setText("");
        JOptionPane.showMessageDialog(this,
                "🔄 Restaurant list refreshed!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private RestaurantRequest collectFormData() {
        String category = (String) txtCategory.getSelectedItem();
        // Remove emoji if present
        if (category != null && category.contains(" ")) {
            category = category.substring(category.indexOf(" ") + 1);
        }

        return new RestaurantRequest(
                txtName.getText().trim(),
                category != null ? category : "",
                txtRating.getValue(),
                txtPhone.getText().trim(),
                txtLocation.getText().trim()
        );
    }

    private void loadAllRestaurants() {
        tableModel.setRowCount(0);
        try {
            List<RestaurantResponse> restaurants = restaurantService.findAllRestaurants();

            int totalRating = 0;
            int ratedRestaurants = 0;

            for (RestaurantResponse restaurant : restaurants) {
                String ratingDisplay = getStarRating(restaurant.getRating());
                tableModel.addRow(new Object[]{
                        restaurant.getId(),
                        restaurant.getName(),
                        restaurant.getCategory(),
                        ratingDisplay + " (" + restaurant.getRating() + "/5)",
                        formatPhone(restaurant.getPhone_number()),
                        restaurant.getLocation(),
                        restaurant.getId() // Hidden column for reference
                });

                if (restaurant.getRating() > 0) {
                    totalRating += restaurant.getRating();
                    ratedRestaurants++;
                }
            }

            // Update statistics
            lblTotalRestaurants.setText("🏢 Total: " + restaurants.size());

            double avgRating = ratedRestaurants > 0 ? (double) totalRating / ratedRestaurants : 0;
            lblAvgRating.setText(String.format("⭐ Avg Rating: %.1f/5", avgRating));
            lblAvgRating.setForeground(getRatingColor((int) Math.round(avgRating)));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Error loading restaurants: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getStarRating(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }

    private Color getRatingColor(int rating) {
        switch (rating) {
            case 5: return Color.decode("#27ae60"); // Green
            case 4: return Color.decode("#2ecc71"); // Light Green
            case 3: return Color.decode("#f39c12"); // Orange
            case 2: return Color.decode("#e67e22"); // Dark Orange
            case 1: return Color.decode("#e74c3c"); // Red
            default: return Color.decode("#95a5a6"); // Gray
        }
    }

    private String formatPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return "N/A";
        // Simple formatting: (123) 456-7890
        phone = phone.replaceAll("[^\\d]", "");
        if (phone.length() == 10) {
            return String.format("(%s) %s-%s",
                    phone.substring(0, 3),
                    phone.substring(3, 6),
                    phone.substring(6));
        }
        return phone;
    }

    private void fillFormFromSelectedRow() {
        int selectedRow = restaurantTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = restaurantTable.convertRowIndexToModel(selectedRow);

        txtId.setText(tableModel.getValueAt(modelRow, 0).toString());
        txtName.setText(tableModel.getValueAt(modelRow, 1).toString());

        // Set category (add emoji back if needed)
        String category = tableModel.getValueAt(modelRow, 2).toString();
        for (int i = 0; i < txtCategory.getItemCount(); i++) {
            String item = txtCategory.getItemAt(i);
            if (item.contains(category)) {
                txtCategory.setSelectedIndex(i);
                break;
            }
        }

        // Extract rating from display string like "★★★☆☆ (3/5)"
        String ratingDisplay = tableModel.getValueAt(modelRow, 3).toString();
        if (ratingDisplay.contains("(")) {
            String ratingStr = ratingDisplay.substring(
                    ratingDisplay.indexOf("(") + 1,
                    ratingDisplay.indexOf("/")
            ).trim();
            try {
                int rating = Integer.parseInt(ratingStr);
                txtRating.setValue(rating);
                lblRatingValue.setText("⭐ " + rating + "/5");
                lblRatingValue.setForeground(getRatingColor(rating));
            } catch (NumberFormatException e) {
                txtRating.setValue(0);
            }
        }

        // Phone (remove formatting)
        String phone = tableModel.getValueAt(modelRow, 4).toString();
        txtPhone.setText(phone.equals("N/A") ? "" : phone.replaceAll("[^\\d]", ""));

        txtLocation.setText(tableModel.getValueAt(modelRow, 5).toString());

        // Update selected info label
        lblSelectedInfo.setText("✅ Selected: " + txtName.getText());
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtCategory.setSelectedIndex(0);
        txtRating.setValue(0);
        txtPhone.setText("");
        txtLocation.setText("");
        lblSelectedInfo.setText("👆 Select a restaurant");
        restaurantTable.clearSelection();
    }

    // UI Helper methods
    private JTextField createTextField(boolean editable) {
        JTextField field = new JTextField();
        field.setEditable(editable);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return field;
    }

    private void addFormField(String label, JTextField field, int x, int y, int width, JPanel panel) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(x, y, 130, 25);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(Color.decode("#2c3e50"));
        panel.add(lbl);

        field.setBounds(x + 140, y, width - 140, 30);
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

    private JButton createIconButton(String icon, Color color, int fontSize) {
        JButton button = new JButton(icon);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, fontSize));
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
}