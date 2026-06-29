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
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantPanel extends JPanel {
    private final ServiceRestaurantImp restaurantService = new ServiceRestaurantImp();

    // Form fields
    private final JTextField txtId = createTextField(false);
    private final JTextField txtName = createTextField(true);
    private final JComboBox<String> txtCategory = new JComboBox<>(new String[]{
            "Italian", "Japanese", "American", "Mexican",
            "Chinese", "Korean", "Vietnamese", "Healthy",
            "Cafe", "Dessert", "Pub", "Steakhouse"
    });
    private final JSlider txtRating = new JSlider(0, 5, 0);
    private final JLabel lblRatingValue = new JLabel("0/5");
    private final JTextField txtPhone = createTextField(true);
    private final JTextField txtLocation = createTextField(true);

    // Buttons
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    // Search field
    private final JTextField txtSearch = new JTextField();

    // Table
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Category", "Rating", "Phone", "Location", "Internal ID"}, 0
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
    private final JLabel lblTotalRestaurants = new JLabel("Total: 0");
    private final JLabel lblAvgRating = new JLabel("Avg Rating: 0.0");
    private final JLabel lblSelectedInfo = new JLabel("Select a restaurant");

    private JSplitPane splitPane;

    public RestaurantPanel() {
        initializeUI();
        setupEventListeners();
        loadAllRestaurants();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Make it full screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(screenSize);

        // Top panel with title and search
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center panel with split view
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.35);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);

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

        // Add resize listener
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeComponents();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                // Set divider location after component is shown
                SwingUtilities.invokeLater(() -> {
                    if (splitPane != null && splitPane.getWidth() > 0) {
                        splitPane.setDividerLocation(0.35);
                    }
                });
            }
        });
    }

    private void resizeComponents() {
        if (splitPane != null && splitPane.getWidth() > 0) {
            splitPane.setDividerLocation(0.35);
        }
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#f8f9fa"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#3498db")),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        // Title
        JLabel titleLabel = new JLabel("Restaurant Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(Color.decode("#2c3e50"));

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(15, 0));
        searchPanel.setBackground(Color.decode("#f8f9fa"));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        searchLabel.setForeground(Color.decode("#2c3e50"));

        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#3498db"), 2),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        txtSearch.setPreferredSize(new Dimension(400, 45));
        txtSearch.setToolTipText("Search by name, category, location...");

        JButton btnClearSearch = createIconButton("Clear", Color.decode("#e74c3c"), 16);
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
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Form title
        JLabel formTitle = new JLabel("Add/Edit Restaurant");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        formTitle.setForeground(Color.decode("#2c3e50"));
        formTitle.setBounds(20, 20, 400, 40);
        panel.add(formTitle);

        int yPos = 80;
        int fieldWidth = 500;
        int labelWidth = 180;

        // Restaurant ID (read-only)
        addFormField("Restaurant ID:", txtId, 20, yPos, labelWidth, fieldWidth, panel);
        yPos += 55;

        // Name field
        addFormField("Restaurant Name:", txtName, 20, yPos, labelWidth, fieldWidth, panel);
        yPos += 55;

        // Category combo
        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setBounds(20, yPos, labelWidth, 35);
        lblCategory.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblCategory.setForeground(Color.decode("#2c3e50"));
        panel.add(lblCategory);

        txtCategory.setBounds(20 + labelWidth + 10, yPos, fieldWidth - labelWidth - 30, 35);
        txtCategory.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txtCategory.setBackground(Color.WHITE);
        panel.add(txtCategory);
        yPos += 55;

        // Rating slider
        JLabel lblRating = new JLabel("Rating:");
        lblRating.setBounds(20, yPos, labelWidth, 35);
        lblRating.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblRating.setForeground(Color.decode("#2c3e50"));
        panel.add(lblRating);

        txtRating.setBounds(20 + labelWidth + 10, yPos, 200, 35);
        txtRating.setMajorTickSpacing(1);
        txtRating.setPaintTicks(true);
        txtRating.setPaintLabels(true);
        txtRating.setSnapToTicks(true);
        txtRating.setBackground(Color.WHITE);
        panel.add(txtRating);

        lblRatingValue.setBounds(20 + labelWidth + 10 + 210, yPos, 80, 35);
        lblRatingValue.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblRatingValue.setForeground(Color.decode("#f39c12"));
        panel.add(lblRatingValue);
        yPos += 55;

        // Phone field
        addFormField("Phone Number:", txtPhone, 20, yPos, labelWidth, fieldWidth, panel);
        yPos += 55;

        // Location field
        addFormField("Location:", txtLocation, 20, yPos, labelWidth, fieldWidth, panel);
        yPos += 65;

        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        buttonPanel.setBounds(20, yPos, fieldWidth + 10, 110);
        buttonPanel.setBackground(Color.WHITE);

        btnAdd = createStyledButton("Add Restaurant", Color.decode("#2ecc71"));
        btnUpdate = createStyledButton("Update", Color.decode("#3498db"));
        btnDelete = createStyledButton("Delete", Color.decode("#e74c3c"));
        btnClear = createStyledButton("Clear", Color.decode("#95a5a6"));

        // Make buttons larger
        Dimension buttonSize = new Dimension(180, 45);
        btnAdd.setPreferredSize(buttonSize);
        btnUpdate.setPreferredSize(buttonSize);
        btnDelete.setPreferredSize(buttonSize);
        btnClear.setPreferredSize(buttonSize);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        panel.add(buttonPanel);

        yPos += 120;

        // Quick actions
        JLabel quickActions = new JLabel("Quick Actions:");
        quickActions.setBounds(20, yPos, 150, 30);
        quickActions.setFont(new Font("SansSerif", Font.BOLD, 16));
        quickActions.setForeground(Color.decode("#2c3e50"));
        panel.add(quickActions);

        JPanel quickButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        quickButtons.setBounds(180, yPos, fieldWidth - 160, 35);
        quickButtons.setBackground(Color.WHITE);

        JButton btnRefresh = createIconButton("Refresh", Color.decode("#9b59b6"), 14);
        btnRefresh.setToolTipText("Refresh List");
        JButton btnViewDetails = createIconButton("Details", Color.decode("#1abc9c"), 14);
        btnViewDetails.setToolTipText("View Details");
        JButton btnCopyPhone = createIconButton("Copy Phone", Color.decode("#f39c12"), 14);
        btnCopyPhone.setToolTipText("Copy Phone");

        btnRefresh.addActionListener(e -> refreshRestaurants());
        btnViewDetails.addActionListener(e -> showSelectedDetails());
        btnCopyPhone.addActionListener(e -> copyToClipboard(txtPhone.getText()));

        quickButtons.add(btnRefresh);
        quickButtons.add(btnViewDetails);
        quickButtons.add(btnCopyPhone);
        panel.add(quickButtons);

        yPos += 50;

        // Tips
        JLabel tipsLabel = new JLabel("<html><div style='text-align: center;'>Tips:<br>" +
                "• Double-click row to edit<br>" +
                "• Click category to filter<br>" +
                "• Use Ctrl+F to search<br>" +
                "• Press Enter to navigate fields</div></html>");
        tipsLabel.setBounds(20, yPos, fieldWidth + 10, 80);
        tipsLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        tipsLabel.setForeground(Color.decode("#7f8c8d"));
        tipsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(tipsLabel);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 30));

        // Table header
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);
        tableHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel tableTitle = new JLabel("Restaurants List");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        tableTitle.setForeground(Color.decode("#2c3e50"));

        lblSelectedInfo.setFont(new Font("SansSerif", Font.ITALIC, 14));
        lblSelectedInfo.setForeground(Color.decode("#7f8c8d"));

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(lblSelectedInfo, BorderLayout.EAST);

        // Configure table
        restaurantTable.setRowHeight(45);
        restaurantTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        restaurantTable.getTableHeader().setBackground(Color.decode("#34495e"));
        restaurantTable.getTableHeader().setForeground(Color.WHITE);
        restaurantTable.getTableHeader().setPreferredSize(new Dimension(0, 50));
        restaurantTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        restaurantTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
                } else {
                    c.setBackground(new Color(220, 240, 255));
                    c.setForeground(Color.BLACK);
                }

                // Style rating column
                if (column == 3 && value != null) {
                    setForeground(Color.decode("#f39c12")); // Orange for rating
                    setFont(getFont().deriveFont(Font.BOLD));
                }

                // Center align ID column
                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                }

                // Add padding
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(restaurantTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(tableHeader, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 15));
        panel.setBackground(Color.decode("#f1f8ff"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, Color.decode("#3498db")),
                BorderFactory.createEmptyBorder(15, 30, 15, 30)
        ));

        lblTotalRestaurants.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTotalRestaurants.setForeground(Color.decode("#2c3e50"));

        lblAvgRating.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblAvgRating.setForeground(Color.decode("#f39c12"));

        panel.add(lblTotalRestaurants);
        panel.add(lblAvgRating);

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
            lblRatingValue.setText(rating + "/5");
            lblRatingValue.setForeground(getRatingColor(rating));
        });

        // Button actions
        btnAdd.addActionListener(e -> addRestaurant());
        btnUpdate.addActionListener(e -> updateRestaurant());
        btnDelete.addActionListener(e -> deleteRestaurant());
        btnClear.addActionListener(e -> clearForm());

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

        // Keyboard shortcuts
        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        // Ctrl+F to focus search
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("control F"), "focusSearch");
        getActionMap().put("focusSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtSearch.requestFocus();
                txtSearch.selectAll();
            }
        });

        // Delete key to delete selected restaurant
        restaurantTable.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("DELETE"), "deleteRestaurant");
        restaurantTable.getActionMap().put("deleteRestaurant", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteRestaurant();
            }
        });

        // F5 to refresh
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F5"), "refresh");
        getActionMap().put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshRestaurants();
            }
        });

        // Ctrl+N for new restaurant
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("control N"), "newRestaurant");
        getActionMap().put("newRestaurant", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
                txtName.requestFocus();
            }
        });

        // Enter key navigation
        txtName.addActionListener(e -> txtCategory.requestFocus());
        txtCategory.addActionListener(e -> txtRating.requestFocus());
        txtPhone.addActionListener(e -> txtLocation.requestFocus());
        txtLocation.addActionListener(e -> {
            if (txtId.getText().isEmpty()) addRestaurant();
            else updateRestaurant();
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
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = restaurantTable.convertRowIndexToModel(selectedRow);
        String details = String.format(
                "<html><div style='font-size:12pt;'>" +
                        "<b>Restaurant Details</b><br><br>" +
                        "ID: %s<br>" +
                        "Name: %s<br>" +
                        "Category: %s<br>" +
                        "Rating: %s<br>" +
                        "Phone: %s<br>" +
                        "Location: %s" +
                        "</div></html>",
                tableModel.getValueAt(modelRow, 0),
                tableModel.getValueAt(modelRow, 1),
                tableModel.getValueAt(modelRow, 2),
                tableModel.getValueAt(modelRow, 3),
                tableModel.getValueAt(modelRow, 4),
                tableModel.getValueAt(modelRow, 5)
        );

        JOptionPane.showMessageDialog(this, details,
                "Restaurant Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void copyToClipboard(String text) {
        if (text == null || text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No text to copy!", "Empty",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(text);
        java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);

        JOptionPane.showMessageDialog(this,
                "Copied to clipboard: " + text,
                "Copied", JOptionPane.INFORMATION_MESSAGE);
    }

    public void addRestaurant() {
        try {
            // Validate required fields
            if (txtName.getText().trim().isEmpty()) {
                showValidationError("Please enter restaurant name!", txtName);
                return;
            }

            if (txtLocation.getText().trim().isEmpty()) {
                showValidationError("Please enter restaurant location!", txtLocation);
                return;
            }

            RestaurantRequest request = collectFormData();
            restaurantService.CreateRestaurant(request);
            JOptionPane.showMessageDialog(this,
                    "<html><div style='font-size:12pt;'>" +
                            "<b>Restaurant added successfully!</b><br><br>" +
                            "Name: " + request.getName() + "<br>" +
                            "Category: " + request.getCategory() + "<br>" +
                            "Rating: " + request.getRating() + "/5" +
                            "</div></html>",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            loadAllRestaurants();
            clearForm();
            txtName.requestFocus();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showValidationError(String message, JComponent field) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
        field.requestFocus();
        if (field instanceof JTextField) {
            ((JTextField) field).selectAll();
        }
    }

    public void updateRestaurant() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a restaurant to update", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><div style='font-size:12pt;'>Update this restaurant's information?</div></html>",
                "Confirm Update",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(txtId.getText());
                RestaurantRequest request = collectFormData();
                restaurantService.updateRestaurant(request, id);
                JOptionPane.showMessageDialog(this,
                        "Restaurant updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadAllRestaurants();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void deleteRestaurant() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a restaurant to delete", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(txtId.getText());

        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><div style='font-size:12pt;'>" +
                        "<b>Are you sure you want to delete this restaurant?</b><br>" +
                        "This action cannot be undone!<br><br>" +
                        "All menu items from this restaurant will also be deleted.<br><br>" +
                        "<b>Restaurant Details:</b><br>" +
                        "Name: " + txtName.getText() + "<br>" +
                        "Category: " + txtCategory.getSelectedItem() +
                        "</div></html>",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                restaurantService.deleteRestaurant(id);
                JOptionPane.showMessageDialog(this,
                        "Restaurant deleted successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadAllRestaurants();
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshRestaurants() {
        loadAllRestaurants();
        clearForm();
        txtSearch.setText("");
        JOptionPane.showMessageDialog(this,
                "Restaurant list refreshed!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private RestaurantRequest collectFormData() {
        String category = (String) txtCategory.getSelectedItem();
        // No emoji removal needed

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
                tableModel.addRow(new Object[]{
                        restaurant.getId(),
                        restaurant.getName(),
                        restaurant.getCategory(),
                        restaurant.getRating() + "/5",
                        restaurant.getPhone_number() != null ? restaurant.getPhone_number() : "N/A",
                        restaurant.getLocation(),
                        restaurant.getId() // Hidden column for reference
                });

                if (restaurant.getRating() > 0) {
                    totalRating += restaurant.getRating();
                    ratedRestaurants++;
                }
            }

            // Update statistics
            lblTotalRestaurants.setText("Total: " + restaurants.size());

            double avgRating = ratedRestaurants > 0 ? (double) totalRating / ratedRestaurants : 0;
            lblAvgRating.setText(String.format("Avg Rating: %.1f/5", avgRating));
            lblAvgRating.setForeground(getRatingColor((int) Math.round(avgRating)));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading restaurants: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // For debugging
        }
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

    private void fillFormFromSelectedRow() {
        int selectedRow = restaurantTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = restaurantTable.convertRowIndexToModel(selectedRow);

        txtId.setText(tableModel.getValueAt(modelRow, 0).toString());
        txtName.setText(tableModel.getValueAt(modelRow, 1).toString());

        // Set category
        String category = tableModel.getValueAt(modelRow, 2).toString();
        boolean found = false;
        for (int i = 0; i < txtCategory.getItemCount(); i++) {
            String item = txtCategory.getItemAt(i);
            if (item.equals(category)) {
                txtCategory.setSelectedIndex(i);
                found = true;
                break;
            }
        }

        // If category not found, add it
        if (!found && !category.isEmpty()) {
            txtCategory.addItem(category);
            txtCategory.setSelectedItem(category);
        }

        // Extract rating from display string like "3/5"
        String ratingDisplay = tableModel.getValueAt(modelRow, 3).toString();
        if (ratingDisplay.contains("/")) {
            String ratingStr = ratingDisplay.substring(0, ratingDisplay.indexOf("/")).trim();
            try {
                int rating = Integer.parseInt(ratingStr);
                txtRating.setValue(rating);
                lblRatingValue.setText(rating + "/5");
                lblRatingValue.setForeground(getRatingColor(rating));
            } catch (NumberFormatException e) {
                txtRating.setValue(0);
            }
        }

        // Phone (remove formatting if any)
        String phone = tableModel.getValueAt(modelRow, 4).toString();
        txtPhone.setText(phone.equals("N/A") ? "" : phone);

        txtLocation.setText(tableModel.getValueAt(modelRow, 5).toString());

        // Update selected info label
        lblSelectedInfo.setText("Selected: " + txtName.getText());
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtCategory.setSelectedIndex(0);
        txtRating.setValue(0);
        txtPhone.setText("");
        txtLocation.setText("");
        lblSelectedInfo.setText("Select a restaurant");
        restaurantTable.clearSelection();
        tableSorter.setRowFilter(null);
    }

    // UI Helper methods
    private JTextField createTextField(boolean editable) {
        JTextField field = new JTextField();
        field.setEditable(editable);
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        return field;
    }

    private void addFormField(String label, JTextField field, int x, int y, int labelWidth, int fieldWidth, JPanel panel) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(x, y, labelWidth, 35);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        lbl.setForeground(Color.decode("#2c3e50"));
        panel.add(lbl);

        field.setBounds(x + labelWidth + 10, y, fieldWidth - labelWidth - 30, 35);
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        panel.add(field);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                BorderFactory.createEmptyBorder(12, 25, 12, 25)
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

    private JButton createIconButton(String text, Color color, int fontSize) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
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