package org.example.UI;

import org.example.DTO.Response.UserResponse;
import org.example.Service.ServiceImplement.ServiceUserImp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class LoginFrame extends JFrame {
    private final JTextField txtEmail = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final ServiceUserImp userService = new ServiceUserImp();
    private JButton btnLogin;
    private JButton btnRegister;
    private JLabel lblError;

    public LoginFrame() {
        initializeUI();
        setupEventListeners();
    }

    private void initializeUI() {
        setTitle("🍽️ Food Order System - Login");
        setSize(450, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = Color.decode("#667eea");
                Color color2 = Color.decode("#764ba2");
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setOpaque(false);
        contentPanel.setBounds(50, 50, 350, 250);

        // Title
        JLabel titleLabel = new JLabel("🍽️ Welcome Back!");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 0, 350, 40);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(titleLabel);

        // Email field
        JLabel lblEmail = new JLabel("📧 Email:");
        lblEmail.setForeground(Color.WHITE);
        lblEmail.setBounds(30, 60, 100, 25);
        contentPanel.add(lblEmail);

        txtEmail.setBounds(130, 60, 180, 30);
        txtEmail.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        contentPanel.add(txtEmail);

        // Password field
        JLabel lblPassword = new JLabel("🔒 Password:");
        lblPassword.setForeground(Color.WHITE);
        lblPassword.setBounds(30, 100, 100, 25);
        contentPanel.add(lblPassword);

        txtPassword.setBounds(130, 100, 180, 30);
        txtPassword.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        contentPanel.add(txtPassword);

        // Buttons
        btnLogin = createStyledButton("🚀 Login", Color.decode("#4CAF50"));
        btnRegister = createStyledButton("📝 Register", Color.decode("#2196F3"));

        btnLogin.setBounds(50, 150, 120, 35);
        btnRegister.setBounds(180, 150, 120, 35);

        contentPanel.add(btnLogin);
        contentPanel.add(btnRegister);

        // Error message label
        lblError = new JLabel("");
        lblError.setForeground(Color.YELLOW);
        lblError.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblError.setBounds(30, 200, 300, 25);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(lblError);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Focus on email field when window opens
        SwingUtilities.invokeLater(() -> txtEmail.requestFocus());
    }

    private void setupEventListeners() {
        // Login button action
        btnLogin.addActionListener(e -> performLogin());

        // Register button action
        btnRegister.addActionListener(e -> openRegister());

        // Enter key in password field
        txtPassword.addActionListener(e -> performLogin());

        // Enter key in email field
        txtEmail.addActionListener(e -> txtPassword.requestFocus());

        // Keyboard shortcuts
        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        // Ctrl+L to focus login
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), "focusLogin");
        getRootPane().getActionMap().put("focusLogin", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtEmail.requestFocus();
            }
        });

        // Ctrl+R to register
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "openRegister");
        getRootPane().getActionMap().put("openRegister", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openRegister();
            }
        });

        // Escape to clear fields
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearFields");
        getRootPane().getActionMap().put("clearFields", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearFields();
            }
        });

        // F1 for help
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "showHelp");
        getRootPane().getActionMap().put("showHelp", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showHelp();
            }
        });
    }

    private void performLogin() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        // Validate inputs
        if (email.isEmpty() && password.isEmpty()) {
            lblError.setText("⚠️ Please enter email and password");
            txtEmail.requestFocus();
            return;
        } else if (email.isEmpty()) {
            lblError.setText("⚠️ Please enter email");
            txtEmail.requestFocus();
            return;
        } else if (password.isEmpty()) {
            lblError.setText("⚠️ Please enter password");
            txtPassword.requestFocus();
            return;
        }

        // Email format validation
        if (!isValidEmail(email)) {
            lblError.setText("⚠️ Invalid email format");
            txtEmail.selectAll();
            txtEmail.requestFocus();
            return;
        }

        // Clear previous error
        lblError.setText("");

        // Show loading state
        btnLogin.setText("⏳ Logging in...");
        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false);

        // Simulate async login (in real app, use SwingWorker)
        Timer timer = new Timer(1500, e -> {
            try {
                UserResponse user = userService.login(email, password);

                if (user != null) {
                    // Login successful
                    lblError.setText("");

                    // Show success message
                    JOptionPane.showMessageDialog(this,
                            String.format("🎉 Welcome back, %s!", user.getName()),
                            "Login Successful", JOptionPane.INFORMATION_MESSAGE);

                    // Open appropriate dashboard
                    SwingUtilities.invokeLater(() -> {
                        if ("ADMIN".equalsIgnoreCase(user.getStatus())) {
                            new AdminDashboard(user).setVisible(true);
                        } else {
                            new CustomerApp(user).setVisible(true);
                        }
                        dispose();
                    });
                } else {
                    // Login failed
                    lblError.setText("❌ Invalid email or password");
                    txtPassword.setText("");
                    txtPassword.requestFocus();

                    // Restore buttons
                    btnLogin.setText("🚀 Login");
                    btnLogin.setEnabled(true);
                    btnRegister.setEnabled(true);
                }
            } catch (Exception ex) {
                // Handle service errors
                lblError.setText("❌ System error: " + ex.getMessage());
                System.err.println("Login error: " + ex.getMessage());

                // Restore buttons
                btnLogin.setText("🚀 Login");
                btnLogin.setEnabled(true);
                btnRegister.setEnabled(true);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void openRegister() {
        // Create a dialog for registration
        JDialog registerDialog = new JDialog(this, "📝 Create New Account", true);
        registerDialog.setSize(650, 550);
        registerDialog.setLocationRelativeTo(this);
        registerDialog.setLayout(new BorderLayout());

        // Add UserPanel to dialog
        UserPanel userPanel = new UserPanel(null);
        registerDialog.add(userPanel, BorderLayout.CENTER);

        // Add close button at bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnClose = createStyledButton("✖️ Close", Color.decode("#95a5a6"));
        btnClose.addActionListener(e -> registerDialog.dispose());

        bottomPanel.add(btnClose);
        registerDialog.add(bottomPanel, BorderLayout.SOUTH);

        registerDialog.setVisible(true);
    }

    private void clearFields() {
        txtEmail.setText("");
        txtPassword.setText("");
        lblError.setText("");
        txtEmail.requestFocus();
    }

    private void showHelp() {
        String helpMessage = """
            🆘 Login Help
            
            📧 Email: Enter your registered email address
            🔒 Password: Enter your password
            
            💡 Tips:
            • Press Enter in password field to login
            • Press Esc to clear all fields
            • Ctrl+R to open registration
            • Ctrl+L to focus on email field
            
            🔐 Forgot Password?
            Contact administrator to reset your password
            
            📞 Support: support@foodorder.com
            """;

        JOptionPane.showMessageDialog(this, helpMessage,
                "❓ Login Help", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
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

    public static void main(String[] args) {
        // For testing
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}