package org.example.Service.ServiceImplement;

import org.example.Database.DatabaseConnection;
import org.example.DTO.Request.OrderRequest;
import org.example.DTO.Response.OrderResponse;
import org.example.Model.User;
import org.example.Model.Restaurant;
import org.example.Model.Payment;
import org.example.Service.ServiceOrder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceOrderImp implements ServiceOrder {


    @Override
    public OrderResponse createOrder(OrderRequest r) {

        String sql = """
            INSERT INTO orders
            (order_date, total_price, user_id, restaurants_id, payment_id)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(r.getOrderDate()));
            ps.setDouble(2, r.getTotalPrice());
            ps.setInt(3, r.getUser().getId());
            ps.setInt(4, r.getRestaurant().getId());
            ps.setInt(5, r.getPayment().getId());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return findOrderById(rs.getInt(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public OrderResponse updateOrder(int orderId, OrderRequest r) {

        String sql = """
            UPDATE orders SET
            order_date=?, total_price=?, user_id=?, restaurants_id=?, payment_id=?
            WHERE id=?
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(r.getOrderDate()));
            ps.setDouble(2, r.getTotalPrice());
            ps.setInt(3, r.getUser().getId());
            ps.setInt(4, r.getRestaurant().getId());
            ps.setInt(5, r.getPayment().getId());
            ps.setInt(6, orderId);

            ps.executeUpdate();
            return findOrderById(orderId);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public void deleteOrder(int orderId) {

        String sql = "DELETE FROM orders WHERE id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public OrderResponse findOrderById(int orderId) {

        String sql = "SELECT * FROM orders WHERE id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapToOrderResponse(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public List<OrderResponse> findAllOrders() {

        List<OrderResponse> list = new ArrayList<>();

        String sql = "SELECT * FROM orders";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapToOrderResponse(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    private OrderResponse mapToOrderResponse(ResultSet rs) throws SQLException {

        User user = new User();
        user.setId(rs.getInt("user_id"));

        Restaurant restaurant = new Restaurant();
        restaurant.setId(rs.getInt("restaurants_id"));

        Payment payment = new Payment();
        payment.setId(rs.getInt("payment_id"));

        return new OrderResponse(
                rs.getTimestamp("order_date").toLocalDateTime(),
                rs.getDouble("total_price"),
                user,
                restaurant,
                null,
                payment
        );
    }
}
