package org.example.Service;

import org.example.DTO.Request.OrderRequest;
import org.example.DTO.Response.OrderResponse;

import java.util.List;

public interface ServiceOrder {
    OrderResponse createOrder(OrderRequest orderRequest);
    OrderResponse updateOrder(OrderRequest orderRequest);
    void deleteOrder(OrderRequest orderRequest);
    OrderResponse findOrderById(OrderRequest orderRequest);
    List<OrderResponse> findAllOrders();
}
