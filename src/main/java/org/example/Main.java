package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.UI.LoginFrame;
import org.example.UI.UITheme;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        setupLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }

    /**
     * Installs a clean, modern flat look-and-feel (FlatLaf) tuned to the app's
     * indigo accent, so every standard component — tables, fields, tabs, combos,
     * scrollbars — reads like a modern web app. Falls back gracefully if the
     * modern L&F can't be loaded for any reason.
     */
    private static void setupLookAndFeel() {
        try {
            FlatLightLaf.setup();

            // Rounded, spacious, web-like component geometry
            UIManager.put("Component.arc", 12);
            UIManager.put("Button.arc", 12);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("CheckBox.arc", 6);
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 1);

            // Accent-driven focus / selection
            UIManager.put("Component.focusColor", UITheme.PRIMARY);
            UIManager.put("Component.borderColor", UITheme.BORDER);

            // Tabs: flat with an accent underline on the selected tab
            UIManager.put("TabbedPane.tabType", "underlined");
            UIManager.put("TabbedPane.showTabSeparators", false);
            UIManager.put("TabbedPane.tabHeight", 44);
            UIManager.put("TabbedPane.selectedBackground", UITheme.CARD);
            UIManager.put("TabbedPane.underlineColor", UITheme.PRIMARY);
            UIManager.put("TabbedPane.focusColor", UITheme.PRIMARY_SOFT);
            UIManager.put("TabbedPane.hoverColor", UITheme.PRIMARY_SOFT);

            // Tables: airy rows, subtle grid, accent selection
            UIManager.put("Table.rowHeight", 40);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
            UIManager.put("Table.gridColor", UITheme.BORDER);
            UIManager.put("Table.selectionBackground", UITheme.PRIMARY_SOFT);
            UIManager.put("Table.selectionForeground", UITheme.TEXT_DARK);
            UIManager.put("TableHeader.height", 42);
            UIManager.put("TableHeader.separatorColor", UITheme.BORDER);
            UIManager.put("TableHeader.bottomSeparatorColor", UITheme.BORDER);

            // Rounded, slim scrollbars
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("ScrollBar.track", UITheme.BACKGROUND);

            UIManager.put("Component.background", UITheme.CARD);
            UIManager.put("Panel.background", UITheme.BACKGROUND);

            // Modern system font
            Font ui = UITheme.baseFont(14f);
            UIManager.put("defaultFont", new FontUIResource(ui));

            return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fallbacks if FlatLaf is unavailable
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }
    }
}