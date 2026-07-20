package org.example.UI;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Shared "warm & playful food theme" palette and rounded component helpers,
 * so every screen looks like one consistent, cute app instead of a patchwork.
 */
public final class UITheme {
    private UITheme() {}

    public static final Color BACKGROUND = Color.decode("#FFF6EC");
    public static final Color CARD = Color.WHITE;
    public static final Color PRIMARY = Color.decode("#FF9F45");
    public static final Color SECONDARY = Color.decode("#FF6B5B");
    public static final Color SUCCESS = Color.decode("#6FCF97");
    public static final Color ACCENT = Color.decode("#FFB627");
    public static final Color NEUTRAL = Color.decode("#D8A48F");
    public static final Color TEXT_DARK = Color.decode("#6B4226");
    public static final Color TEXT_MUTED = Color.decode("#A67B5B");

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 13);

    /** Soft rounded outline (no fill) for text fields/combo boxes, safe to use on opaque editable components. */
    public static class RoundedLineBorder extends AbstractBorder {
        private final Color outline;
        private final int radius;
        private final int padV;
        private final int padH;

        public RoundedLineBorder(Color outline, int radius, int padV, int padH) {
            this.outline = outline;
            this.radius = radius;
            this.padV = padV;
            this.padH = padH;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(outline);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(padV, padH, padV, padH);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(padV, padH, padV, padH);
            return insets;
        }
    }

    /**
     * A rounded, warm-colored, hover-aware pill button.
     *
     * Custom-painted rounded fill has to happen in paintComponent(), not in a Border:
     * borders paint AFTER the button's own text/icon, so a border that fills the
     * background would paint right over the label and hide it.
     */
    public static class RoundedButton extends JButton {
        private final Color base;

        public RoundedButton(String text, Color color) {
            super(text);
            this.base = color;
            setForeground(Color.WHITE);
            setFont(FONT_BUTTON);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new RoundedLineBorder(color.darker(), 18, 10, 20));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBackground(color);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(isEnabled() ? base.darker() : base);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(base);
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JButton createButton(String text, Color color) {
        return new RoundedButton(text, color);
    }

    /** Applies a soft rounded outline + padding to a text field/combo without touching its editing behavior. */
    public static void styleField(JComponent field) {
        field.setFont(FONT_BODY);
        field.setBorder(new RoundedLineBorder(NEUTRAL, 14, 8, 14));
    }
}