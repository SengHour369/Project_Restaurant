package org.example.Service.ServiceHandler;

import org.example.Exception.MessageException;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.example.Database.DatabaseConnection.getConnection;

public class OrderServiceHandler {



    public boolean hasValidRestaurant(String restaurantId) throws MessageException {
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Restaurant ID must not be blank");
            throw new MessageException("Restaurant ID must not be blank");
        }
        return true;
    }

    public boolean hasValidUser(String userId) throws MessageException {
        if (userId == null || userId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "User ID must not be blank");
            throw new MessageException("User ID must not be blank");
        }
        return true;
    }



    public boolean isUserIdExist(String userId) throws MessageException {
        String sql = "SELECT id FROM users WHERE user_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(null,
                        "User ID does not exist");
                throw new MessageException("User ID does not exist");
            }

        } catch (SQLException e) {
            throw new MessageException("Database error while checking user ID");
        }

        return true;
    }


    public boolean isRestaurantIdExist(String restaurantId) throws MessageException {
        String sql = "SELECT id FROM restaurants WHERE restaurant_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, restaurantId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(null,
                        "Restaurant ID does not exist");
                throw new MessageException("Restaurant ID does not exist");
            }

        } catch (SQLException e) {
            throw new MessageException("Database error while checking restaurant ID");
        }

        return true;
    }
}
