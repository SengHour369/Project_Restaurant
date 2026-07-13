package org.example.Service;

import org.example.DTO.Request.OrderRequest;
import org.example.DTO.Response.OrderResponse;
import org.example.Exception.MessageException;

import java.util.List;

public interface ServiceOrder {
    OrderResponse createOrder(OrderRequest orderRequest) throws MessageException;
    OrderResponse updateOrder(int orderId, OrderRequest r) throws MessageException;
    void deleteOrder(int orderId) throws MessageException;
    OrderResponse findOrderById(int orderId);
    List<OrderResponse> findAllOrders();

    // NEW METHOD: retrieve orders for a specific user
    List<OrderResponse> findOrdersByUserId(int userId);
}