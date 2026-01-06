package org.example.Service;

import org.example.DTO.Request.OrderItemRequest;
import org.example.DTO.Response.OrderItemResponse;


import java.util.List;

public interface ServiceOrderItem {
     OrderItemResponse createOrderItem(OrderItemRequest orderItemRequest);
     OrderItemResponse updateOrderItem(OrderItemRequest orderItemRequest,int id);
     void deleteOrderItem(int id);
     OrderItemResponse findOrderItemById(int id);
     List<OrderItemResponse> findAllOrderItems();
}
