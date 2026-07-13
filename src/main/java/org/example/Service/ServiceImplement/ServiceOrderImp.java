package org.example.Service.ServiceImplement;

import org.example.DTO.Request.OrderRequest;
import org.example.DTO.Request.OrderItemRequest;
import org.example.DTO.Response.OrderResponse;
import org.example.DTO.Response.RestaurantResponse;
import org.example.DTO.Response.UserResponse;
import org.example.Exception.MessageException;
import org.example.Model.OrderItem;
import org.example.Model.Payment;
import org.example.Service.ServiceOrder;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.example.Database.DatabaseConnection.getConnection;

public class ServiceOrderImp implements ServiceOrder {
    ServiceUserImp serviceUser = new ServiceUserImp();
    ServiceRestaurantImp serviceRestaurant = new ServiceRestaurantImp();

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest) throws MessageException {
        // No existence checks – the User and Restaurant are already valid objects from the database.
        // We only validate they are not null.
        if (orderRequest.getUser() == null || orderRequest.getUser().getId() <= 0) {
            throw new MessageException("Invalid user: user ID is missing or zero.");
        }
        if (orderRequest.getRestaurant() == null || orderRequest.getRestaurant().getId() <= 0) {
            throw new MessageException("Invalid restaurant: restaurant ID is missing or zero.");
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // Insert payment
            String sqlPayment = """
                INSERT INTO payments(amount, type)
                VALUES (?,?) RETURNING id
            """;
            PreparedStatement psPay = conn.prepareStatement(sqlPayment);
            psPay.setDouble(1, orderRequest.getTotalPrice());
            psPay.setString(2, "PAID");
            ResultSet rsPay = psPay.executeQuery();
            rsPay.next();
            int paymentId = rsPay.getInt("id");

            // Insert order
            String sqlOrder = """
                INSERT INTO orders(order_date, total_price, user_id, restaurants_id, payment_id)
                VALUES (?,?,?,?,?) RETURNING id
            """;
            PreparedStatement psOrder = conn.prepareStatement(sqlOrder);
            psOrder.setTimestamp(1, Timestamp.valueOf(orderRequest.getOrderDate()));
            psOrder.setDouble(2, orderRequest.getTotalPrice());
            psOrder.setInt(3, orderRequest.getUser().getId());
            psOrder.setInt(4, orderRequest.getRestaurant().getId());
            psOrder.setInt(5, paymentId);
            ResultSet rsOrder = psOrder.executeQuery();
            rsOrder.next();
            int orderId = rsOrder.getInt("id");

            // Insert order items
            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderItemRequest itemReq : orderRequest.getOrderItems()) {
                String sqlItem = """
                    INSERT INTO orderitems(order_id, menu_item_id, quantity, price)
                    VALUES (?,?,?,?) RETURNING id
                """;
                PreparedStatement psItem = conn.prepareStatement(sqlItem);
                psItem.setInt(1, orderId);
                psItem.setInt(2, itemReq.getMenuItemId());
                psItem.setInt(3, itemReq.getQuantity());
                psItem.setDouble(4, itemReq.getPrice());
                ResultSet rsItem = psItem.executeQuery();
                rsItem.next();
                OrderItem item = new OrderItem();
                item.setId(rsItem.getInt("id"));
                item.setMenuItemId(itemReq.getMenuItemId());
                item.setQuantity(itemReq.getQuantity());
                item.setPrice(itemReq.getPrice());
                orderItems.add(item);
            }

            conn.commit();

            return new OrderResponse(
                    orderId,
                    orderRequest.getOrderDate(),
                    orderRequest.getTotalPrice(),
                    orderRequest.getUser(),
                    orderRequest.getRestaurant(),
                    orderItems,
                    new Payment(paymentId)
            );

        } catch (SQLException e) {
            e.printStackTrace();
            throw new MessageException("Failed to create order: " + e.getMessage());
        }
    }

    @Override
    public void deleteOrder(int orderId) throws MessageException {
        try (Connection conn = getConnection()) {
            PreparedStatement ps1 = conn.prepareStatement(
                    "DELETE FROM orderitems WHERE order_id=?");
            ps1.setInt(1, orderId);
            ps1.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement(
                    "DELETE FROM orders WHERE id=?");
            ps2.setInt(1, orderId);
            ps2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MessageException("Failed to delete order");
        }
    }

    @Override
    public OrderResponse findOrderById(int orderId) {
        try (Connection conn = getConnection()) {
            PreparedStatement psOrder =
                    conn.prepareStatement("SELECT * FROM orders WHERE id=?");
            psOrder.setInt(1, orderId);
            ResultSet rsOrder = psOrder.executeQuery();
            if (!rsOrder.next()) return null;

            LocalDateTime orderDate =
                    rsOrder.getTimestamp("order_date").toLocalDateTime();
            double total = rsOrder.getDouble("total_price");
            int userId = rsOrder.getInt("user_id");
            int restaurantId = rsOrder.getInt("restaurants_id");
            int paymentId = rsOrder.getInt("payment_id");

            List<OrderItem> items = new ArrayList<>();
            PreparedStatement psItems =
                    conn.prepareStatement("SELECT * FROM orderitems WHERE order_id=?");
            psItems.setInt(1, orderId);
            ResultSet rsItems = psItems.executeQuery();
            while (rsItems.next()) {
                OrderItem item = new OrderItem();
                item.setId(rsItems.getInt("id"));
                item.setMenuItemId(rsItems.getInt("menu_item_id"));
                item.setQuantity(rsItems.getInt("quantity"));
                item.setPrice(rsItems.getDouble("price"));
                items.add(item);
            }

            UserResponse user = serviceUser.findById(userId);
            RestaurantResponse restaurant = serviceRestaurant.findRestaurantById(restaurantId);

            return new OrderResponse(
                    orderId,
                    orderDate,
                    total,
                    user,
                    restaurant,
                    items,
                    new Payment(paymentId)
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<OrderResponse> findAllOrders() {
        List<OrderResponse> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement ps =
                    conn.prepareStatement("SELECT id FROM orders ORDER BY order_date DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderResponse o = findOrderById(rs.getInt("id"));
                if (o != null) list.add(o);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<OrderResponse> findOrdersByUserId(int userId) {
        List<OrderResponse> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement ps =
                    conn.prepareStatement("SELECT id FROM orders WHERE user_id = ? ORDER BY order_date DESC");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderResponse o = findOrderById(rs.getInt("id"));
                if (o != null) list.add(o);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public OrderResponse updateOrder(int orderId, OrderRequest r) throws MessageException {
        // Not implemented – add logic if needed
        return null;
    }
}