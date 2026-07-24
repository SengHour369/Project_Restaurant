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
        setTitle("Food Order System — Sign in");
        setSize(920, 600);
        setMinimumSize(new Dimension(760, 520));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(UITheme.CARD);

        root.add(createBrandPanel());
        root.add(createFormPanel());

        setContentPane(root);
        SwingUtilities.invokeLater(txtusername::requestFocus);
    }

    /** Left: a colorful gradient brand panel — the "cool" hero side of a web login. */
    private JPanel createBrandPanel() {
        JPanel brand = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, UITheme.PRIMARY,
                        getWidth(), getHeight(), UITheme.blend(UITheme.PRIMARY, Color.BLACK, 0.35f));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // subtle decorative circles
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fillOval(getWidth() - 120, -60, 260, 260);
                g2.fillOval(-80, getHeight() - 160, 240, 240);
            }
        };
        brand.setLayout(new GridBagLayout());
        brand.setBorder(BorderFactory.createEmptyBorder(40, 48, 40, 48));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("🍽️");
        logo.setFont(new Font("SansSerif", Font.PLAIN, 56));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Food Order System");
        title.setFont(UITheme.FONT_H1);
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(16, 0, 8, 0));

        JLabel subtitle = new JLabel("<html><div style='width:320px;'>"
                + "Order from your favourite restaurants and manage everything "
                + "from one clean, simple dashboard.</div></html>");
        subtitle.setFont(UITheme.baseFont(15f));
        subtitle.setForeground(new Color(255, 255, 255, 220));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        stack.add(logo);
        stack.add(title);
        stack.add(subtitle);

        brand.add(stack, new GridBagConstraints());
        return brand;
    }

    /** Right: the actual sign-in form on a clean white surface. */
    private JPanel createFormPanel() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(UITheme.CARD);
        wrap.setBorder(BorderFactory.createEmptyBorder(40, 56, 40, 56));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Welcome back 👋");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.TEXT_DARK);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to continue");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 24, 0));

        form.add(heading);
        form.add(sub);

        form.add(fieldLabel("Email"));
        txtusername.putClientProperty("JTextField.placeholderText", "you@example.com");
        styleInput(txtusername);
        form.add(txtusername);
        form.add(Box.createVerticalStrut(16));

        form.add(fieldLabel("Password"));
        txtPassword.putClientProperty("JTextField.placeholderText", "••••••••");
        styleInput(txtPassword);
        form.add(txtPassword);

        lblError = new JLabel(" ");
        lblError.setFont(UITheme.FONT_MUTED);
        lblError.setForeground(UITheme.SECONDARY);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblError.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        form.add(lblError);

        btnLogin = UITheme.createButton("Sign in", UITheme.PRIMARY);
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        form.add(btnLogin);
        form.add(Box.createVerticalStrut(12));

        JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        registerRow.setOpaque(false);
        registerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel noAccount = new JLabel("New here?");
        noAccount.setFont(UITheme.FONT_BODY);
        noAccount.setForeground(UITheme.TEXT_MUTED);
        btnRegister = UITheme.createSoftButton("Create an account", UITheme.PRIMARY);
        registerRow.add(noAccount);
        registerRow.add(btnRegister);
        form.add(registerRow);

        wrap.add(form, new GridBagConstraints());
        return wrap;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        l.setForeground(UITheme.TEXT_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        return l;
    }

    private void styleInput(JComponent field) {
        field.setFont(UITheme.baseFont(15f));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setPreferredSize(new Dimension(360, 44));
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
    }

    private void performLogin() {
        String username = txtusername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() && password.isEmpty()) {
            lblError.setText("Please enter your email and password");
            txtusername.requestFocus();
            return;
        } else if (username.isEmpty()) {
            lblError.setText("Please enter your email");
            txtusername.requestFocus();
            return;
        } else if (password.isEmpty()) {
            lblError.setText("Please enter your password");
            txtPassword.requestFocus();
            return;
        }

        lblError.setText(" ");
        btnLogin.setText("Signing in…");
        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false);

        Timer timer = new Timer(400, e -> {
            try {
                UserResponse user = userService.login(username, password);
                if (user != null) {
                    lblError.setText(" ");
                    SwingUtilities.invokeLater(() -> {
                        if ("ADMIN".equalsIgnoreCase(user.getStatus())) {
                            new AdminDashboard(user).setVisible(true);
                        } else {
                            new CustomerApp(user).setVisible(true);
                        }
                        dispose();
                    });
                } else {
                    lblError.setText("Invalid email or password");
                    txtPassword.setText("");
                    txtPassword.requestFocus();
                    btnLogin.setText("Sign in");
                    btnLogin.setEnabled(true);
                    btnRegister.setEnabled(true);
                }
            } catch (Exception ex) {
                lblError.setText("System error: " + ex.getMessage());
                btnLogin.setText("Sign in");
                btnLogin.setEnabled(true);
                btnRegister.setEnabled(true);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void openRegister() {
        JDialog registerDialog = new JDialog(this, "Create Account", true);
        registerDialog.setSize(920, 640);
        registerDialog.setMinimumSize(new Dimension(760, 560));
        registerDialog.setLocationRelativeTo(this);
        registerDialog.setLayout(new BorderLayout());

        RegisterPanel registerPanel = new RegisterPanel(registerDialog::dispose);
        registerDialog.add(registerPanel, BorderLayout.CENTER);
        registerDialog.setVisible(true);
    }

    private void clearFields() {
        txtusername.setText("");
        txtPassword.setText("");
        lblError.setText(" ");
        txtusername.requestFocus();
    }
}