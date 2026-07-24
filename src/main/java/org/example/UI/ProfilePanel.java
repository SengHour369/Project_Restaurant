package org.example.UI;

import org.example.DTO.Request.UserRequest;
import org.example.DTO.Response.UserResponse;
import org.example.Service.ServiceImplement.ServiceUserImp;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * A dedicated "My Profile" screen for a single logged-in customer.
 *
 * Unlike {@link UserPanel} (the admin's full user-management table), this shows
 * and edits only the current user's own details, with a circular profile photo.
 */
public class ProfilePanel extends JPanel {
    private final ServiceUserImp userService = new ServiceUserImp();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private UserResponse currentUser;

    // Form fields
    private final JTextField txtName = new JTextField();
    private final JComboBox<String> txtGender = new JComboBox<>(new String[]{"", "Male", "Female", "Other"});
    private final JTextField txtDob = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtAddress = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();

    private final AvatarLabel avatar = new AvatarLabel();
    private final JLabel lblNameHeader = new JLabel();
    private final UITheme.PillLabel lblStatusBadge = new UITheme.PillLabel("", UITheme.PRIMARY_DARK, Color.WHITE);

    // Path of the image staged/stored for this user
    private String selectedImagePath;

    public ProfilePanel(UserResponse user) {
        this.currentUser = user;
        setBackground(UITheme.BACKGROUND);
        initializeUI();
        populateFromUser();
    }

    private void initializeUI() {
        // Fixed WIDTH only — height is computed live from the actual header/form/
        // footer content (via the getPreferredSize() override below) so nothing
        // is ever silently clipped as fields are added.
        JPanel card = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                Dimension natural = super.getPreferredSize();
                return new Dimension(860, natural.height);
            }
        };
        card.setLayout(new BorderLayout(0, 0));
        card.setBackground(UITheme.CARD);
        card.setBorder(new UITheme.CardBorder(20));

        card.add(createHeader(), BorderLayout.NORTH);
        card.add(createForm(), BorderLayout.CENTER);
        card.add(createFooter(), BorderLayout.SOUTH);

        // Center the card horizontally; vertical overflow scrolls instead of clipping.
        JPanel centerer = new JPanel(new GridBagLayout());
        centerer.setBackground(UITheme.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(25, 25, 25, 25);
        centerer.add(card, gbc);

        JScrollPane scroll = new JScrollPane(centerer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.BACKGROUND);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
    }

    /** A gradient hero header (matches the login screen's brand panel) with the avatar, name and status. */
    private JPanel createHeader() {
        JPanel header = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, UITheme.PRIMARY,
                        getWidth(), getHeight(), UITheme.blend(UITheme.PRIMARY, Color.BLACK, 0.35f));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fillOval(getWidth() - 90, -50, 200, 200);
                g2.fillOval(-60, getHeight() - 70, 150, 150);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.WEST;
        header.add(avatar, gbc);

        JButton btnChangePhoto = UITheme.createOutlineButton("📷  Change Photo");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridheight = 1;
        gbc.insets = new Insets(12, 0, 0, 0);
        btnChangePhoto.addActionListener(e -> chooseImage());
        header.add(btnChangePhoto, gbc);

        lblNameHeader.setFont(UITheme.FONT_H1);
        lblNameHeader.setForeground(Color.WHITE);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(0, 25, 0, 0);
        header.add(lblNameHeader, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 25, 0, 0);
        header.add(lblStatusBadge, gbc);

        return header;
    }

    /** A small uppercase section label used to separate form groups ("Personal Information", "Security"). */
    private JPanel sectionTitle(String text) {
        JPanel wrap = new JPanel(new BorderLayout(10, 0));
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 12));
        lbl.setForeground(UITheme.PRIMARY);

        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);

        wrap.add(lbl, BorderLayout.WEST);
        wrap.add(sep, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.CARD);
        form.setBorder(BorderFactory.createEmptyBorder(24, 40, 4, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(9, 8, 9, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 8, 10, 8);
        form.add(sectionTitle("Personal Information"), gbc);
        gbc.insets = new Insets(9, 8, 9, 8);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(fieldGroup("Full Name", txtName), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        form.add(fieldGroup("Gender", txtGender), gbc);

        gbc.gridx = 0; gbc.gridy = row;
        form.add(fieldGroup("Date of Birth (YYYY-MM-DD)", txtDob), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        form.add(fieldGroup("Phone Number", txtPhone), gbc);

        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        form.add(fieldGroup("Address", txtAddress), gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.insets = new Insets(18, 8, 10, 8);
        form.add(sectionTitle("Account & Security"), gbc);
        gbc.insets = new Insets(9, 8, 9, 8);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(fieldGroup("Email", txtEmail), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        form.add(fieldGroup("New Password", txtPassword), gbc);
        txtPassword.setEchoChar('•');
        txtPassword.setToolTipText("Leave blank to keep your current password");

        JLabel hint = new JLabel("Leave the password blank to keep your current one.");
        hint.setFont(UITheme.FONT_MUTED);
        hint.setForeground(UITheme.TEXT_MUTED);
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 8, 10, 8);
        form.add(hint, gbc);

        return form;
    }

    private JPanel fieldGroup(String label, JComponent field) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        lbl.setForeground(UITheme.TEXT_DARK);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));

        field.setFont(UITheme.baseFont(14.5f));
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(10, 40));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        if (field instanceof JComboBox) {
            ((JComboBox<?>) field).setBackground(Color.WHITE);
        } else {
            field.setBorder(new UITheme.RoundedLineBorder(UITheme.BORDER, 12, 8, 12));
        }

        group.add(lbl);
        group.add(field);
        return group;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UITheme.CARD);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(16, 40, 20, 40)));

        JButton btnReset = UITheme.createSoftButton("Reset", UITheme.NEUTRAL);
        btnReset.addActionListener(e -> populateFromUser());

        JButton btnSave = UITheme.createButton("Save Changes", UITheme.PRIMARY);
        btnSave.addActionListener(e -> saveProfile());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(btnReset);
        right.add(btnSave);

        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private void populateFromUser() {
        txtName.setText(nullToEmpty(currentUser.getName()));
        txtGender.setSelectedItem(nullToEmpty(currentUser.getGender()));
        txtDob.setText("N/A".equals(currentUser.getDateOfBirth()) ? "" : nullToEmpty(currentUser.getDateOfBirth()));
        txtPhone.setText(nullToEmpty(currentUser.getPhone()));
        txtAddress.setText(nullToEmpty(currentUser.getAddress()));
        txtEmail.setText(nullToEmpty(currentUser.getEmail()));
        txtPassword.setText("");

        selectedImagePath = currentUser.getImage_path();
        avatar.setImagePath(selectedImagePath);

        lblNameHeader.setText(nullToEmpty(currentUser.getName()));
        String status = currentUser.getStatus() == null ? "Active" : currentUser.getStatus();
        lblStatusBadge.setText(status.toUpperCase());
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
            avatar.setImagePath(selectedImagePath);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load image: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveProfile() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty()) {
            showError("Please enter your name.", txtName);
            return;
        }
        if (email.isEmpty()) {
            showError("Please enter your email.", txtEmail);
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Invalid email format. Example: user@example.com", txtEmail);
            return;
        }

        String dob = txtDob.getText().trim();
        if (!dob.isEmpty()) {
            try {
                LocalDate.parse(dob, dateFormatter);
            } catch (DateTimeParseException ex) {
                showError("Invalid date of birth. Please use YYYY-MM-DD.", txtDob);
                return;
            }
        }

        String phone = txtPhone.getText().trim();
        if (!phone.isEmpty() && !phone.matches("\\d{9,15}")) {
            showError("Phone number must be 9-15 digits.", txtPhone);
            return;
        }

        // Preserve the existing password when the user leaves the field blank
        String password = new String(txtPassword.getPassword());
        if (password.isEmpty()) {
            String existing = userService.getPasswordHash(currentUser.getId());
            password = existing != null ? existing : "";
        }

        String gender = (String) txtGender.getSelectedItem();
        // Status is not editable by the customer — keep whatever they already have
        String status = currentUser.getStatus() != null ? currentUser.getStatus() : "Active";

        UserRequest request = new UserRequest(
                name,
                gender != null ? gender : "",
                dob,
                phone,
                txtAddress.getText().trim(),
                email,
                password,
                status,
                selectedImagePath
        );

        try {
            userService.update(currentUser.getId(), request);

            // Refresh our in-memory user so the header/badge stay in sync
            UserResponse refreshed = userService.findById(currentUser.getId());
            if (refreshed != null) {
                this.currentUser = refreshed;
            }
            populateFromUser();

            JOptionPane.showMessageDialog(this,
                    "<html><div style='font-size:13pt;'>✅ Your profile has been updated!</div></html>",
                    "Profile Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save profile: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showError(String message, JComponent field) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
        field.requestFocus();
        if (field instanceof JTextField) {
            ((JTextField) field).selectAll();
        }
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /** A round profile picture that falls back to the user's initial when no image is set. */
    private class AvatarLabel extends JComponent {
        private static final int SIZE = 96;
        private Image image;

        AvatarLabel() {
            setPreferredSize(new Dimension(SIZE, SIZE));
        }

        void setImagePath(String path) {
            image = null;
            if (path != null && !path.isBlank()) {
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    image = new ImageIcon(file.getAbsolutePath()).getImage();
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Ellipse2D circle = new Ellipse2D.Float(1, 1, SIZE - 2, SIZE - 2);

            if (image != null) {
                g2.setClip(circle);
                g2.drawImage(image, 1, 1, SIZE - 2, SIZE - 2, null);
                g2.setClip(null);
            } else {
                g2.setColor(Color.WHITE);
                g2.fill(circle);
                g2.setColor(UITheme.PRIMARY);
                String initial = "?";
                String name = txtName.getText();
                if (name != null && !name.isBlank()) {
                    initial = name.trim().substring(0, 1).toUpperCase();
                }
                g2.setFont(new Font("SansSerif", Font.BOLD, 46));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(initial);
                int th = fm.getAscent();
                g2.drawString(initial, (SIZE - tw) / 2, (SIZE + th) / 2 - 6);
            }

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3f));
            g2.draw(circle);
            g2.dispose();
        }
    }
}