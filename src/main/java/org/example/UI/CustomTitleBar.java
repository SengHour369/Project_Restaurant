package org.example.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A custom title bar with minimize, maximize/restore, and close buttons.
 * Supports dragging the window and toggling maximize state.
 */
public class CustomTitleBar extends JPanel {
    private final JFrame parentFrame;
    private JButton btnMinimize;
    private JButton btnMaximize;
    private JButton btnClose;
    private JLabel titleLabel;

    private Point initialClick;
    private boolean maximized = false;

    public CustomTitleBar(JFrame frame, String title) {
        this.parentFrame = frame;
        setLayout(new BorderLayout());
        setOpaque(false); // allow gradient painting
        setPreferredSize(new Dimension(0, 45));
        setBorder(new EmptyBorder(5, 15, 5, 15));

        // Left: Title with emoji for cuteness
        titleLabel = new JLabel("🍽️ " + title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.WEST);

        // Right: Window controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controlsPanel.setOpaque(false);

        btnMinimize = createControlButton("—", new Color(52, 152, 219)); // blue
        btnMaximize = createControlButton("⧉", new Color(46, 204, 113)); // green
        btnClose = createControlButton("✕", new Color(231, 76, 60));     // red

        controlsPanel.add(btnMinimize);
        controlsPanel.add(btnMaximize);
        controlsPanel.add(btnClose);
        add(controlsPanel, BorderLayout.EAST);

        // Drag listener to move window
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (initialClick != null && !maximized) {
                    Point newPos = parentFrame.getLocation();
                    newPos.translate(e.getX() - initialClick.x, e.getY() - initialClick.y);
                    parentFrame.setLocation(newPos);
                }
            }
        });

        // Button actions
        btnMinimize.addActionListener(e -> parentFrame.setState(JFrame.ICONIFIED));
        btnMaximize.addActionListener(e -> toggleMaximize());
        btnClose.addActionListener(e -> parentFrame.dispose());

        // Window state listener to update maximize button icon
        parentFrame.addWindowStateListener(e -> {
            if ((e.getNewState() & JFrame.MAXIMIZED_BOTH) != 0) {
                maximized = true;
                btnMaximize.setText("⧉");
                btnMaximize.setToolTipText("Restore Down");
            } else if ((e.getNewState() & JFrame.NORMAL) != 0) {
                maximized = false;
                btnMaximize.setText("⧉");
                btnMaximize.setToolTipText("Maximize");
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        // Gradient from dark blue to lighter blue
        GradientPaint gp = new GradientPaint(0, 0, new Color(52, 73, 94), w, 0, new Color(41, 128, 185));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
        g2d.dispose();
    }

    private JButton createControlButton(String label, Color baseColor) {
        JButton button = new JButton(label);
        button.setFont(new Font("SansSerif", Font.PLAIN, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(35, 28));
        button.setToolTipText(label.equals("—") ? "Minimize" :
                label.equals("⧉") ? "Maximize" : "Close");

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(baseColor.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
            }
        });
        return button;
    }

    private void toggleMaximize() {
        int state = parentFrame.getExtendedState();
        if ((state & JFrame.MAXIMIZED_BOTH) != 0) {
            parentFrame.setExtendedState(state & ~JFrame.MAXIMIZED_BOTH);
        } else {
            parentFrame.setExtendedState(state | JFrame.MAXIMIZED_BOTH);
        }
    }

    public void setTitle(String title) {
        titleLabel.setText("🍽️ " + title);
    }
}