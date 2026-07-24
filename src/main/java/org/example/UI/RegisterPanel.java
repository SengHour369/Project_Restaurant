package org.example.UI;

import org.example.DTO.Request.UserRequest;
import org.example.Exception.MessageException;
import org.example.Service.ServiceImplement.ServiceUserImp;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * A dedicated "Create account" screen, styled like {@link LoginFrame} (gradient
 * brand panel + clean white form), instead of reusing the admin's full user
 * management table inside a dialog.
 */
public class RegisterPanel extends JPanel {
    private final ServiceUserImp userService = new ServiceUserImp();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JTextField txtName = new JTextField();
    private final JComboBox<String> txtGender = new JComboBox<>(new String[]{"", "Male", "Female", "Other"});
    private final JTextField txtDob = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtAddress = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JPasswordField txtConfirmPassword = new JPasswordField();
    private JLabel lblError;
    private JButton btnRegister;

    /** Called after a successful registration, or when the user asks to go back to login. */
    private final Runnable onDone;

    public RegisterPanel(Runnable onDone) {
        this.onDone = onDone;
        setLayout(new GridLayout(1, 2));
        setBackground(UITheme.CARD);
        add(createBrandPanel());
        add(createFormPanel());
    }

    /** Left: gradient brand panel, matching the login screen's hero side. */
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

        JLabel logo = new JLabel("🎉");
        logo.setFont(new Font("SansSerif", Font.PLAIN, 56));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Join us today");
        title.setFont(UITheme.FONT_H1);
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(16, 0, 8, 0));

        JLabel subtitle = new JLabel("<html><div style='width:320px;'>"
                + "Create an account to order from your favourite restaurants, "
                + "track your orders and manage your profile.</div></html>");
        subtitle.setFont(UITheme.baseFont(15f));
        subtitle.setForeground(new Color(255, 255, 255, 220));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        stack.add(logo);
        stack.add(title);
        stack.add(subtitle);

        brand.add(stack, new GridBagConstraints());
        return brand;
    }

    private JPanel createFormPanel() {
        JPanel wrap = new JPanel();
        wrap.setBackground(UITheme.CARD);
        wrap.setLayout(new BorderLayout());
        wrap.setBorder(BorderFactory.createEmptyBorder(32, 48, 24, 48));

        JScrollPane scroll = new JScrollPane(buildForm());
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.CARD);
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Create your account");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.TEXT_DARK);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("It only takes a minute");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 20, 0));

        form.add(heading);
        form.add(sub);

        JPanel row1 = twoUp(field("Full Name", txtName), comboField("Gender", txtGender));
        form.add(row1);
        form.add(Box.createVerticalStrut(14));

        JPanel row2 = twoUp(field("Date of Birth (YYYY-MM-DD)", txtDob), field("Phone Number", txtPhone));
        form.add(row2);
        form.add(Box.createVerticalStrut(14));

        form.add(field("Address", txtAddress));
        form.add(Box.createVerticalStrut(14));
        form.add(field("Email", txtEmail));
        form.add(Box.createVerticalStrut(14));

        JPanel row3 = twoUp(field("Password", txtPassword), field("Confirm Password", txtConfirmPassword));
        form.add(row3);

        lblError = new JLabel(" ");
        lblError.setFont(UITheme.FONT_MUTED);
        lblError.setForeground(UITheme.SECONDARY);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblError.setBorder(BorderFactory.createEmptyBorder(14, 0, 8, 0));
        form.add(lblError);

        btnRegister = UITheme.createButton("Create account", UITheme.PRIMARY);
        btnRegister.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnRegister.addActionListener(e -> register());
        form.add(btnRegister);
        form.add(Box.createVerticalStrut(12));

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        backRow.setOpaque(false);
        backRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        backRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel already = new JLabel("Already have an account?");
        already.setFont(UITheme.FONT_BODY);
        already.setForeground(UITheme.TEXT_MUTED);
        JButton btnBack = UITheme.createSoftButton("Back to sign in", UITheme.PRIMARY);
        btnBack.addActionListener(e -> { if (onDone != null) onDone.run(); });
        backRow.add(already);
        backRow.add(btnBack);
        form.add(backRow);

        return form;
    }

    private JPanel twoUp(JPanel left, JPanel right) {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        row.add(left);
        row.add(right);
        return row;
    }

    private JPanel field(String label, JComponent input) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        lbl.setForeground(UITheme.TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));

        input.setFont(UITheme.baseFont(14.5f));
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setPreferredSize(new Dimension(10, 40));
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        if (!(input instanceof JComboBox)) {
            input.setBorder(new UITheme.RoundedLineBorder(UITheme.BORDER, 12, 8, 12));
        }

        group.add(lbl);
        group.add(input);
        return group;
    }

    private JPanel comboField(String label, JComboBox<String> combo) {
        combo.setBackground(Color.WHITE);
        return field(label, combo);
    }

    private void register() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());
        String dob = txtDob.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty()) { showError("Please enter your full name.", txtName); return; }
        if (email.isEmpty()) { showError("Please enter your email.", txtEmail); return; }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) { showError("Invalid email format. Example: user@example.com", txtEmail); return; }
        if (password.isEmpty()) { showError("Please enter a password.", txtPassword); return; }
        if (!password.equals(confirm)) { showError("Passwords do not match.", txtConfirmPassword); return; }
        if (!dob.isEmpty()) {
            try {
                LocalDate.parse(dob, dateFormatter);
            } catch (DateTimeParseException ex) {
                showError("Invalid date of birth. Please use YYYY-MM-DD.", txtDob);
                return;
            }
        }
        if (!phone.isEmpty() && !phone.matches("\\d{9,15}")) { showError("Phone number must be 9-15 digits.", txtPhone); return; }

        String gender = (String) txtGender.getSelectedItem();

        UserRequest request = new UserRequest(
                name,
                gender != null ? gender : "",
                dob,
                phone,
                txtAddress.getText().trim(),
                email,
                password,
                "Active",
                null
        );

        btnRegister.setEnabled(false);
        btnRegister.setText("Creating account…");
        try {
            userService.create(request);
            JOptionPane.showMessageDialog(this,
                    "<html><div style='font-size:13pt;'>🎉 Account created! You can now sign in.</div></html>",
                    "Welcome", JOptionPane.INFORMATION_MESSAGE);
            if (onDone != null) onDone.run();
        } catch (MessageException ex) {
            lblError.setText(ex.getMessage());
        } catch (Exception ex) {
            lblError.setText("Could not create account: " + ex.getMessage());
        } finally {
            btnRegister.setEnabled(true);
            btnRegister.setText("Create account");
        }
    }

    private void showError(String message, JComponent field) {
        lblError.setText(message);
        field.requestFocus();
        if (field instanceof JTextField) {
            ((JTextField) field).selectAll();
        }
    }
}