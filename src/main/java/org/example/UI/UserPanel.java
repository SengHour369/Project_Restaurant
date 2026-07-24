package org.example.UI;

import org.example.DTO.Request.UserRequest;
import org.example.DTO.Response.UserResponse;
import org.example.Exception.MessageException;
import org.example.Service.ServiceExcel;
import org.example.Service.ServiceImplement.ServiceExcelImp;
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
    private final ServiceExcel excelService = new ServiceExcelImp();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String USERS_EXCEL_PATH = "exports/users_export.xlsx";
    private static final String USERS_EXCEL_SHEET = "Users";
    private static final List<String> USERS_EXCEL_HEADERS = List.of(
            "ID", "Name", "Gender", "Date of Birth", "Phone", "Address", "Email", "Status"
    );

    // Form fields (no emojis)
    private final JTextField txtId = createTextField(false, "");
    private final JTextField txtName = createTextField(true, "");
    private final JComboBox<String> txtGender = new JComboBox<>(new String[]{"", "Male", "Female", "Other"});
    private final JTextField txtDob = createTextField(true, "");
    private final JTextField txtPhone = createTextField(true, "");
    private final JTextField txtAddress = createTextField(true, "");
    private final JTextField txtEmail = createTextField(true, "");
    private final JPasswordField txtPassword = createPasswordField();
    private final JComboBox<String> txtStatus = new JComboBox<>(new String[]{"Active", "Admin", "Inactive", "Suspended"});
    private final JLabel lblImagePreview = new JLabel("No Image", SwingConstants.CENTER);

    // Path of the image staged/stored for the user currently in the form
    private String selectedImagePath;
    // User ID -> stored profile image path, refreshed on every load
    private final java.util.Map<Integer, String> imagePathById = new java.util.HashMap<>();

    // Search field
    private final JTextField txtSearch = new JTextField();

    // Table with plain headers
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"", "ID", "Name", "Gender", "Date of Birth", "Phone", "Address", "Email", "Status", "Internal ID"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) return Icon.class;
            if (column == 1 || column == 9) return Integer.class;
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
    private JButton btnChooseImage;
    private JButton btnExportExcel;

    private JSplitPane splitPane;
    private boolean isResizing = false;

    public UserPanel(UserResponse currentUser) {
        setLayout(new BorderLayout());
        setBackground(Color.decode("#F5F6F8"));

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
        panel.setBackground(Color.decode("#F5F6F8"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#4F46E5")),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        JLabel titleLabel = new JLabel("👤 User Management System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(Color.decode("#111827"));

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.decode("#F5F6F8"));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        searchLabel.setForeground(Color.decode("#111827"));

        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txtSearch.setBorder(new UITheme.RoundedLineBorder(UITheme.PRIMARY, 16, 12, 15));
        txtSearch.setPreferredSize(new Dimension(350, 45));
        txtSearch.setToolTipText("Search by name, email, phone, address, or status...");

        btnSearchBtn = createIconButton("Search", Color.decode("#4F46E5"), 16);
        btnSearchBtn.setToolTipText("Search");

        JButton btnClearSearch = createIconButton("Clear", Color.decode("#EF4444"), 16);
        btnClearSearch.setToolTipText("Clear search");
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            txtSearch.requestFocus();
        });

        JPanel searchButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchButtons.setBackground(Color.decode("#F5F6F8"));
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
        panel.setBackground(Color.decode("#F5F6F8"));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel formTitle = new JLabel("✏️ Add / Edit User");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        formTitle.setForeground(Color.decode("#111827"));
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

        JButton btnDatePicker = createIconButton("Pick", Color.decode("#4F46E5"), 14);
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
        lblPassword.setForeground(Color.decode("#111827"));
        panel.add(lblPassword);

        txtPassword.setBounds(20 + labelWidth + 10, yPos, fieldWidth - labelWidth - 80, 35);
        txtPassword.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txtPassword.setBorder(new UITheme.RoundedLineBorder(UITheme.NEUTRAL, 14, 8, 15));
        panel.add(txtPassword);

        JButton btnShowPassword = createIconButton("Show", Color.decode("#6B7280"), 14);
        btnShowPassword.setBounds(20 + labelWidth + fieldWidth - 70, yPos, 60, 35);
        btnShowPassword.setToolTipText("Show/Hide Password");
        btnShowPassword.addActionListener(e -> togglePasswordVisibility(btnShowPassword));
        panel.add(btnShowPassword);
        yPos += 60;

        addComboBoxField("Status:", txtStatus, 20, yPos, labelWidth, fieldWidth, panel);
        yPos += 60;

        // Profile image field
        JLabel lblImage = new JLabel("Profile Image:");
        lblImage.setBounds(20, yPos, labelWidth, 90);
        lblImage.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblImage.setForeground(Color.decode("#111827"));
        panel.add(lblImage);

        lblImagePreview.setBounds(20 + labelWidth + 10, yPos, 100, 90);
        lblImagePreview.setOpaque(true);
        lblImagePreview.setBackground(Color.WHITE);
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Color.decode("#64748B")));
        lblImagePreview.setFont(new Font("SansSerif", Font.PLAIN, 11));
        panel.add(lblImagePreview);

        btnChooseImage = createStyledButton("Choose Image", Color.decode("#4F46E5"));
        btnChooseImage.setBounds(20 + labelWidth + 10 + 115, yPos + 25, 170, 40);
        panel.add(btnChooseImage);
        yPos += 105;

        JPanel buttonPanel = new JPanel(new GridLayout(3, 3, 15, 15));
        buttonPanel.setBounds(20, yPos, fieldWidth, 165);
        buttonPanel.setBackground(Color.decode("#F5F6F8"));

        btnAdd = createStyledButton("Add User", Color.decode("#16A34A"));
        btnUpdate = createStyledButton("Update", Color.decode("#4F46E5"));
        btnDelete = createStyledButton("Delete", Color.decode("#EF4444"));
        btnClear = createStyledButton("Clear", Color.decode("#64748B"));
        btnRefresh = createStyledButton("Refresh", Color.decode("#4F46E5"));
        JButton btnViewDetails = createStyledButton("View Details", UITheme.TEXT_DARK);
        btnViewDetails.addActionListener(e -> showSelectedDetails());
        btnExportExcel = createStyledButton("Export Excel", Color.decode("#0EA5E9"));

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnViewDetails);
        buttonPanel.add(btnExportExcel);
        panel.add(buttonPanel);

        yPos += 175;

        JLabel tipsLabel = new JLabel("<html><div style='text-align: center; color: #6B7280; font-size: 12px;'>" +
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
        lbl.setForeground(Color.decode("#111827"));
        panel.add(lbl);

        comboBox.setBounds(x + labelWidth + 10, y, fieldWidth - labelWidth - 30, 35);
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 16));
        comboBox.setBackground(Color.decode("#F5F6F8"));
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
        panel.setBackground(Color.decode("#F5F6F8"));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 30));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.decode("#F5F6F8"));
        tableHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel tableTitle = new JLabel("📋 All Users List");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        tableTitle.setForeground(Color.decode("#111827"));

        lblSelectedInfo.setFont(new Font("SansSerif", Font.ITALIC, 14));
        lblSelectedInfo.setForeground(Color.decode("#6B7280"));

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(lblSelectedInfo, BorderLayout.EAST);

        userTable.setRowHeight(45);
        userTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        userTable.getTableHeader().setBackground(Color.decode("#4F46E5"));
        userTable.getTableHeader().setForeground(Color.WHITE);
        userTable.getTableHeader().setPreferredSize(new Dimension(0, 50));
        userTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Thumbnail column: small, fixed width, no header text
        userTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        userTable.getColumnModel().getColumn(0).setMaxWidth(55);
        userTable.getColumnModel().getColumn(0).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
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

        userTable.removeColumn(userTable.getColumnModel().getColumn(9));

        tableSorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(tableSorter);

        userTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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

                if (column == 8) {
                    String status = value.toString();
                    if (status.contains("ADMIN")) {
                        setForeground(Color.decode("#EF4444"));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (status.contains("INACTIVE") || status.contains("SUSPENDED")) {
                        setForeground(Color.decode("#64748B"));
                    } else {
                        setForeground(Color.decode("#16A34A"));
                    }
                }

                if (column == 1) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                }

                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        scrollPane.getViewport().setBackground(Color.decode("#F5F6F8"));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

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

        lblTotalUsers.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTotalUsers.setForeground(Color.decode("#111827"));

        lblAdmins.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblAdmins.setForeground(Color.decode("#EF4444"));

        JLabel lblActive = new JLabel("Active: 0");
        lblActive.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblActive.setForeground(Color.decode("#16A34A"));

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
        btnExportExcel.addActionListener(e -> exportUsersToExcel());
        btnChooseImage.addActionListener(e -> chooseImage());

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

                        if (column == 8) {
                            String status = tableModel.getValueAt(
                                    userTable.convertRowIndexToModel(row), 8).toString();
                            txtStatus.setSelectedItem(status);

                            txtStatus.setBackground(new Color(224, 231, 255));
                            Timer timer = new Timer(1000, evt -> {
                                txtStatus.setBackground(Color.decode("#F5F6F8"));
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
                if (i != 0 && i != 1) {
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
            txtDob.setBorder(new UITheme.RoundedLineBorder(Color.LIGHT_GRAY, 14, 8, 15));
            return;
        }

        try {
            LocalDate dob = LocalDate.parse(dobText, dateFormatter);
            LocalDate today = LocalDate.now();

            if (dob.isAfter(today)) {
                txtDob.setBorder(new UITheme.RoundedLineBorder(Color.decode("#EF4444"), 14, 8, 15));
                JOptionPane.showMessageDialog(this,
                        "Date of birth cannot be in the future!",
                        "Date Error", JOptionPane.WARNING_MESSAGE);
                txtDob.requestFocus();
            } else if (dob.getYear() < 1900) {
                txtDob.setBorder(new UITheme.RoundedLineBorder(Color.decode("#EF4444"), 14, 8, 15));
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
                    txtDob.setBorder(new UITheme.RoundedLineBorder(Color.decode("#EF4444"), 14, 8, 15));
                    JOptionPane.showMessageDialog(this,
                            "Invalid date! Age cannot be negative.",
                            "Date Error", JOptionPane.WARNING_MESSAGE);
                    txtDob.requestFocus();
                } else if (age < 13) {
                    txtDob.setBorder(new UITheme.RoundedLineBorder(Color.decode("#F59E0B"), 14, 8, 15));
                    JOptionPane.showMessageDialog(this,
                            "User is under 13 years old (" + age + " years). Are you sure?",
                            "Age Warning", JOptionPane.WARNING_MESSAGE);
                } else if (age > 150) {
                    txtDob.setBorder(new UITheme.RoundedLineBorder(Color.decode("#F59E0B"), 14, 8, 15));
                    JOptionPane.showMessageDialog(this,
                            "User is over 150 years old (" + age + " years). Please verify.",
                            "Age Warning", JOptionPane.WARNING_MESSAGE);
                } else {
                    txtDob.setBorder(new UITheme.RoundedLineBorder(Color.decode("#16A34A"), 14, 8, 15));
                }
            }
        } catch (DateTimeParseException e) {
            txtDob.setBorder(new UITheme.RoundedLineBorder(Color.decode("#EF4444"), 14, 8, 15));
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

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Profile Image");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File selected = chooser.getSelectedFile();
        try {
            java.io.File imagesDir = new java.io.File("images/users");
            imagesDir.mkdirs();

            String ext = "";
            int dot = selected.getName().lastIndexOf('.');
            if (dot >= 0) ext = selected.getName().substring(dot);

            java.io.File dest = new java.io.File(imagesDir, "user_" + System.currentTimeMillis() + ext);
            java.nio.file.Files.copy(selected.toPath(), dest.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            selectedImagePath = "images/users/" + dest.getName();
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
                .getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
        lblImagePreview.setText("");
        lblImagePreview.setIcon(new ImageIcon(scaled));
    }

    /** Small 32x32 table thumbnail: the user's profile photo if set, else a lettered placeholder. */
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

    /** Shows the selected user's full details, including their profile photo, in a popup. */
    private void showSelectedDetails() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user first!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        String details = String.format(
                "<html><div style='font-size:12pt;'>" +
                        "<b>User Details</b><br><br>" +
                        "ID: %s<br>" +
                        "Name: %s<br>" +
                        "Gender: %s<br>" +
                        "Date of Birth: %s<br>" +
                        "Phone: %s<br>" +
                        "Address: %s<br>" +
                        "Email: %s<br>" +
                        "Status: %s" +
                        "</div></html>",
                tableModel.getValueAt(modelRow, 1),
                tableModel.getValueAt(modelRow, 2),
                tableModel.getValueAt(modelRow, 3),
                tableModel.getValueAt(modelRow, 4),
                tableModel.getValueAt(modelRow, 5),
                tableModel.getValueAt(modelRow, 6),
                tableModel.getValueAt(modelRow, 7),
                tableModel.getValueAt(modelRow, 8)
        );

        JPanel detailPanel = new JPanel(new BorderLayout(0, 10));
        detailPanel.setOpaque(false);
        detailPanel.add(new JLabel(details), BorderLayout.CENTER);

        String imgPath = imagePathById.get((Integer) tableModel.getValueAt(modelRow, 1));
        if (imgPath != null && !imgPath.isBlank()) {
            java.io.File imgFile = new java.io.File(imgPath);
            if (imgFile.exists()) {
                Image scaled = new ImageIcon(imgFile.getAbsolutePath())
                        .getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                detailPanel.add(new JLabel(new ImageIcon(scaled)), BorderLayout.NORTH);
            }
        }

        JOptionPane.showMessageDialog(this, detailPanel,
                "User Details", JOptionPane.INFORMATION_MESSAGE);
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
                status != null ? status : "Active",
                selectedImagePath
        );
    }

    // Exports the full user list to exports/users_export.xlsx via the generic ServiceExcel.
    private void exportUsersToExcel() {
        try {
            List<UserResponse> users = userService.findAll();
            List<List<Object>> rows = new ArrayList<>();
            for (UserResponse user : users) {
                rows.add(List.of(
                        user.getId(),
                        user.getName() != null ? user.getName() : "",
                        user.getGender() != null ? user.getGender() : "",
                        user.getDateOfBirth() != null ? user.getDateOfBirth() : "",
                        user.getPhone() != null ? user.getPhone() : "",
                        user.getAddress() != null ? user.getAddress() : "",
                        user.getEmail() != null ? user.getEmail() : "",
                        user.getStatus() != null ? user.getStatus() : ""
                ));
            }

            java.io.File file = excelService.writeToExcel(USERS_EXCEL_PATH, USERS_EXCEL_SHEET, USERS_EXCEL_HEADERS, rows);
            JOptionPane.showMessageDialog(this,
                    "Exported " + users.size() + " user(s) to:\n" + file.getPath(),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (MessageException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to export users: " + ex.getMessage(),
                    "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllUsers() {
        tableModel.setRowCount(0);
        imagePathById.clear();
        try {
            List<UserResponse> users = userService.findAll();

            int adminCount = 0;
            int activeCount = 0;

            for (UserResponse user : users) {
                imagePathById.put(user.getId(), user.getImage_path());
                tableModel.addRow(new Object[]{
                        rowThumbnail(user.getImage_path(), user.getName()),
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

        txtId.setText(tableModel.getValueAt(modelRow, 1).toString());

        txtName.setText(tableModel.getValueAt(modelRow, 2).toString());

        String gender = tableModel.getValueAt(modelRow, 3).toString();
        txtGender.setSelectedItem(gender);

        String dob = tableModel.getValueAt(modelRow, 4).toString();
        txtDob.setText(dob.equals("N/A") ? "" : dob);

        String phone = tableModel.getValueAt(modelRow, 5).toString();
        txtPhone.setText(phone.equals("N/A") ? "" : phone.replaceAll("[^\\d]", ""));

        txtAddress.setText(tableModel.getValueAt(modelRow, 6).toString());
        txtEmail.setText(tableModel.getValueAt(modelRow, 7).toString());

        String status = tableModel.getValueAt(modelRow, 8).toString();
        txtStatus.setSelectedItem(status);

        txtPassword.setText("");

        selectedImagePath = imagePathById.get((Integer) tableModel.getValueAt(modelRow, 1));
        showImagePreview(selectedImagePath);

        lblSelectedInfo.setText("Selected: " + tableModel.getValueAt(modelRow, 2).toString());
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
        selectedImagePath = null;
        showImagePreview(null);
        txtDob.setBorder(new UITheme.RoundedLineBorder(Color.LIGHT_GRAY, 14, 8, 15));
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
        lbl.setForeground(Color.decode("#111827"));
        panel.add(lbl);

        field.setBounds(x + labelWidth + 10, y, fieldWidth - labelWidth - 30, 35);
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        field.setBorder(new UITheme.RoundedLineBorder(UITheme.NEUTRAL, 14, 8, 15));
        panel.add(field);
    }

    private JButton createStyledButton(String text, Color color) {
        return UITheme.createButton(text, color);
    }

    private JButton createIconButton(String text, Color color, int fontSize) {
        return UITheme.createButton(text, color);
    }
}