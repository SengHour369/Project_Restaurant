package org.example;


import org.example.UI.MainAppUI;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            MainAppUI mainUI = new MainAppUI();
            mainUI.setVisible(true);
        });
    }
}
