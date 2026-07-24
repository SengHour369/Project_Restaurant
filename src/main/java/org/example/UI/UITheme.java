package org.example.UI;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Shared modern "clean web app" design system: a light SaaS palette with an
 * indigo accent, a resolved system font, and flat rounded component helpers,
 * so every screen reads as one consistent, modern app.
 *
 * Constant names and method signatures are kept stable so the whole app picks
 * up the new look without structural changes.
 */
public final class UITheme {
    private UITheme() {}

    // ---- Palette (modern light SaaS) ----
    public static final Color BACKGROUND   = Color.decode("#F5F6F8"); // app background
    public static final Color CARD         = Color.decode("#FFFFFF"); // surfaces / cards
    public static final Color PRIMARY      = Color.decode("#4F46E5"); // indigo accent
    public static final Color PRIMARY_DARK = Color.decode("#4338CA"); // accent hover
    public static final Color PRIMARY_SOFT = Color.decode("#EEF2FF"); // soft accent tint
    public static final Color SECONDARY    = Color.decode("#EF4444"); // red (destructive)
    public static final Color SUCCESS      = Color.decode("#16A34A"); // green
    public static final Color ACCENT       = Color.decode("#F59E0B"); // amber (ratings/highlights)
    public static final Color NEUTRAL      = Color.decode("#64748B"); // slate (neutral buttons / borders)
    public static final Color BORDER       = Color.decode("#E5E7EB"); // hairline borders
    public static final Color ROW_ALT      = Color.decode("#F9FAFB"); // zebra rows
    public static final Color SELECTION    = Color.decode("#E0E7FF"); // selection tint
    public static final Color TEXT_DARK    = Color.decode("#111827"); // primary text
    public static final Color TEXT_MUTED   = Color.decode("#6B7280"); // secondary text

    // ---- Sidebar-shell accent (top bar / active nav highlight only) ----
    public static final Color BRAND_BLUE      = Color.decode("#29ABE2");
    public static final Color BRAND_BLUE_DARK = Color.decode("#1E90C4");
    public static final Color BRAND_BLUE_SOFT = Color.decode("#E4F6FD");

    // ---- Typography ----
    private static final String FAMILY = resolveFamily();

    public static final Font FONT_TITLE  = new Font(FAMILY, Font.BOLD, 22);
    public static final Font FONT_H1     = new Font(FAMILY, Font.BOLD, 28);
    public static final Font FONT_BODY   = new Font(FAMILY, Font.PLAIN, 14);
    public static final Font FONT_BUTTON = new Font(FAMILY, Font.BOLD, 13);
    public static final Font FONT_MUTED  = new Font(FAMILY, Font.PLAIN, 12);

    public static Font baseFont(float size) {
        return new Font(FAMILY, Font.PLAIN, Math.round(size));
    }

    /** Picks the nicest available modern UI font on this machine. */
    private static String resolveFamily() {
        String[] preferred = {"Inter", "SF Pro Text", "SF Pro Display", ".AppleSystemUIFont",
                "Helvetica Neue", "Segoe UI", "Roboto", "Arial"};
        java.util.Set<String> available = new java.util.HashSet<>(java.util.Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : preferred) {
            if (available.contains(f)) return f;
        }
        return "SansSerif";
    }

    /**
     * Soft rounded outline (no fill) for text fields/combo boxes.
     * Kept for backwards-compatibility with existing panels.
     */
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
            g2.setStroke(new BasicStroke(1.2f));
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
     * A flat, rounded, web-style button with hover/pressed feedback.
     * White label on a solid accent fill; darkens on hover, more on press.
     */
    public static class RoundedButton extends JButton {
        private final Color base;
        private boolean hover;
        private boolean pressed;

