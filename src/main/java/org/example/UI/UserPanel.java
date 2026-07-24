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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * "Members" screen: a searchable, filterable, paginated member directory styled
 * after the modern Members reference layout — avatar+name rows, role/status
 * pills, inline edit/delete actions, and an Add/Edit Member dialog.
 */
public class UserPanel extends JPanel {
    private final ServiceUserImp userService = new ServiceUserImp();
    private final ServiceExcel excelService = new ServiceExcelImp();
    private final DateTimeFormatter dobFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String USERS_EXCEL_PATH = "exports/users_export.xlsx";
    private static final String USERS_EXCEL_SHEET = "Users";
    private static final List<String> USERS_EXCEL_HEADERS = List.of(
            "ID", "Name", "Gender", "Date of Birth", "Phone", "Address", "Email", "Status"
    );

    private List<UserResponse> allUsers = new ArrayList<>();
    private List<UserResponse> filtered = new ArrayList<>();
    private final Map<Integer, String> imagePathById = new HashMap<>();

    private int currentPage = 0;
    private int pageSize = 10;

    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> statusFilter =
            new JComboBox<>(new String[]{"All Status", "Active", "Admin", "Inactive", "Suspended"});
    private final JComboBox<Integer> pageSizeCombo = new JComboBox<>(new Integer[]{10, 20, 50, 100});
    private final JLabel lblResults = new JLabel();
    private JButton btnPrev;
    private JButton btnNext;

