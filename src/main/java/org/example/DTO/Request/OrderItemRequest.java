package org.example.DTO.Request;

public class OrderItemRequest {

    private Integer orderId;
    private Integer menuItemId;
    private Integer quantity;
    private Double price;

    public OrderItemRequest(Integer orderId, Integer menuItemId,
                            Integer quantity, Double price) {
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.quantity = quantity;
        this.price = price;
    }

    public OrderItemRequest() {

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

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public void setMenuItemId(Integer menuItemId) {
        this.menuItemId = menuItemId;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