        public RoundedButton(String text, Color color) {
            super(text);
            this.base = color;
            setForeground(Color.WHITE);
            setFont(FONT_BUTTON);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; pressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = base;
            if (!isEnabled()) {
                fill = blend(base, Color.WHITE, 0.55f);
            } else if (pressed) {
                fill = blend(base, Color.BLACK, 0.18f);
            } else if (hover) {
                fill = blend(base, Color.BLACK, 0.09f);
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JButton createButton(String text, Color color) {
        return new RoundedButton(text, color);
    }

    /** A softer, "ghost" button: tinted background, colored label — for secondary actions. */
    public static JButton createSoftButton(String text, Color color) {
        RoundedButton b = new RoundedButton(text, blend(color, Color.WHITE, 0.86f));
        b.setForeground(color.darker());
        return b;
    }

    /** Applies a soft rounded outline + padding to a text field/combo. */
    public static void styleField(JComponent field) {
        field.setFont(FONT_BODY);
        field.setBorder(new RoundedLineBorder(BORDER, 12, 8, 12));
    }

    /** A white rounded "card" surface with a hairline border. */
    public static JPanel createCard() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(new CardBorder(16));
        panel.setBackground(CARD);
        return panel;
    }

    /** Rounded filled card background + hairline border, painted via a Border. */
    public static class CardBorder extends AbstractBorder {
        private final int radius;

        public CardBorder(int radius) { this.radius = radius; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CARD);
            g2.fillRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(1, 1, 1, 1);
            return insets;
        }
    }

    /** A small rounded status pill (e.g. ADMIN / ACTIVE badges). */
    public static JLabel createBadge(String text, Color fg, Color bg) {
        return new PillLabel(text, fg, bg);
    }

    /**
     * A label that paints its own rounded pill background, then its text, in that
     * order — unlike a {@code Border} (which Swing always paints AFTER the
     * component's own content), so the text is never covered by the fill.
     */
    public static class PillLabel extends JLabel {
        private Color fill;

        public PillLabel(String text, Color fg, Color fill) {
            super(text);
            this.fill = fill;
            setForeground(fg);
            setFont(new Font(FAMILY, Font.BOLD, 11));
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        }

        public void setFill(Color fill) {
            this.fill = fill;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * A translucent "ghost" button meant for use ON TOP OF a colored or gradient
     * background (e.g. a hero header) — a soft white fill, white outline, white
     * text. {@link #createSoftButton} is unsuitable there: it tints toward white,
     * so passing white in produces near-invisible white-on-white text.
     */
    public static JButton createOutlineButton(String text) {
        JButton b = new JButton(text) {
            private boolean hover;
            {
                setForeground(Color.WHITE);
                setFont(FONT_BUTTON);
                setFocusPainted(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setOpaque(false);
                setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, hover ? 60 : 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(255, 255, 255, 130));
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        return b;
    }

    /** Linear blend between two colors (0 = a, 1 = b). */
    public static Color blend(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));
        int r = Math.round(a.getRed()   * (1 - t) + b.getRed()   * t);
        int g = Math.round(a.getGreen() * (1 - t) + b.getGreen() * t);
        int bl = Math.round(a.getBlue() * (1 - t) + b.getBlue()  * t);
        return new Color(r, g, bl);
    }

    /** Convenience matte "hairline" bottom border used by headers. */
    public static Border bottomHairline() {
        return BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER);
    }

    /** A small round avatar: the given photo if it exists, else a lettered placeholder. */
    public static JComponent createAvatar(String imagePath, String name, int size) {
        return new AvatarCircle(imagePath, name, size);
    }

    /** Round profile picture that falls back to the person's initial when no image is set. */
    public static class AvatarCircle extends JComponent {
        private final int size;
        private final String name;
        private Image image;

        public AvatarCircle(String imagePath, String name, int size) {
            this.size = size;
            this.name = name;
            setPreferredSize(new Dimension(size, size));
            setImagePath(imagePath);
        }

        public void setImagePath(String path) {
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

            java.awt.geom.Ellipse2D circle = new java.awt.geom.Ellipse2D.Float(1, 1, size - 2, size - 2);

            if (image != null) {
                g2.setClip(circle);
                g2.drawImage(image, 1, 1, size - 2, size - 2, null);
                g2.setClip(null);
            } else {
                g2.setColor(Color.WHITE);
                g2.fill(circle);
                g2.setColor(PRIMARY);
                String initial = (name != null && !name.isBlank()) ? name.trim().substring(0, 1).toUpperCase() : "?";
                g2.setFont(new Font(FAMILY, Font.BOLD, Math.round(size * 0.4f)));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (size - fm.stringWidth(initial)) / 2, (size + fm.getAscent()) / 2 - 4);
            }

            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(circle);
            g2.dispose();
        }
    }
}