    // Column 7 ("id") is hidden from the view but kept in the model so row
    // renderers/actions can look up the real user behind each visible row.
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"", "Name", "Role", "Email", "ID Number", "Status", "Action", "id"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0 || column == 6;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) return Boolean.class;
            if (column == 7) return Integer.class;
            return String.class;
        }
    };
    private final JTable table = new JTable(tableModel);

    public UserPanel(UserResponse currentUser) {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(buildToolbar(), BorderLayout.NORTH);
        body.add(buildTableCard(), BorderLayout.CENTER);
        body.add(buildFooter(), BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);

        wireFilterEvents();
        loadAllUsers();
    }

    // ---------- Header / toolbar / footer ----------

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 2, 20, 2));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Members");
        title.setFont(UITheme.FONT_H1);
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Manage your team members and their account access");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        titles.add(title);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);

        JButton btnAdd = UITheme.createButton("+  Add New Members", UITheme.SUCCESS);
        btnAdd.addActionListener(e -> openMemberDialog(null));
        header.add(btnAdd, BorderLayout.EAST);

        return header;
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel searchBox = new JPanel(new BorderLayout(8, 0));
        searchBox.setBackground(Color.WHITE);
        searchBox.setBorder(new UITheme.RoundedLineBorder(UITheme.BORDER, 12, 9, 14));
        searchBox.setPreferredSize(new Dimension(280, 42));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setForeground(UITheme.TEXT_MUTED);

        txtSearch.setBorder(BorderFactory.createEmptyBorder());
        txtSearch.setOpaque(false);
        txtSearch.setFont(UITheme.FONT_BODY);
        txtSearch.setToolTipText("Search by name, email, phone, or address...");

        searchBox.add(searchIcon, BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);

        UITheme.styleField(statusFilter);
        statusFilter.setPreferredSize(new Dimension(140, 42));

        JButton btnExport = UITheme.createSoftButton("Export Excel", UITheme.BRAND_BLUE);
        btnExport.addActionListener(e -> exportUsersToExcel());

        bar.add(searchBox);
        bar.add(statusFilter);
        bar.add(btnExport);
        return bar;
    }

    private JPanel buildTableCard() {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(card.getBorder(),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        configureTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(14, 6, 0, 6));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel showLbl = new JLabel("Show");
        showLbl.setFont(UITheme.FONT_MUTED);
        showLbl.setForeground(UITheme.TEXT_MUTED);

        pageSizeCombo.setFont(UITheme.FONT_BODY);
        pageSizeCombo.setSelectedItem(pageSize);

        JLabel rowLbl = new JLabel("row");
        rowLbl.setFont(UITheme.FONT_MUTED);
        rowLbl.setForeground(UITheme.TEXT_MUTED);

        lblResults.setFont(UITheme.FONT_MUTED);
        lblResults.setForeground(UITheme.TEXT_MUTED);
        lblResults.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        left.add(showLbl);
        left.add(pageSizeCombo);
        left.add(rowLbl);
        left.add(lblResults);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        btnPrev = UITheme.createSoftButton("Previous", UITheme.NEUTRAL);
        btnNext = UITheme.createButton("Next", UITheme.PRIMARY);
        right.add(btnPrev);
        right.add(btnNext);

        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private void wireFilterEvents() {
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { currentPage = 0; applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { currentPage = 0; applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { currentPage = 0; applyFilters(); }
        });
        statusFilter.addActionListener(e -> { currentPage = 0; applyFilters(); });
        pageSizeCombo.addActionListener(e -> {
            pageSize = (Integer) pageSizeCombo.getSelectedItem();
            currentPage = 0;
            renderPage();
        });
        btnPrev.addActionListener(e -> { currentPage--; renderPage(); });
        btnNext.addActionListener(e -> { currentPage++; renderPage(); });
    }

    // ---------- Table configuration ----------

    private void configureTable() {
        table.setRowHeight(58);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(UITheme.SELECTION);
        table.setFont(UITheme.FONT_BODY);

        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                lbl.setText(value == null ? "" : value.toString().toUpperCase());
                lbl.setFont(new Font(UITheme.FONT_MUTED.getFamily(), Font.BOLD, 11));
                lbl.setForeground(UITheme.TEXT_MUTED);
                lbl.setBackground(Color.WHITE);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)));
                return lbl;
            }
        });

        table.setDefaultRenderer(Object.class, (tbl, value, isSelected, hasFocus, row, column) -> {
            JLabel lbl = new JLabel(value == null ? "" : value.toString());
            lbl.setFont(UITheme.FONT_BODY);
            lbl.setForeground(UITheme.TEXT_DARK);
            styleRow(lbl, row, isSelected);
            return lbl;
        });

        table.getColumnModel().getColumn(1).setCellRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
            int userId = (Integer) tableModel.getValueAt(row, 7);
            String name = value == null ? "" : value.toString();
            JLabel lbl = new JLabel(name);
            lbl.setIcon(circleAvatarIcon(imagePathById.get(userId), name, 32));
            lbl.setIconTextGap(10);
            lbl.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 13));
            lbl.setForeground(UITheme.TEXT_DARK);
            styleRow(lbl, row, isSelected);
            return lbl;
        });

        table.getColumnModel().getColumn(5).setCellRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            wrap.add(statusPill(value == null ? "" : value.toString()));
            styleRow(wrap, row, isSelected);
            return wrap;
        });

        table.getColumnModel().getColumn(6).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionCellEditor());

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setMinWidth(40);
        cm.getColumn(0).setMaxWidth(40);
        cm.getColumn(1).setPreferredWidth(200);
        cm.getColumn(2).setPreferredWidth(110);
        cm.getColumn(3).setPreferredWidth(220);
        cm.getColumn(4).setPreferredWidth(100);
        cm.getColumn(5).setPreferredWidth(110);
        cm.getColumn(6).setPreferredWidth(90);

        table.removeColumn(cm.getColumn(7));
    }

    private void styleRow(JComponent c, int row, boolean isSelected) {
        c.setOpaque(true);
        c.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        c.setBackground(isSelected ? UITheme.SELECTION : (row % 2 == 0 ? Color.WHITE : UITheme.ROW_ALT));
    }

    private class ActionCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JPanel panel = actionButtons(row);
            styleRow(panel, row, isSelected);
            return panel;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object value, boolean isSelected, int row, int column) {
            JPanel panel = actionButtons(row);
            panel.setOpaque(true);
            panel.setBackground(row % 2 == 0 ? Color.WHITE : UITheme.ROW_ALT);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    private JPanel actionButtons(int row) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);

        JButton editBtn = iconActionButton("✎", UITheme.PRIMARY);
        JButton deleteBtn = iconActionButton("🗑", UITheme.SECONDARY);

        editBtn.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            int id = (Integer) tableModel.getValueAt(row, 7);
            UserResponse u = findUserById(id);
            if (u != null) openMemberDialog(u);
        }));
        deleteBtn.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            int id = (Integer) tableModel.getValueAt(row, 7);
            String name = String.valueOf(tableModel.getValueAt(row, 1));
            deleteMember(id, name);
        }));

        panel.add(editBtn);
        panel.add(deleteBtn);
        return panel;
    }

    private JButton iconActionButton(String glyph, Color color) {
        JButton btn = new JButton(glyph) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                        ? UITheme.blend(color, Color.WHITE, 0.78f)
                        : UITheme.blend(color, Color.WHITE, 0.88f));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setPreferredSize(new Dimension(30, 30));
        btn.setForeground(color.darker());
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ---------- Data loading / filtering / pagination ----------

    private void loadAllUsers() {
        try {
            allUsers = new ArrayList<>(userService.findAll());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading members: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            allUsers = new ArrayList<>();
        }
        imagePathById.clear();
        for (UserResponse u : allUsers) {
            imagePathById.put(u.getId(), u.getImage_path());
        }
        applyFilters();
    }

    private void applyFilters() {
        String q = txtSearch.getText().trim().toLowerCase();
        String statusSel = (String) statusFilter.getSelectedItem();

        filtered = allUsers.stream()
                .filter(u -> statusSel == null || "All Status".equals(statusSel) || statusSel.equalsIgnoreCase(u.getStatus()))
                .filter(u -> q.isEmpty() || matchesSearch(u, q))
                .collect(Collectors.toList());

        renderPage();
    }

    private boolean matchesSearch(UserResponse u, String q) {
        return contains(u.getName(), q) || contains(u.getEmail(), q)
                || contains(u.getPhone(), q) || contains(u.getAddress(), q);
    }

    private boolean contains(String s, String q) {
        return s != null && s.toLowerCase().contains(q);
    }

    private void renderPage() {
        int total = filtered.size();
        int maxPage = total == 0 ? 0 : (int) Math.ceil(total / (double) pageSize) - 1;
        if (currentPage > maxPage) currentPage = maxPage;
        if (currentPage < 0) currentPage = 0;

        int from = Math.min(currentPage * pageSize, total);
        int to = Math.min(from + pageSize, total);

        tableModel.setRowCount(0);
        for (int i = from; i < to; i++) {
            UserResponse u = filtered.get(i);
            String status = u.getStatus() != null ? u.getStatus() : "Inactive";
            tableModel.addRow(new Object[]{
                    Boolean.FALSE,
                    u.getName(),
                    roleLabel(status),
                    u.getEmail(),
                    String.format("ID-%04d", u.getId()),
                    status,
                    "",
                    u.getId()
            });
        }

        lblResults.setText(total == 0 ? "No results" : String.format("%d-%d of %d results", from + 1, to, total));
        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled(to < total);
    }

    private UserResponse findUserById(int id) {
        return allUsers.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    // The data model only tracks one "status" field (Active/Admin/Inactive/Suspended),
    // so Role is derived from it rather than being a separate stored attribute.
    private String roleLabel(String status) {
        return status != null && status.equalsIgnoreCase("ADMIN") ? "Administrator" : "Staff User";
    }

    private JLabel statusPill(String status) {
        String s = status == null ? "INACTIVE" : status.toUpperCase();
        switch (s) {
            case "ADMIN":
                return UITheme.createBadge("Admin", UITheme.PRIMARY.darker(),
                        UITheme.blend(UITheme.PRIMARY, Color.WHITE, 0.85f));
            case "ACTIVE":
                return UITheme.createBadge("Active", UITheme.SUCCESS.darker(),
                        UITheme.blend(UITheme.SUCCESS, Color.WHITE, 0.85f));
            case "SUSPENDED":
                return UITheme.createBadge("Suspended", UITheme.SECONDARY.darker(),
                        UITheme.blend(UITheme.SECONDARY, Color.WHITE, 0.85f));
            default:
                return UITheme.createBadge("Inactive", UITheme.NEUTRAL.darker(),
                        UITheme.blend(UITheme.NEUTRAL, Color.WHITE, 0.85f));
        }
    }

    /** Small circular table thumbnail: the member's profile photo if set, else a lettered placeholder. */
    private Icon circleAvatarIcon(String imagePath, String name, int size) {
        if (imagePath != null && !imagePath.isBlank()) {
            File f = new File(imagePath);
            if (f.exists()) {
                BufferedImage circular = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = circular.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new Ellipse2D.Float(0, 0, size, size));
                Image img = new ImageIcon(f.getAbsolutePath()).getImage()
                        .getScaledInstance(size, size, Image.SCALE_SMOOTH);
                g2.drawImage(img, 0, 0, size, size, null);
                g2.dispose();
                return new ImageIcon(circular);
            }
        }
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(UITheme.PRIMARY_SOFT);
        g.fillOval(0, 0, size, size);
        g.setColor(UITheme.PRIMARY);
        g.setFont(new Font("SansSerif", Font.BOLD, Math.round(size * 0.42f)));
        String ch = (name != null && !name.isBlank()) ? name.trim().substring(0, 1).toUpperCase() : "?";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(ch, (size - fm.stringWidth(ch)) / 2, (size + fm.getAscent()) / 2 - 4);
        g.dispose();
        return new ImageIcon(img);
    }

    // ---------- Delete ----------

    private void deleteMember(int id, String name) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><div style='font-size:12pt;'>Are you sure you want to delete this member?<br>" +
                        "This action cannot be undone!<br><br>Member: " + name + "</div></html>",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userService.delete(id);
                loadAllUsers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting member: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---------- Export ----------

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
            File file = excelService.writeToExcel(USERS_EXCEL_PATH, USERS_EXCEL_SHEET, USERS_EXCEL_HEADERS, rows);
            JOptionPane.showMessageDialog(this,
                    "Exported " + users.size() + " member(s) to:\n" + file.getPath(),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (MessageException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to export members: " + ex.getMessage(),
                    "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Add / Edit dialog ----------

    private void openMemberDialog(UserResponse existing) {
        boolean isEdit = existing != null;
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, isEdit ? "Edit Member" : "Add New Member", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.getContentPane().setBackground(UITheme.CARD);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UITheme.CARD);
        form.setBorder(BorderFactory.createEmptyBorder(24, 28, 12, 28));

        JTextField fName = fieldFor(isEdit ? nvl(existing.getName()) : "");

        JComboBox<String> fGender = new JComboBox<>(new String[]{"", "Male", "Female", "Other"});
        UITheme.styleField(fGender);
        if (isEdit && existing.getGender() != null) fGender.setSelectedItem(existing.getGender());

        JTextField fDob = fieldFor(isEdit ? nvl(existing.getDateOfBirth()) : "");
        JButton btnPick = UITheme.createSoftButton("Pick", UITheme.PRIMARY);
        btnPick.addActionListener(e -> pickDate(fDob));
        JPanel dobRow = new JPanel(new BorderLayout(8, 0));
        dobRow.setOpaque(false);
        dobRow.add(fDob, BorderLayout.CENTER);
        dobRow.add(btnPick, BorderLayout.EAST);

        JTextField fPhone = fieldFor(isEdit ? nvl(existing.getPhone()) : "");
        JTextField fAddress = fieldFor(isEdit ? nvl(existing.getAddress()) : "");
        JTextField fEmail = fieldFor(isEdit ? nvl(existing.getEmail()) : "");

        JPasswordField fPassword = new JPasswordField();
        fPassword.setEchoChar('•');
        UITheme.styleField(fPassword);
        JButton btnShowPw = UITheme.createSoftButton("Show", UITheme.NEUTRAL);
        btnShowPw.addActionListener(e -> togglePasswordVisibility(fPassword, btnShowPw));
        JPanel pwRow = new JPanel(new BorderLayout(8, 0));
        pwRow.setOpaque(false);
        pwRow.add(fPassword, BorderLayout.CENTER);
        pwRow.add(btnShowPw, BorderLayout.EAST);

        JComboBox<String> fStatus = new JComboBox<>(new String[]{"Active", "Admin", "Inactive", "Suspended"});
        UITheme.styleField(fStatus);
        if (isEdit && existing.getStatus() != null) fStatus.setSelectedItem(existing.getStatus());

        JLabel imgPreview = new JLabel("No Image", SwingConstants.CENTER);
        imgPreview.setOpaque(true);
        imgPreview.setBackground(Color.WHITE);
        imgPreview.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        imgPreview.setPreferredSize(new Dimension(90, 90));
        String[] imagePathHolder = {isEdit ? existing.getImage_path() : null};
        showImagePreview(imgPreview, imagePathHolder[0]);
        JButton btnChooseImage = UITheme.createSoftButton("Choose Image", UITheme.PRIMARY);
        btnChooseImage.addActionListener(e -> chooseImage(imgPreview, path -> imagePathHolder[0] = path));
        JPanel imgRow = new JPanel(new BorderLayout(12, 0));
        imgRow.setOpaque(false);
        imgRow.add(imgPreview, BorderLayout.WEST);
        imgRow.add(btnChooseImage, BorderLayout.CENTER);

        form.add(labeledRow("Full Name", fName));
        form.add(labeledRow("Gender", fGender));
        form.add(labeledRow("Date of Birth (YYYY-MM-DD)", dobRow));
        form.add(labeledRow("Phone Number", fPhone));
        form.add(labeledRow("Address", fAddress));
        form.add(labeledRow("Email", fEmail));
        form.add(labeledRow(isEdit ? "Password (leave blank to keep current)" : "Password", pwRow));
        form.add(labeledRow("Status", fStatus));
        form.add(labeledRow("Profile Image", imgRow));

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(UITheme.CARD);
        actions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(14, 28, 14, 28)));

        JButton btnCancel = UITheme.createSoftButton("Cancel", UITheme.NEUTRAL);
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = UITheme.createButton(isEdit ? "Save Changes" : "Add Member", UITheme.PRIMARY);
        btnSave.addActionListener(e -> saveMember(dialog, existing, fName, fGender, fDob, fPhone,
                fAddress, fEmail, fPassword, fStatus, imagePathHolder));

        actions.add(btnCancel);
        actions.add(btnSave);

        dialog.add(formScroll, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setSize(460, 640);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void saveMember(JDialog dialog, UserResponse existing, JTextField fName, JComboBox<String> fGender,
                            JTextField fDob, JTextField fPhone, JTextField fAddress, JTextField fEmail,
                            JPasswordField fPassword, JComboBox<String> fStatus, String[] imagePathHolder) {
        String name = fName.getText().trim();
        if (name.isEmpty()) { warn(dialog, "Please enter a full name", fName); return; }

        String email = fEmail.getText().trim();
        if (email.isEmpty()) { warn(dialog, "Please enter an email", fEmail); return; }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            warn(dialog, "Invalid email format. Example: user@example.com", fEmail);
            return;
        }

        String dob = fDob.getText().trim();
        if (!dob.isEmpty()) {
            try {
                LocalDate.parse(dob, dobFormatter);
            } catch (DateTimeParseException ex) {
                warn(dialog, "Invalid date format. Please use: YYYY-MM-DD", fDob);
                return;
            }
        }

        String phone = fPhone.getText().trim();
        if (!phone.isEmpty() && !phone.matches("\\d{9,15}")) {
            warn(dialog, "Phone number must be 9-15 digits", fPhone);
            return;
        }

        boolean isEdit = existing != null;
        String password = new String(fPassword.getPassword());
        if (!isEdit && password.isEmpty()) {
            warn(dialog, "Please enter a password", fPassword);
            return;
        }
        // Editing without retyping a password keeps the member's current password
        // hash instead of overwriting it with an empty string.
        if (isEdit && password.isEmpty()) {
            String kept = userService.getPasswordHash(existing.getId());
            password = kept != null ? kept : "";
        }

        String gender = (String) fGender.getSelectedItem();
        String status = (String) fStatus.getSelectedItem();

        UserRequest request = new UserRequest(name, gender != null ? gender : "", dob, phone,
                fAddress.getText().trim(), email, password, status != null ? status : "Active",
                imagePathHolder[0]);

        try {
            if (isEdit) {
                userService.update(existing.getId(), request);
                JOptionPane.showMessageDialog(dialog, "Member updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                userService.create(request);
                JOptionPane.showMessageDialog(dialog, "Member added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            dialog.dispose();
            loadAllUsers();
        } catch (MessageException ex) {
            JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void warn(Component parent, String message, JComponent field) {
        JOptionPane.showMessageDialog(parent, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
        field.requestFocus();
        if (field instanceof JTextField) ((JTextField) field).selectAll();
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private JTextField fieldFor(String text) {
        JTextField f = new JTextField(text);
        UITheme.styleField(f);
        return f;
    }

    private JPanel labeledRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 6));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(UITheme.FONT_BODY.getFamily(), Font.BOLD, 12));
        lbl.setForeground(UITheme.TEXT_DARK);

        row.add(lbl, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private void chooseImage(JLabel preview, Consumer<String> onPicked) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Profile Image");
        chooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File selected = chooser.getSelectedFile();
        try {
            File imagesDir = new File("images/users");
            imagesDir.mkdirs();
            String ext = "";
            int dot = selected.getName().lastIndexOf('.');
            if (dot >= 0) ext = selected.getName().substring(dot);
            File dest = new File(imagesDir, "user_" + System.currentTimeMillis() + ext);
            Files.copy(selected.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            String path = "images/users/" + dest.getName();
            showImagePreview(preview, path);
            onPicked.accept(path);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showImagePreview(JLabel preview, String path) {
        if (path == null || path.isBlank()) {
            preview.setIcon(null);
            preview.setText("No Image");
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            preview.setIcon(null);
            preview.setText("Missing");
            return;
        }
        Image scaled = new ImageIcon(file.getAbsolutePath()).getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
        preview.setText("");
        preview.setIcon(new ImageIcon(scaled));
    }

    private void pickDate(JTextField target) {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();

        Integer[] years = new Integer[150];
        for (int i = 0; i < 150; i++) years[i] = currentYear - i;

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
        panel.add(dayField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Select Date",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

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
                target.setText(String.format("%04d-%02d-%02d", year, month, day));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void togglePasswordVisibility(JPasswordField field, JButton toggleBtn) {
        if (field.getEchoChar() == '•') {
            field.setEchoChar((char) 0);
            toggleBtn.setText("Hide");
        } else {
            field.setEchoChar('•');
            toggleBtn.setText("Show");
        }
    }
}