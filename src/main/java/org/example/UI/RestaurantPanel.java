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
    private final JLabel lblImagePreview = new JLabel("No Image", SwingConstants.CENTER);

    // Path of the image staged/stored for the restaurant currently in the form
    private String selectedImagePath;
    // Restaurant ID -> stored image path, refreshed on every load
    private final java.util.Map<Integer, String> imagePathById = new java.util.HashMap<>();

    // Buttons
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnChooseImage;

    // Search field
    private final JTextField txtSearch = new JTextField();

    // Table
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"", "ID", "Name", "Category", "Rating", "Phone", "Location", "Internal ID"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) return Icon.class;
            if (column == 1 || column == 7) return Integer.class;
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
        setBackground(Color.decode("#F5F6F8"));

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

        // Left panel - Form (scrollable so it never gets clipped on smaller windows)
        JScrollPane formScroll = new JScrollPane(createFormPanel());
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(Color.decode("#F5F6F8"));
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        splitPane.setLeftComponent(formScroll);

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
        panel.setBackground(Color.decode("#F5F6F8"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#4F46E5")),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        // Title
        JLabel titleLabel = new JLabel("🏪 Restaurant Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(Color.decode("#111827"));

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(15, 0));
        searchPanel.setBackground(Color.decode("#F5F6F8"));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        searchLabel.setForeground(Color.decode("#111827"));

        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txtSearch.setBorder(new UITheme.RoundedLineBorder(UITheme.PRIMARY, 16, 12, 20));
        txtSearch.setPreferredSize(new Dimension(400, 45));
        txtSearch.setToolTipText("Search by name, category, location...");

        JButton btnClearSearch = createIconButton("Clear", Color.decode("#EF4444"), 16);
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
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Color.decode("#F5F6F8"));
        outer.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("✏️  Add / Edit Restaurant");
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
        JLabel imgHint = new JLabel("<html>A photo helps customers recognize<br>this restaurant while ordering.</html>");
        imgHint.setFont(UITheme.FONT_MUTED);
        imgHint.setForeground(UITheme.TEXT_MUTED);
        imgHint.setAlignmentX(LEFT_ALIGNMENT);
        btnChooseImage = createStyledButton("📷 Choose Image", UITheme.PRIMARY);
        btnChooseImage.setAlignmentX(LEFT_ALIGNMENT);
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
        gbc.weightx = 1;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        fields.add(fieldGroup("Restaurant ID (auto-assigned)", txtId), gbc);

        gbc.gridx = 0; gbc.gridy = row++;
        fields.add(fieldGroup("Restaurant Name", txtName), gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        fields.add(fieldGroup("Category", txtCategory), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        fields.add(ratingGroup(), gbc);

        gbc.gridx = 0; gbc.gridy = row;
        fields.add(fieldGroup("Phone Number", txtPhone), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        fields.add(fieldGroup("Location", txtLocation), gbc);

        card.add(fields, BorderLayout.SOUTH);

        // ---- Actions ----
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setBorder(BorderFactory.createEmptyBorder(4, 24, 24, 24));

        JPanel row1 = new JPanel(new GridLayout(1, 2, 10, 0));
        row1.setOpaque(false);
        btnAdd = createStyledButton("Add Restaurant", UITheme.SUCCESS);
        btnUpdate = createStyledButton("Update", UITheme.PRIMARY);
        row1.add(btnAdd);
        row1.add(btnUpdate);

        JPanel row2 = new JPanel(new GridLayout(1, 2, 10, 0));
        row2.setOpaque(false);
        btnDelete = createStyledButton("Delete", UITheme.SECONDARY);
        btnClear = createStyledButton("Clear", UITheme.NEUTRAL);
        row2.add(btnDelete);
        row2.add(btnClear);

        row1.setAlignmentX(LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        row2.setAlignmentX(LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        actions.add(row1);
        actions.add(Box.createVerticalStrut(10));
        actions.add(row2);
        actions.add(Box.createVerticalStrut(18));

        JLabel quickActionsLabel = new JLabel("QUICK ACTIONS");
        quickActionsLabel.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 12));
        quickActionsLabel.setForeground(UITheme.PRIMARY);
        quickActionsLabel.setAlignmentX(LEFT_ALIGNMENT);
        quickActionsLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 0));
        actions.add(quickActionsLabel);

        JPanel quickButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        quickButtons.setOpaque(false);
        quickButtons.setAlignmentX(LEFT_ALIGNMENT);
        quickButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton btnRefresh = createIconButton("Refresh", UITheme.PRIMARY, 14);
        btnRefresh.setToolTipText("Refresh List");
        JButton btnViewDetails = createIconButton("Details", UITheme.SUCCESS, 14);
        btnViewDetails.setToolTipText("View Details");
        JButton btnCopyPhone = createIconButton("Copy Phone", UITheme.ACCENT, 14);
        btnCopyPhone.setToolTipText("Copy Phone");

        btnRefresh.addActionListener(e -> refreshRestaurants());
        btnViewDetails.addActionListener(e -> showSelectedDetails());
        btnCopyPhone.addActionListener(e -> copyToClipboard(txtPhone.getText()));

        quickButtons.add(btnRefresh);
        quickButtons.add(btnViewDetails);
        quickButtons.add(btnCopyPhone);
        actions.add(quickButtons);
        actions.add(Box.createVerticalStrut(16));

        JLabel tipsLabel = new JLabel("<html><div style='text-align:left;'>"
                + "Tips: Double-click a row to edit &middot; Ctrl+F to search &middot; "
                + "Ctrl+N for a new restaurant &middot; Enter to move between fields</div></html>");
        tipsLabel.setFont(UITheme.FONT_MUTED);
        tipsLabel.setForeground(UITheme.TEXT_MUTED);
        tipsLabel.setAlignmentX(LEFT_ALIGNMENT);
        actions.add(tipsLabel);

        JPanel cardWithActions = new JPanel(new BorderLayout());
        cardWithActions.setOpaque(false);
        cardWithActions.add(card, BorderLayout.CENTER);
        cardWithActions.add(actions, BorderLayout.SOUTH);

        outer.add(cardWithActions, BorderLayout.NORTH);
        return outer;
    }

    private JPanel fieldGroup(String label, JComponent field) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        lbl.setForeground(UITheme.TEXT_DARK);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);

        field.setFont(UITheme.FONT_BODY);
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setPreferredSize(new Dimension(10, 40));
        if (field instanceof JComboBox) {
            ((JComboBox<?>) field).setBackground(Color.WHITE);
        } else {
            field.setBorder(new UITheme.RoundedLineBorder(UITheme.BORDER, 12, 10, 12));
        }

        group.add(lbl);
        group.add(field);
        return group;
    }

    private JPanel ratingGroup() {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel("Rating");
        lbl.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        lbl.setForeground(UITheme.TEXT_DARK);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        txtRating.setMajorTickSpacing(1);
        txtRating.setPaintTicks(true);
        txtRating.setPaintLabels(true);
        txtRating.setSnapToTicks(true);
        txtRating.setOpaque(false);
        row.add(txtRating, BorderLayout.CENTER);

        lblRatingValue.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 15));
        lblRatingValue.setForeground(UITheme.ACCENT);
        lblRatingValue.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
        row.add(lblRatingValue, BorderLayout.EAST);

        group.add(lbl);
        group.add(row);
        return group;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.decode("#F5F6F8"));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 30));

        // Table header
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.decode("#F5F6F8"));
        tableHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel tableTitle = new JLabel("📋 Restaurants List");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        tableTitle.setForeground(Color.decode("#111827"));

        lblSelectedInfo.setFont(new Font("SansSerif", Font.ITALIC, 14));
        lblSelectedInfo.setForeground(Color.decode("#6B7280"));

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(lblSelectedInfo, BorderLayout.EAST);

        // Configure table
        restaurantTable.setRowHeight(45);
        restaurantTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        restaurantTable.getTableHeader().setBackground(Color.decode("#4F46E5"));
        restaurantTable.getTableHeader().setForeground(Color.WHITE);
        restaurantTable.getTableHeader().setPreferredSize(new Dimension(0, 50));
        restaurantTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        restaurantTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Thumbnail column: small, fixed width, no header text
        restaurantTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        restaurantTable.getColumnModel().getColumn(0).setMaxWidth(55);
        restaurantTable.getColumnModel().getColumn(0).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
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

        // Hide internal ID column
        restaurantTable.removeColumn(restaurantTable.getColumnModel().getColumn(7));

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
                        c.setBackground(Color.decode("#F5F6F8"));
                    } else {
                        c.setBackground(new Color(249, 250, 251));
                    }
                } else {
                    c.setBackground(new Color(224, 231, 255));
                    c.setForeground(Color.BLACK);
                }

                // Style rating column
                if (column == 4 && value != null) {
                    setForeground(Color.decode("#F59E0B")); // Orange for rating
                    setFont(getFont().deriveFont(Font.BOLD));
                }

                // Center align ID column
                if (column == 1) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                }

                // Add padding
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(restaurantTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        scrollPane.getViewport().setBackground(Color.decode("#F5F6F8"));

        panel.add(tableHeader, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 15));
        panel.setBackground(Color.decode("#FFFFFF"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, Color.decode("#4F46E5")),
                BorderFactory.createEmptyBorder(15, 30, 15, 30)
        ));

        lblTotalRestaurants.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTotalRestaurants.setForeground(Color.decode("#111827"));

        lblAvgRating.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblAvgRating.setForeground(Color.decode("#F59E0B"));

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
        btnChooseImage.addActionListener(e -> chooseImage());

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
                            txtCategory.setBackground(new Color(224, 231, 255));
                            Timer timer = new Timer(1000, evt -> {
                                txtCategory.setBackground(Color.decode("#F5F6F8"));
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

            // Search in all visible text columns (skip the thumbnail icon and ID columns)
            for (int i = 0; i < tableModel.getColumnCount() - 1; i++) {
                if (i != 0 && i != 1) {
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
                tableModel.getValueAt(modelRow, 1),
                tableModel.getValueAt(modelRow, 2),
                tableModel.getValueAt(modelRow, 3),
                tableModel.getValueAt(modelRow, 4),
                tableModel.getValueAt(modelRow, 5),
                tableModel.getValueAt(modelRow, 6)
        );

        JPanel detailPanel = new JPanel(new BorderLayout(0, 10));
        detailPanel.setOpaque(false);
        detailPanel.add(new JLabel(details), BorderLayout.CENTER);

        String imgPath = imagePathById.get((Integer) tableModel.getValueAt(modelRow, 1));
        if (imgPath != null && !imgPath.isBlank()) {
            java.io.File imgFile = new java.io.File(imgPath);
            if (imgFile.exists()) {
                Image scaled = new ImageIcon(imgFile.getAbsolutePath())
                        .getImage().getScaledInstance(220, 150, Image.SCALE_SMOOTH);
                detailPanel.add(new JLabel(new ImageIcon(scaled)), BorderLayout.NORTH);
            }
        }

        JOptionPane.showMessageDialog(this, detailPanel,
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

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Restaurant Image");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File selected = chooser.getSelectedFile();
        try {
            java.io.File imagesDir = new java.io.File("images/restaurants");
            imagesDir.mkdirs();

            String ext = "";
            int dot = selected.getName().lastIndexOf('.');
            if (dot >= 0) ext = selected.getName().substring(dot);

            java.io.File dest = new java.io.File(imagesDir, "restaurant_" + System.currentTimeMillis() + ext);
            java.nio.file.Files.copy(selected.toPath(), dest.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            selectedImagePath = "images/restaurants/" + dest.getName();
            showImagePreview(selectedImagePath);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load image: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showImagePreview(String path) {
        if (path == null || path.isBlank()) {
            lblImagePreview.setIcon(null);
            lblImagePreview.setText("No Image");
            return;
        }

        java.io.File file = new java.io.File(path);
        if (!file.exists()) {
            lblImagePreview.setIcon(null);
            lblImagePreview.setText("Missing");
            return;
        }

        Image scaled = new ImageIcon(file.getAbsolutePath())
                .getImage().getScaledInstance(120, 80, Image.SCALE_SMOOTH);
        lblImagePreview.setText("");
        lblImagePreview.setIcon(new ImageIcon(scaled));
    }

    /** Small 32x32 table thumbnail: the restaurant's photo if set, else a lettered placeholder. */
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
                txtLocation.getText().trim(),
                selectedImagePath
        );
    }

    private void loadAllRestaurants() {
        tableModel.setRowCount(0);
        imagePathById.clear();
        try {
            List<RestaurantResponse> restaurants = restaurantService.findAllRestaurants();

            int totalRating = 0;
            int ratedRestaurants = 0;

            for (RestaurantResponse restaurant : restaurants) {
                imagePathById.put(restaurant.getId(), restaurant.getImage_path());
                tableModel.addRow(new Object[]{
                        rowThumbnail(restaurant.getImage_path(), restaurant.getName()),
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
            case 5: return Color.decode("#16A34A"); // Green
            case 4: return Color.decode("#16A34A"); // Light Green
            case 3: return Color.decode("#F59E0B"); // Orange
            case 2: return Color.decode("#F59E0B"); // Dark Orange
            case 1: return Color.decode("#EF4444"); // Red
            default: return Color.decode("#64748B"); // Gray
        }
    }

    private void fillFormFromSelectedRow() {
        int selectedRow = restaurantTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = restaurantTable.convertRowIndexToModel(selectedRow);

        txtId.setText(tableModel.getValueAt(modelRow, 1).toString());
        txtName.setText(tableModel.getValueAt(modelRow, 2).toString());

        // Set category
        String category = tableModel.getValueAt(modelRow, 3).toString();
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
        String ratingDisplay = tableModel.getValueAt(modelRow, 4).toString();
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
        String phone = tableModel.getValueAt(modelRow, 5).toString();
        txtPhone.setText(phone.equals("N/A") ? "" : phone);

        txtLocation.setText(tableModel.getValueAt(modelRow, 6).toString());

        // Load the stored image for this restaurant, if any
        selectedImagePath = imagePathById.get((Integer) tableModel.getValueAt(modelRow, 1));
        showImagePreview(selectedImagePath);

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
        selectedImagePath = null;
        showImagePreview(null);
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

    private JButton createStyledButton(String text, Color color) {
        return UITheme.createButton(text, color);
    }

    private JButton createIconButton(String text, Color color, int fontSize) {
        return UITheme.createButton(text, color);
    }
}