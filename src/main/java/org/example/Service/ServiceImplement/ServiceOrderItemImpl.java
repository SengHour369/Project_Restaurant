package org.example.Service.ServiceImplement;

import org.example.Database.DatabaseConnection;
import org.example.DTO.Request.OrderItemRequest;
import org.example.DTO.Response.OrderItemResponse;
import org.example.Service.ServiceOrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceOrderItemImpl implements ServiceOrderItem {

    @Override
    public OrderItemResponse createOrderItem(OrderItemRequest r) {
        String sql = "INSERT INTO order_items (order_id, menu_item_id, quantity, price) " +
                "VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, r.getOrderId());
            ps.setInt(2, r.getMenuItemId());
            ps.setInt(3, r.getQuantity());
            ps.setDouble(4, r.getPrice());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new OrderItemResponse(
                        rs.getInt("id"),
                        r.getOrderId(),
                        r.getMenuItemId(),
                        r.getQuantity(),
                        r.getPrice()
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public OrderItemResponse updateOrderItem(OrderItemRequest r, int id) {
        String sql = "UPDATE order_items SET order_id=?, menu_item_id=?, quantity=?, price=? WHERE id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, r.getOrderId());
            ps.setInt(2, r.getMenuItemId());
            ps.setInt(3, r.getQuantity());
            ps.setDouble(4, r.getPrice());
            ps.setInt(5, id);

            int updated = ps.executeUpdate();
            if (updated > 0) {
                return findOrderItemById(id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void deleteOrderItem(int id) {
        String sql = "DELETE FROM order_items WHERE id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public OrderItemResponse findOrderItemById(int id) {
        String sql = "SELECT * FROM order_items WHERE id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new OrderItemResponse(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getInt("menu_item_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<OrderItemResponse> findAllOrderItems() {
        List<OrderItemResponse> list = new ArrayList<>();
        String sql = "SELECT * FROM order_items";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new OrderItemResponse(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getInt("menu_item_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
