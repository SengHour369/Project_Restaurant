package org.example.UI;

import org.example.DTO.Request.UserRequest;
import org.example.DTO.Response.UserResponse;
import org.example.Exception.MessageException;
import org.example.Service.ServiceImplement.ServiceUserImp;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class UserPanel extends JPanel {
    private final ServiceUserImp userService = new ServiceUserImp();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Form fields with emojis
    private final JTextField txtId = createTextField(false, "🆔");
    private final JTextField txtName = createTextField(true, "👤");
    private final JComboBox<String> txtGender = new JComboBox<>(new String[]{"", "👨 Male", "👩 Female", "🌈 Other"});
    private final JTextField txtDob = createTextField(true, "🎂");
    private final JTextField txtPhone = createTextField(true, "📞");
    private final JTextField txtAddress = createTextField(true, "🏠");
    private final JTextField txtEmail = createTextField(true, "📧");
    private final JPasswordField txtPassword = createPasswordField();
    private final JComboBox<String> txtStatus = new JComboBox<>(new String[]{"👤 USER", "👑 ADMIN", "🚫 INACTIVE"});

    // Search field
    private final JTextField txtSearch = new JTextField();

    // Table with cute headers
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"🆔 ID", "👤 Name", "⚤ Gender", "🎂 Date of Birth", "📞 Phone", "🏠 Address", "📧 Email", "👑 Status", "🆔 Internal ID"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0 || column == 8) return Integer.class;
            return String.class;
        }
    };
    private final JTable userTable = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> tableSorter;

    // Statistics labels
    private final JLabel lblTotalUsers = new JLabel("👥 Total: 0");
    private final JLabel lblAdmins = new JLabel("👑 Admins: 0");
    private final JLabel lblSelectedInfo = new JLabel("👆 Select a user");

    // Buttons with emojis
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnRefresh;
    private JButton btnExport;
    private JButton btnSearchBtn;

    public UserPanel(UserResponse currentUser) {
        initializeUI();
        setupEventListeners();
        loadAllUsers();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top panel with title and search
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center panel with split view
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);
        splitPane.setResizeWeight(0.4);
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
        JLabel titleLabel = new JLabel("👥 User Management");
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
        txtSearch.setToolTipText("Search by name, email, phone, status...");

        btnSearchBtn = createIconButton("🔎", Color.decode("#3498db"), 14);
        btnSearchBtn.setToolTipText("Search");

        JButton btnClearSearch = createIconButton("🗑️", Color.decode("#e74c3c"), 14);
        btnClearSearch.setToolTipText("Clear search");
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            txtSearch.requestFocus();
        });

        JPanel searchButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchButtons.setBackground(Color.decode("#f8f9fa"));
        searchButtons.add(btnSearchBtn);
        searchButtons.add(btnClearSearch);

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(searchButtons, BorderLayout.EAST);

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
        JLabel formTitle = new JLabel("➕ Add/Edit User");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        formTitle.setForeground(Color.decode("#2c3e50"));
        formTitle.setBounds(20, 20, 400, 30);
        panel.add(formTitle);

        int yPos = 70;
        int fieldWidth = 400;

        // User ID (read-only)
        addFormField("🆔 User ID:", txtId, 20, yPos, fieldWidth, panel);
        yPos += 50;

        // Name field
        addFormField("👤 Full Name:", txtName, 20, yPos, fieldWidth, panel);
        yPos += 50;

        // Gender combo
        JLabel lblGender = new JLabel("⚤ Gender:");
        lblGender.setBounds(20, yPos, 120, 25);
        lblGender.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblGender.setForeground(Color.decode("#2c3e50"));
        panel.add(lblGender);

        txtGender.setBounds(150, yPos, fieldWidth - 130, 30);
        txtGender.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtGender.setBackground(Color.WHITE);
        panel.add(txtGender);
        yPos += 50;

        // Date of Birth with date picker hint
        addFormField("🎂 Date of Birth:", txtDob, 20, yPos, fieldWidth, panel);
        JLabel dobHint = new JLabel("(YYYY-MM-DD)");
        dobHint.setBounds(150 + fieldWidth - 130, yPos, 100, 25);
        dobHint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        dobHint.setForeground(Color.GRAY);
        panel.add(dobHint);
        yPos += 50;

        // Phone field
        addFormField("📞 Phone Number:", txtPhone, 20, yPos, fieldWidth, panel);
        yPos += 50;

        // Address field
        addFormField("🏠 Address:", txtAddress, 20, yPos, fieldWidth, panel);
        yPos += 50;

        // Email field
        addFormField("📧 Email:", txtEmail, 20, yPos, fieldWidth, panel);
        yPos += 50;

        // Password field with show/hide
        JLabel lblPassword = new JLabel("🔒 Password:");
        lblPassword.setBounds(20, yPos, 120, 25);
        lblPassword.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPassword.setForeground(Color.decode("#2c3e50"));
        panel.add(lblPassword);

        txtPassword.setBounds(150, yPos, fieldWidth - 180, 30);
        txtPassword.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panel.add(txtPassword);

        // Show/hide password button
        JButton btnShowPassword = createIconButton("👁️", Color.decode("#7f8c8d"), 12);
        btnShowPassword.setBounds(150 + fieldWidth - 180 + 5, yPos, 40, 30);
        btnShowPassword.setToolTipText("Show/Hide Password");
        btnShowPassword.addActionListener(e -> {
            if (txtPassword.getEchoChar() == '•') {
                txtPassword.setEchoChar((char) 0);
                btnShowPassword.setText("🙈");
            } else {
                txtPassword.setEchoChar('•');
                btnShowPassword.setText("👁️");
            }
        });
        panel.add(btnShowPassword);
        yPos += 50;

        // Status combo
        JLabel lblStatus = new JLabel("👑 Status:");
        lblStatus.setBounds(20, yPos, 120, 25);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblStatus.setForeground(Color.decode("#2c3e50"));
        panel.add(lblStatus);

        txtStatus.setBounds(150, yPos, fieldWidth - 130, 30);
        txtStatus.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtStatus.setBackground(Color.WHITE);
        panel.add(txtStatus);
        yPos += 60;

        // Main buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        buttonPanel.setBounds(20, yPos, fieldWidth, 90);
        buttonPanel.setBackground(Color.WHITE);

        btnAdd = createStyledButton("➕ Add User", Color.decode("#2ecc71"));
        btnUpdate = createStyledButton("✏️ Update", Color.decode("#3498db"));
        btnDelete = createStyledButton("🗑️ Delete", Color.decode("#e74c3c"));
        btnClear = createStyledButton("🧹 Clear", Color.decode("#95a5a6"));
        btnRefresh = createStyledButton("🔄 Refresh", Color.decode("#9b59b6"));
        btnExport = createStyledButton("📊 Export", Color.decode("#f39c12"));

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnExport);
        panel.add(buttonPanel);

        yPos += 110;

        // Quick tips
        JLabel tipsLabel = new JLabel("💡 Tips: Double-click row to edit | Click status to filter");
        tipsLabel.setBounds(20, yPos, fieldWidth, 20);
        tipsLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        tipsLabel.setForeground(Color.decode("#7f8c8d"));
        panel.add(tipsLabel);

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

        JLabel tableTitle = new JLabel("📋 Users List");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        tableTitle.setForeground(Color.decode("#2c3e50"));

        lblSelectedInfo.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblSelectedInfo.setForeground(Color.decode("#7f8c8d"));

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(lblSelectedInfo, BorderLayout.EAST);

        // Configure table
        userTable.setRowHeight(35);
        userTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        userTable.getTableHeader().setBackground(Color.decode("#34495e"));
        userTable.getTableHeader().setForeground(Color.WHITE);
        userTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Hide internal ID column
        userTable.removeColumn(userTable.getColumnModel().getColumn(8));

        // Set up table sorter
        tableSorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(tableSorter);

        // Custom cell renderer
        userTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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

                // Style based on column
                if (column == 7) { // Status column
                    String status = value.toString();
                    if (status.contains("ADMIN")) {
                        setForeground(Color.decode("#e74c3c")); // Red for admin
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (status.contains("INACTIVE")) {
                        setForeground(Color.decode("#95a5a6")); // Gray for inactive
                    } else {
                        setForeground(Color.decode("#27ae60")); // Green for active user
                    }
                }

                // Center align ID column
                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(userTable);
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

        lblTotalUsers.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTotalUsers.setForeground(Color.decode("#2c3e50"));

        lblAdmins.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblAdmins.setForeground(Color.decode("#e74c3c"));

        JLabel lblActive = new JLabel("✅ Active: 0");
        lblActive.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblActive.setForeground(Color.decode("#27ae60"));

        panel.add(lblTotalUsers);
        panel.add(lblAdmins);
        panel.add(lblActive);

        return panel;
    }

    private void setupEventListeners() {
        // Search functionality
        btnSearchBtn.addActionListener(e -> filterTable());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        // Button actions
        btnAdd.addActionListener(e -> addUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> refreshUsers());
        btnExport.addActionListener(e -> exportUsers());

        // Table selection listener
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelectedRow();
            }
        });

        // Double-click to edit
        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = userTable.rowAtPoint(e.getPoint());
                    int column = userTable.columnAtPoint(e.getPoint());

                    if (row >= 0) {
                        fillFormFromSelectedRow();

                        // If clicked on status cell, set combo box
                        if (column == 7) {
                            String status = tableModel.getValueAt(
                                    userTable.convertRowIndexToModel(row), 7).toString();
                            txtStatus.setSelectedItem(status);

                            // Show animation
                            txtStatus.setBackground(new Color(220, 237, 255));
                            Timer timer = new Timer(1000, evt -> {
                                txtStatus.setBackground(Color.WHITE);
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

        // Date validation for DOB field
        txtDob.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                validateDateOfBirth();
            }
        });
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

        // Delete key to delete selected user
        userTable.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("DELETE"), "deleteUser");
        userTable.getActionMap().put("deleteUser", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteUser();
            }
        });

        // F5 to refresh
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F5"), "refresh");
        getActionMap().put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshUsers();
            }
        });

        // Ctrl+N for new user
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("control N"), "newUser");
        getActionMap().put("newUser", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
                txtName.requestFocus();
            }
        });

        // Enter key navigation
        txtName.addActionListener(e -> txtGender.requestFocus());
        txtGender.addActionListener(e -> txtDob.requestFocus());
        txtDob.addActionListener(e -> txtPhone.requestFocus());
        txtPhone.addActionListener(e -> txtAddress.requestFocus());
        txtAddress.addActionListener(e -> txtEmail.requestFocus());
        txtEmail.addActionListener(e -> txtPassword.requestFocus());
        txtPassword.addActionListener(e -> txtStatus.requestFocus());
        txtStatus.addActionListener(e -> {
            if (txtId.getText().isEmpty()) addUser();
            else updateUser();
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

    private void validateDateOfBirth() {
        String dobText = txtDob.getText().trim();
        if (!dobText.isEmpty()) {
            try {
                LocalDate.parse(dobText, dateFormatter);
                txtDob.setBorder(BorderFactory.createLineBorder(Color.decode("#27ae60"), 1));
            } catch (DateTimeParseException e) {
                txtDob.setBorder(BorderFactory.createLineBorder(Color.decode("#e74c3c"), 1));
                JOptionPane.showMessageDialog(this,
                        "⚠️ Invalid date format! Please use YYYY-MM-DD",
                        "Date Error", JOptionPane.WARNING_MESSAGE);
                txtDob.requestFocus();
            }
        }
    }

    private void addUser() {
        try {
            // Validate required fields
            if (txtName.getText().trim().isEmpty()) {
                showValidationError("👤 Please enter user name", txtName);
                return;
            }
            if (txtEmail.getText().trim().isEmpty()) {
                showValidationError("📧 Please enter email", txtEmail);
                return;
            }
            if (txtPassword.getPassword().length == 0) {
                showValidationError("🔒 Please enter password", txtPassword);
                return;
            }

            // Validate email format
            String email = txtEmail.getText().trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showValidationError("📧 Invalid email format", txtEmail);
                return;
            }

            // Validate date if provided
            if (!txtDob.getText().trim().isEmpty()) {
                try {
                    LocalDate.parse(txtDob.getText().trim(), dateFormatter);
                } catch (DateTimeParseException e) {
                    showValidationError("🎂 Invalid date format (YYYY-MM-DD)", txtDob);
                    return;
                }
            }

            UserRequest request = collectFormData();
            userService.create(request);

            JOptionPane.showMessageDialog(this,
                    "✅ User added successfully!\n\n" +
                            "👤 Name: " + request.getName() + "\n" +
                            "📧 Email: " + request.getEmail() + "\n" +
                            "👑 Status: " + request.getStatus(),
                    "🎉 Success", JOptionPane.INFORMATION_MESSAGE);

            loadAllUsers();
            clearForm();
            txtName.requestFocus();

        } catch (MessageException ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Unexpected error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showValidationError(String message, JComponent field) {
        JOptionPane.showMessageDialog(this, message, "⚠️ Validation Error", JOptionPane.WARNING_MESSAGE);
        field.requestFocus();
        if (field instanceof JTextField) {
            ((JTextField) field).selectAll();
        }
    }

    private void updateUser() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to update", "⚠️ No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Update this user's information?", "✏️ Confirm Update",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int userId = Integer.parseInt(txtId.getText());
                UserRequest request = collectFormData();
                userService.update(userId, request);
                JOptionPane.showMessageDialog(this,
                        "✅ User updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadAllUsers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "❌ Error updating user: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteUser() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to delete", "⚠️ No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = Integer.parseInt(txtId.getText());

        // Check if trying to delete own account (you'd need to pass current user)
        // if (userId == currentUser.getId()) {
        //     JOptionPane.showMessageDialog(this, "❌ Cannot delete your own account!",
        //         "Error", JOptionPane.ERROR_MESSAGE);
        //     return;
        // }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this user?\n" +
                        "This action cannot be undone!\n\n" +
                        "👤 User: " + txtName.getText() + "\n" +
                        "📧 Email: " + txtEmail.getText(),
                "🗑️ Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userService.delete(userId);
                JOptionPane.showMessageDialog(this,
                        "✅ User deleted successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadAllUsers();
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "❌ Error deleting user: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshUsers() {
        loadAllUsers();
        clearForm();
        txtSearch.setText("");
        JOptionPane.showMessageDialog(this,
                "🔄 User list refreshed!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportUsers() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("💾 Export Users");
        fileChooser.setSelectedFile(new java.io.File("users_" +
                java.time.LocalDate.now() + ".csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // In a real app, you'd implement CSV export here
            JOptionPane.showMessageDialog(this,
                    "📊 Export feature coming soon!\n\n" +
                            "Would export " + tableModel.getRowCount() + " users.",
                    "Export", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private UserRequest collectFormData() {
        String gender = (String) txtGender.getSelectedItem();
        String status = (String) txtStatus.getSelectedItem();

        // Remove emojis if present
        if (gender != null && gender.contains(" ")) {
            gender = gender.substring(gender.indexOf(" ") + 1);
        }
        if (status != null && status.contains(" ")) {
            status = status.substring(status.indexOf(" ") + 1);
        }

        return new UserRequest(
                txtName.getText().trim(),
                gender != null ? gender : "",
                txtDob.getText().trim(),
                txtPhone.getText().trim(),
                txtAddress.getText().trim(),
                txtEmail.getText().trim(),
                new String(txtPassword.getPassword()),
                status != null ? status : "USER"
        );
    }

    private void loadAllUsers() {
        tableModel.setRowCount(0);
        try {
            List<UserResponse> users = userService.findAll();

            int adminCount = 0;
            int activeCount = 0;

            for (UserResponse user : users) {
                String genderEmoji = getGenderEmoji(user.getGender());
                String statusEmoji = getStatusEmoji(user.getStatus());

                tableModel.addRow(new Object[]{
                        user.getId(),
                        user.getName(),
                        genderEmoji + " " + user.getGender(),
                        formatDate(user.getDateOfBirth()),
                        formatPhone(user.getPhone()),
                        user.getAddress(),
                        user.getEmail(),
                        statusEmoji + " " + formatStatus(user.getStatus()),
                        user.getId() // Hidden column for reference
                });

                // Update counters
                if ("ADMIN".equalsIgnoreCase(user.getStatus())) {
                    adminCount++;
                }
                if (!"INACTIVE".equalsIgnoreCase(user.getStatus())) {
                    activeCount++;
                }
            }

            // Update statistics
            lblTotalUsers.setText("👥 Total: " + users.size());
            lblAdmins.setText("👑 Admins: " + adminCount);

            // Find and update active count label
            updateStatsPanel(activeCount);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Error loading users: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatsPanel(int activeCount) {
        // Find the active label and update it
        for (Component comp : getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                for (Component c : panel.getComponents()) {
                    if (c instanceof JLabel && ((JLabel) c).getText().startsWith("✅")) {
                        ((JLabel) c).setText("✅ Active: " + activeCount);
                        return;
                    }
                }
            }
        }
    }

    private String getGenderEmoji(String gender) {
        if (gender == null) return "⚤";
        gender = gender.toUpperCase();
        if (gender.contains("MALE")) return "👨";
        if (gender.contains("FEMALE")) return "👩";
        if (gender.contains("OTHER")) return "🌈";
        return "⚤";
    }

    private String getStatusEmoji(String status) {
        if (status == null) return "👤";
        status = status.toUpperCase();
        if (status.contains("ADMIN")) return "👑";
        if (status.contains("INACTIVE")) return "🚫";
        return "👤";
    }

    private String formatDate(String date) {
        if (date == null || date.trim().isEmpty()) return "🎂 N/A";
        return "🎂 " + date;
    }

    private String formatPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return "📞 N/A";
        // Format phone: (123) 456-7890
        phone = phone.replaceAll("[^\\d]", "");
        if (phone.length() == 10) {
            return String.format("📞 (%s) %s-%s",
                    phone.substring(0, 3),
                    phone.substring(3, 6),
                    phone.substring(6));
        }
        return "📞 " + phone;
    }

    private String formatStatus(String status) {
        if (status == null) return "INACTIVE";
        return status.toUpperCase();
    }

    private void fillFormFromSelectedRow() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = userTable.convertRowIndexToModel(selectedRow);

        txtId.setText(tableModel.getValueAt(modelRow, 0).toString());

        // Remove emoji from name if present
        String name = tableModel.getValueAt(modelRow, 1).toString();
        txtName.setText(name.replace("👤 ", ""));

        // Extract gender (remove emoji)
        String gender = tableModel.getValueAt(modelRow, 2).toString();
        if (gender.contains(" ")) {
            gender = gender.substring(gender.indexOf(" ") + 1);
        }
        txtGender.setSelectedItem(gender);

        // Extract date (remove emoji)
        String dob = tableModel.getValueAt(modelRow, 3).toString();
        txtDob.setText(dob.replace("🎂 ", "").replace("N/A", ""));

        // Extract phone (remove emoji and formatting)
        String phone = tableModel.getValueAt(modelRow, 4).toString();
        txtPhone.setText(phone.replace("📞 ", "").replace("N/A", "").replaceAll("[^\\d]", ""));

        txtAddress.setText(tableModel.getValueAt(modelRow, 5).toString());
        txtEmail.setText(tableModel.getValueAt(modelRow, 6).toString());

        // Extract status (remove emoji)
        String status = tableModel.getValueAt(modelRow, 7).toString();
        if (status.contains(" ")) {
            status = status.substring(status.indexOf(" ") + 1);
        }
        txtStatus.setSelectedItem(status);

        txtPassword.setText("");

        // Update selected info label
        lblSelectedInfo.setText("✅ Selected: " + name.replace("👤 ", ""));
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtGender.setSelectedIndex(0);
        txtDob.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
        txtStatus.setSelectedIndex(0);
        txtDob.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        lblSelectedInfo.setText("👆 Select a user");
        userTable.clearSelection();
        tableSorter.setRowFilter(null);
    }

    // UI Helper methods
    private JTextField createTextField(boolean editable, String placeholder) {
        JTextField field = new JTextField();
        field.setEditable(editable);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setEchoChar('•');
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