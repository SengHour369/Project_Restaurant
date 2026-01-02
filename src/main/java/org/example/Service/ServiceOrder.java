package org.example.Service;

import org.example.DTO.Request.OrderRequest;
import org.example.DTO.Response.OrderResponse;

import java.util.List;

public interface ServiceOrder {
    OrderResponse createOrder(OrderRequest orderRequest);
    OrderResponse updateOrder(int orderId, OrderRequest r);
    void deleteOrder(int orderId);
    OrderResponse findOrderById(int orderId);
    List<OrderResponse> findAllOrders();
}
