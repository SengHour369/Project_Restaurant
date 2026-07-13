package org.example.UI;

import org.example.DTO.Response.UserResponse;
import org.example.Service.ServiceImplement.ServiceUserImp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class LoginFrame extends JFrame {
    private final JTextField txtusername = new JTextField();
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
        setTitle("Food Order System - Login");
        setSize(450, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

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

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setOpaque(false);
        contentPanel.setBounds(50, 50, 350, 250);

        JLabel titleLabel = new JLabel("Welcome Back!");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 0, 350, 40);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(titleLabel);

        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setForeground(Color.WHITE);
        lblUsername.setBounds(30, 60, 100, 25);
        contentPanel.add(lblUsername);

        txtusername.setBounds(130, 60, 180, 30);
        txtusername.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtusername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        contentPanel.add(txtusername);

        JLabel lblPassword = new JLabel("Password:");
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

        btnLogin = createStyledButton("Login", Color.decode("#4CAF50"));
        btnRegister = createStyledButton("Register", Color.decode("#2196F3"));

        btnLogin.setBounds(50, 150, 120, 35);
        btnRegister.setBounds(180, 150, 120, 35);
        contentPanel.add(btnLogin);
        contentPanel.add(btnRegister);

        lblError = new JLabel("");
        lblError.setForeground(Color.YELLOW);
        lblError.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblError.setBounds(30, 200, 300, 25);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(lblError);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
        SwingUtilities.invokeLater(txtusername::requestFocus);
    }

    private void setupEventListeners() {
        btnLogin.addActionListener(e -> performLogin());
        btnRegister.addActionListener(e -> openRegister());
        txtPassword.addActionListener(e -> performLogin());
        txtusername.addActionListener(e -> txtPassword.requestFocus());

        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), "focusLogin");
        getRootPane().getActionMap().put("focusLogin", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtusername.requestFocus();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "openRegister");
        getRootPane().getActionMap().put("openRegister", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openRegister();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearFields");
        getRootPane().getActionMap().put("clearFields", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearFields();
            }
        });

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
        String username = txtusername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() && password.isEmpty()) {
            lblError.setText("Please enter username and password");
            txtusername.requestFocus();
            return;
        } else if (username.isEmpty()) {
            lblError.setText("Please enter username");
            txtusername.requestFocus();
            return;
        } else if (password.isEmpty()) {
            lblError.setText("Please enter password");
            txtPassword.requestFocus();
            return;
        }

        lblError.setText("");
        btnLogin.setText("Logging in...");
        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false);

        Timer timer = new Timer(1500, e -> {
            try {
                UserResponse user = userService.login(username, password);
                if (user != null) {
                    lblError.setText("");
                    JOptionPane.showMessageDialog(this,
                            String.format("Welcome back, %s!", user.getName()),
                            "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                    SwingUtilities.invokeLater(() -> {
                        if ("ADMIN".equalsIgnoreCase(user.getStatus())) {
                            new AdminDashboard(user).setVisible(true);
                        } else {
                            new CustomerApp(user).setVisible(true);
                        }
                        dispose();
                    });
                } else {
                    lblError.setText("Invalid username or password");
                    txtPassword.setText("");
                    txtPassword.requestFocus();
                    btnLogin.setText("Login");
                    btnLogin.setEnabled(true);
                    btnRegister.setEnabled(true);
                }
            } catch (Exception ex) {
                lblError.setText("System error: " + ex.getMessage());
                btnLogin.setText("Login");
                btnLogin.setEnabled(true);
                btnRegister.setEnabled(true);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void openRegister() {
        JDialog registerDialog = new JDialog(this, "Create New Account", true);
        registerDialog.setSize(650, 550);
        registerDialog.setLocationRelativeTo(this);
        registerDialog.setLayout(new BorderLayout());

        UserPanel userPanel = new UserPanel(null);
        registerDialog.add(userPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnClose = createStyledButton("Close", Color.decode("#95a5a6"));
        btnClose.addActionListener(e -> registerDialog.dispose());

        bottomPanel.add(btnClose);
        registerDialog.add(bottomPanel, BorderLayout.SOUTH);
        registerDialog.setVisible(true);
    }

    private void clearFields() {
        txtusername.setText("");
        txtPassword.setText("");
        lblError.setText("");
        txtusername.requestFocus();
    }

    private void showHelp() {
        String helpMessage = """
            Login Help

            Email: Enter your registered email address
            Password: Enter your password

            Tips:
            - Press Enter in password field to login
            - Press Esc to clear all fields
            - Ctrl+R to open registration
            - Ctrl+L to focus on email field

            Forgot Password?
            Contact administrator to reset your password

            Support: support@foodorder.com
            """;
        JOptionPane.showMessageDialog(this, helpMessage,
                "Login Help", JOptionPane.INFORMATION_MESSAGE);
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
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}