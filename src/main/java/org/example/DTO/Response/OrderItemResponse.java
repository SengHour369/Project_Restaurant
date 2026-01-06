package org.example.DTO.Response;

import org.example.BaseEntity;

public class OrderItemResponse extends BaseEntity {

    private Integer orderId;
    private Integer menuItemId;
    private Integer quantity;
    private Double price;

    public OrderItemResponse(int id, Integer orderId, Integer menuItemId,
                             Integer quantity, Double price) {
        setId(id);
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.quantity = quantity;
        this.price = price;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public Integer getMenuItemId() {
        return menuItemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getPrice() {
        return price;
    }
}
