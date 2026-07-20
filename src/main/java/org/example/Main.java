package org.example;

import org.example.Database.DatabaseConnection;
import org.example.UI.LoginFrame;
import javax.swing.*;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        // Set look and feel (optional)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Launch the login frame on the EDT
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
