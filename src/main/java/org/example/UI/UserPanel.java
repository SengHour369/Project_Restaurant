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
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class UserPanel extends JPanel {
    private final ServiceUserImp userService = new ServiceUserImp();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Form fields (no emojis)
    private final JTextField txtId = createTextField(false, "");
    private final JTextField txtName = createTextField(true, "");
    private final JComboBox<String> txtGender = new JComboBox<>(new String[]{"", "Male", "Female", "Other"});
    private final JTextField txtDob = createTextField(true, "");
    private final JTextField txtPhone = createTextField(true, "");
    private final JTextField txtAddress = createTextField(true, "");
    private final JTextField txtEmail = createTextField(true, "");
    private final JPasswordField txtPassword = createPasswordField();
    private final JComboBox<String> txtStatus = new JComboBox<>(new String[]{"USER", "ADMIN", "INACTIVE"});

    // Search field
    private final JTextField txtSearch = new JTextField();

    // Table with plain headers
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Gender", "Date of Birth", "Phone", "Address", "Email", "Status", "Internal ID"}, 0
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
    private final JLabel lblTotalUsers = new JLabel("Total: 0");
    private final JLabel lblAdmins = new JLabel("Admins: 0");
    private final JLabel lblSelectedInfo = new JLabel("Select a user");

    // Buttons
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnRefresh;
    private JButton btnSearchBtn;

    private JSplitPane splitPane;
    private boolean isResizing = false;

    public UserPanel(UserResponse currentUser) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initializeUI();
        setupEventListeners();
        loadAllUsers();

        setupAutoFullScreen();
    }

    private void setupAutoFullScreen() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!isResizing) {
                    adjustComponentsForFullScreen();
                }
            }
        });
    }

    private void adjustComponentsForFullScreen() {
        int width = getWidth();
        int height = getHeight();

        if (width <= 100 || height <= 100) return;

        if (splitPane != null && splitPane.getWidth() > 100) {
            try {
                isResizing = true;
                double location = Math.max(0.3, Math.min(0.4, (double) width * 0.35 / width));
                splitPane.setDividerLocation(location);
            } finally {
                isResizing = false;
            }
        }
    }

    private void initializeUI() {
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.35);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(5);
        splitPane.setOneTouchExpandable(true);

        JPanel formPanel = createFormPanel();
        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(null);
        formScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        splitPane.setLeftComponent(formScrollPane);

        JPanel tablePanel = createTablePanel();
        splitPane.setRightComponent(tablePanel);

        add(splitPane, BorderLayout.CENTER);

        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            if (splitPane != null && splitPane.getWidth() > 100) {
                splitPane.setDividerLocation(0.35);
            }
        });
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#f8f9fa"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#3498db")),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        JLabel titleLabel = new JLabel("User Management System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(Color.decode("#2c3e50"));

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.decode("#f8f9fa"));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        searchLabel.setForeground(Color.decode("#2c3e50"));

        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#3498db"), 2),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        txtSearch.setPreferredSize(new Dimension(350, 45));
        txtSearch.setToolTipText("Search by name, email, phone, address, or status...");

        btnSearchBtn = createIconButton("Search", Color.decode("#3498db"), 16);
        btnSearchBtn.setToolTipText("Search");

        JButton btnClearSearch = createIconButton("Clear", Color.decode("#e74c3c"), 16);
        btnClearSearch.setToolTipText("Clear search");
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            txtSearch.requestFocus();
        });

        JPanel searchButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
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
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel formTitle = new JLabel("Add / Edit User");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        formTitle.setForeground(Color.decode("#2c3e50"));
        formTitle.setBounds(20, 20, 400, 40);
        panel.add(formTitle);

        int yPos = 80;
        int fieldWidth = 500;
        int labelWidth = 150;

        addFormField("User ID:", txtId, 20, yPos, labelWidth, fieldWidth, panel);
        yPos += 60;

        addFormField("Full Name:", txtName, 20, yPos, labelWidth, fieldWidth, panel);
        yPos += 60;

        addComboBoxField("Gender:", txtGender, 20, yPos, labelWidth, fieldWidth, panel);
        yPos += 60;

        addFormField("Date of Birth:", txtDob, 20, yPos, labelWidth, fieldWidth, panel);

        JButton btnDatePicker = createIconButton("Pick", Color.decode("#9b59b6"), 14);
        btnDatePicker.setBounds(20 + labelWidth + fieldWidth - 170, yPos, 60, 35);
        btnDatePicker.setToolTipText("Click for date picker");
        btnDatePicker.addActionListener(e -> showSimpleDatePickerDialog());
        panel.add(btnDatePicker);

        JLabel dobHint = new JLabel("(YYYY-MM-DD)");
        dobHint.setBounds(20 + labelWidth + fieldWidth - 110, yPos, 100, 35);
        dobHint.setFont(new Font("SansSerif", Font.ITALIC, 12));
        dobHint.setForeground(Color.GRAY);
        panel.add(dobHint);
        yPos += 60;

        addFormField("Phone Number:", txtPhone, 20, yPos, labelWidth, fieldWidth, panel);
        yPos += 60;

        addFormField("Address:", txtAddress, 20, yPos, labelWidth, fieldWidth, panel);
        yPos += 60;

        addFormField("Email:", txtEmail, 20, yPos, labelWidth, fieldWidth, panel);
        yPos += 60;

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setBounds(20, yPos, labelWidth, 30);
        lblPassword.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblPassword.setForeground(Color.decode("#2c3e50"));
        panel.add(lblPassword);

        txtPassword.setBounds(20 + labelWidth + 10, yPos, fieldWidth - labelWidth - 80, 35);
        txtPassword.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        panel.add(txtPassword);

        JButton btnShowPassword = createIconButton("Show", Color.decode("#7f8c8d"), 14);
        btnShowPassword.setBounds(20 + labelWidth + fieldWidth - 70, yPos, 60, 35);
        btnShowPassword.setToolTipText("Show/Hide Password");
        btnShowPassword.addActionListener(e -> togglePasswordVisibility(btnShowPassword));
        panel.add(btnShowPassword);
        yPos += 60;

        addComboBoxField("Status:", txtStatus, 20, yPos, labelWidth, fieldWidth, panel);
        yPos += 70;

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        buttonPanel.setBounds(20, yPos, fieldWidth, 110);
        buttonPanel.setBackground(Color.WHITE);

        btnAdd = createStyledButton("Add User", Color.decode("#2ecc71"));
        btnUpdate = createStyledButton("Update", Color.decode("#3498db"));
        btnDelete = createStyledButton("Delete", Color.decode("#e74c3c"));
        btnClear = createStyledButton("Clear", Color.decode("#95a5a6"));
        btnRefresh = createStyledButton("Refresh", Color.decode("#9b59b6"));

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(new JLabel());
        panel.add(buttonPanel);

        yPos += 120;

        JLabel tipsLabel = new JLabel("<html><div style='text-align: center; color: #7f8c8d; font-size: 12px;'>" +
                "Quick Tips: Double-click row to edit | Ctrl+F to search | F5 to refresh | Delete to remove</div></html>");
        tipsLabel.setBounds(20, yPos, fieldWidth, 40);
        panel.add(tipsLabel);

        panel.setPreferredSize(new Dimension(600, yPos + 50));

        return panel;
    }

    private void addComboBoxField(String label, JComboBox<String> comboBox, int x, int y, int labelWidth, int fieldWidth, JPanel panel) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(x, y, labelWidth, 30);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        lbl.setForeground(Color.decode("#2c3e50"));
        panel.add(lbl);

        comboBox.setBounds(x + labelWidth + 10, y, fieldWidth - labelWidth - 30, 35);
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 16));
        comboBox.setBackground(Color.WHITE);
        panel.add(comboBox);
    }

    private void showSimpleDatePickerDialog() {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();

        Integer[] years = new Integer[150];
        for (int i = 0; i < 150; i++) {
            years[i] = currentYear - i;
        }

        String[] months = {"01 - January", "02 - February", "03 - March", "04 - April",
                "05 - May", "06 - June", "07 - July", "08 - August",
                "09 - September", "10 - October", "11 - November", "12 - December"};

        JComboBox<Integer> yearCombo = new JComboBox<>(years);
        JComboBox<String> monthCombo = new JComboBox<>(months);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Year:"));
        panel.add(yearCombo);
        panel.add(new JLabel("Month:"));
        panel.add(monthCombo);
        panel.add(new JLabel("Day:"));

        JTextField dayField = new JTextField("01");
        dayField.setToolTipText("Enter day (1-31)");
        panel.add(dayField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Select Date", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int year = (int) yearCombo.getSelectedItem();
                String monthStr = (String) monthCombo.getSelectedItem();
                int month = Integer.parseInt(monthStr.substring(0, 2).trim());
                int day = Integer.parseInt(dayField.getText().trim());

                if (day < 1 || day > 31) {
                    JOptionPane.showMessageDialog(this, "Day must be between 1 and 31",
                            "Invalid Day", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String dateStr = String.format("%04d-%02d-%02d", year, month, day);
                txtDob.setText(dateStr);
                validateDateOfBirth();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void togglePasswordVisibility(JButton btnShowPassword) {
        if (txtPassword.getEchoChar() == '•') {
            txtPassword.setEchoChar((char) 0);
            btnShowPassword.setText("Hide");
        } else {
            txtPassword.setEchoChar('•');
            btnShowPassword.setText("Show");
        }
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 30));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);
        tableHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel tableTitle = new JLabel("All Users List");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        tableTitle.setForeground(Color.decode("#2c3e50"));

        lblSelectedInfo.setFont(new Font("SansSerif", Font.ITALIC, 14));
        lblSelectedInfo.setForeground(Color.decode("#7f8c8d"));

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(lblSelectedInfo, BorderLayout.EAST);

        userTable.setRowHeight(45);
        userTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        userTable.getTableHeader().setBackground(Color.decode("#34495e"));
        userTable.getTableHeader().setForeground(Color.WHITE);
        userTable.getTableHeader().setPreferredSize(new Dimension(0, 50));
        userTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        userTable.removeColumn(userTable.getColumnModel().getColumn(8));

        tableSorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(tableSorter);

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
                } else {
                    c.setBackground(new Color(52, 152, 219, 100));
                    c.setForeground(Color.BLACK);
                }

                if (column == 7) {
                    String status = value.toString();
                    if (status.contains("ADMIN")) {
                        setForeground(Color.decode("#e74c3c"));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (status.contains("INACTIVE")) {
                        setForeground(Color.decode("#95a5a6"));
                    } else {
                        setForeground(Color.decode("#27ae60"));
                    }
                }

                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                }

                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

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

        lblTotalUsers.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTotalUsers.setForeground(Color.decode("#2c3e50"));

        lblAdmins.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblAdmins.setForeground(Color.decode("#e74c3c"));

        JLabel lblActive = new JLabel("Active: 0");
        lblActive.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblActive.setForeground(Color.decode("#27ae60"));

        panel.add(lblTotalUsers);
        panel.add(lblAdmins);
        panel.add(lblActive);

        return panel;
    }

    private void setupEventListeners() {
        btnSearchBtn.addActionListener(e -> filterTable());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        btnAdd.addActionListener(e -> addUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> refreshUsers());

        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelectedRow();
            }
        });

        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = userTable.rowAtPoint(e.getPoint());
                    int column = userTable.columnAtPoint(e.getPoint());

                    if (row >= 0) {
                        fillFormFromSelectedRow();

                        if (column == 7) {
                            String status = tableModel.getValueAt(
                                    userTable.convertRowIndexToModel(row), 7).toString();
                            txtStatus.setSelectedItem(status);

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

        setupKeyboardShortcuts();

        txtDob.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateDateOfBirth();
            }
        });

        txtDob.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                formatDateInput();
            }
        });
    }

    private void formatDateInput() {
        String text = txtDob.getText();
        if (text.length() == 4 || text.length() == 7) {
            txtDob.setText(text + "-");
        }
    }

    private void setupKeyboardShortcuts() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("control F"), "focusSearch");
        getActionMap().put("focusSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtSearch.requestFocus();
                txtSearch.selectAll();
            }
        });

        userTable.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("DELETE"), "deleteUser");
        userTable.getActionMap().put("deleteUser", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteUser();
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F5"), "refresh");
        getActionMap().put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshUsers();
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("control N"), "newUser");
        getActionMap().put("newUser", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
                txtName.requestFocus();
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ESCAPE"), "clearForm");
        getActionMap().put("clearForm", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });

        setupEnterKeyNavigation();
    }

    private void setupEnterKeyNavigation() {
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
            for (int i = 0; i < tableModel.getColumnCount() - 1; i++) {
                if (i != 0) {
                    filters.add(javax.swing.RowFilter.regexFilter("(?i)" + searchText, i));
                }
            }
            javax.swing.RowFilter<Object, Object> orFilter = javax.swing.RowFilter.orFilter(filters);
            tableSorter.setRowFilter(orFilter);
        }
    }

    private void validateDateOfBirth() {
        String dobText = txtDob.getText().trim();

        if (dobText.isEmpty()) {
            txtDob.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            return;
        }

        try {
            LocalDate dob = LocalDate.parse(dobText, dateFormatter);
            LocalDate today = LocalDate.now();

            if (dob.isAfter(today)) {
                txtDob.setBorder(BorderFactory.createLineBorder(Color.decode("#e74c3c"), 2));
                JOptionPane.showMessageDialog(this,
                        "Date of birth cannot be in the future!",
                        "Date Error", JOptionPane.WARNING_MESSAGE);
                txtDob.requestFocus();
            } else if (dob.getYear() < 1900) {
                txtDob.setBorder(BorderFactory.createLineBorder(Color.decode("#e74c3c"), 2));
                JOptionPane.showMessageDialog(this,
                        "Date of birth must be after 1900!",
                        "Date Error", JOptionPane.WARNING_MESSAGE);
                txtDob.requestFocus();
            } else {
                int age = today.getYear() - dob.getYear();
                if (today.getMonthValue() < dob.getMonthValue() ||
                        (today.getMonthValue() == dob.getMonthValue() && today.getDayOfMonth() < dob.getDayOfMonth())) {
                    age--;
                }

                if (age < 0) {
                    txtDob.setBorder(BorderFactory.createLineBorder(Color.decode("#e74c3c"), 2));
                    JOptionPane.showMessageDialog(this,
                            "Invalid date! Age cannot be negative.",
                            "Date Error", JOptionPane.WARNING_MESSAGE);
                    txtDob.requestFocus();
                } else if (age < 13) {
                    txtDob.setBorder(BorderFactory.createLineBorder(Color.decode("#f39c12"), 2));
                    JOptionPane.showMessageDialog(this,
                            "User is under 13 years old (" + age + " years). Are you sure?",
                            "Age Warning", JOptionPane.WARNING_MESSAGE);
                } else if (age > 150) {
                    txtDob.setBorder(BorderFactory.createLineBorder(Color.decode("#f39c12"), 2));
                    JOptionPane.showMessageDialog(this,
                            "User is over 150 years old (" + age + " years). Please verify.",
                            "Age Warning", JOptionPane.WARNING_MESSAGE);
                } else {
                    txtDob.setBorder(BorderFactory.createLineBorder(Color.decode("#27ae60"), 2));
                }
            }
        } catch (DateTimeParseException e) {
            txtDob.setBorder(BorderFactory.createLineBorder(Color.decode("#e74c3c"), 2));
            String message;
            if (!dobText.matches("\\d{4}-\\d{2}-\\d{2}")) {
                message = "Invalid date format!\nPlease use: YYYY-MM-DD\nExample: 1990-01-15";
            } else if (dobText.length() != 10) {
                message = "Date must be exactly 10 characters!\nFormat: YYYY-MM-DD";
            } else {
                message = "Invalid date!\nPlease check:\n- Year: 1900-2024\n- Month: 01-12\n- Day: 01-31";
            }
            JOptionPane.showMessageDialog(this,
                    message,
                    "Date Error", JOptionPane.WARNING_MESSAGE);
            txtDob.requestFocus();
        }
    }

    private void addUser() {
        try {
            if (txtName.getText().trim().isEmpty()) {
                showValidationError("Please enter user name", txtName);
                return;
            }
            if (txtEmail.getText().trim().isEmpty()) {
                showValidationError("Please enter email", txtEmail);
                return;
            }
            if (txtPassword.getPassword().length == 0) {
                showValidationError("Please enter password", txtPassword);
                return;
            }

            String email = txtEmail.getText().trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showValidationError("Invalid email format. Example: user@example.com", txtEmail);
                return;
            }

            if (!txtDob.getText().trim().isEmpty()) {
                try {
                    LocalDate.parse(txtDob.getText().trim(), dateFormatter);
                } catch (DateTimeParseException e) {
                    showValidationError("Invalid date format. Please use: YYYY-MM-DD", txtDob);
                    return;
                }
            }

            String phone = txtPhone.getText().trim();
            if (!phone.isEmpty() && !phone.matches("\\d{9,15}")) {
                showValidationError("Phone number must be 9-15 digits", txtPhone);
                return;
            }

            UserRequest request = collectFormData();
            userService.create(request);

            JOptionPane.showMessageDialog(this,
                    "<html><div style='font-size:14pt;'>User added successfully!<br><br>" +
                            "Name: " + request.getName() + "<br>" +
                            "Email: " + request.getEmail() + "<br>" +
                            "Status: " + request.getStatus() + "</div></html>",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            loadAllUsers();
            clearForm();
            txtName.requestFocus();

        } catch (MessageException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Unexpected error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showValidationError(String message, JComponent field) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
        field.requestFocus();
        if (field instanceof JTextField) {
            ((JTextField) field).selectAll();
        }
    }

    private void updateUser() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to update", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><div style='font-size:12pt;'>Update this user's information?</div></html>",
                "Confirm Update",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int userId = Integer.parseInt(txtId.getText());
                UserRequest request = collectFormData();
                userService.update(userId, request);
                JOptionPane.showMessageDialog(this,
                        "User updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadAllUsers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error updating user: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteUser() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to delete", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = Integer.parseInt(txtId.getText());

        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><div style='font-size:12pt;'>Are you sure you want to delete this user?<br>" +
                        "This action cannot be undone!<br><br>" +
                        "User: " + txtName.getText() + "<br>" +
                        "Email: " + txtEmail.getText() + "</div></html>",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userService.delete(userId);
                JOptionPane.showMessageDialog(this,
                        "User deleted successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadAllUsers();
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting user: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshUsers() {
        loadAllUsers();
        clearForm();
        txtSearch.setText("");
        JOptionPane.showMessageDialog(this,
                "User list refreshed!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private UserRequest collectFormData() {
        String gender = (String) txtGender.getSelectedItem();
        String status = (String) txtStatus.getSelectedItem();

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
                tableModel.addRow(new Object[]{
                        user.getId(),
                        user.getName(),
                        user.getGender() != null ? user.getGender() : "",
                        user.getDateOfBirth() != null ? user.getDateOfBirth() : "N/A",
                        user.getPhone() != null ? user.getPhone() : "N/A",
                        user.getAddress(),
                        user.getEmail(),
                        user.getStatus() != null ? user.getStatus().toUpperCase() : "INACTIVE",
                        user.getId()
                });

                if ("ADMIN".equalsIgnoreCase(user.getStatus())) {
                    adminCount++;
                }
                if (!"INACTIVE".equalsIgnoreCase(user.getStatus())) {
                    activeCount++;
                }
            }

            lblTotalUsers.setText("Total: " + users.size());
            lblAdmins.setText("Admins: " + adminCount);

            for (Component comp : getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    for (Component c : panel.getComponents()) {
                        if (c instanceof JLabel) {
                            JLabel label = (JLabel) c;
                            if (label.getText().startsWith("Active:")) {
                                label.setText("Active: " + activeCount);
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading users: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillFormFromSelectedRow() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = userTable.convertRowIndexToModel(selectedRow);

        txtId.setText(tableModel.getValueAt(modelRow, 0).toString());

        txtName.setText(tableModel.getValueAt(modelRow, 1).toString());

        String gender = tableModel.getValueAt(modelRow, 2).toString();
        txtGender.setSelectedItem(gender);

        String dob = tableModel.getValueAt(modelRow, 3).toString();
        txtDob.setText(dob.equals("N/A") ? "" : dob);

        String phone = tableModel.getValueAt(modelRow, 4).toString();
        txtPhone.setText(phone.equals("N/A") ? "" : phone.replaceAll("[^\\d]", ""));

        txtAddress.setText(tableModel.getValueAt(modelRow, 5).toString());
        txtEmail.setText(tableModel.getValueAt(modelRow, 6).toString());

        String status = tableModel.getValueAt(modelRow, 7).toString();
        txtStatus.setSelectedItem(status);

        txtPassword.setText("");

        lblSelectedInfo.setText("Selected: " + tableModel.getValueAt(modelRow, 1).toString());
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
        lblSelectedInfo.setText("Select a user");
        userTable.clearSelection();
        tableSorter.setRowFilter(null);
    }

    // UI Helper methods
    private JTextField createTextField(boolean editable, String placeholder) {
        JTextField field = new JTextField();
        field.setEditable(editable);
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        field.setEchoChar('•');
        return field;
    }

    private void addFormField(String label, JTextField field, int x, int y, int labelWidth, int fieldWidth, JPanel panel) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(x, y, labelWidth, 30);
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
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
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

    private JButton createIconButton(String text, Color color, int fontSize) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
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