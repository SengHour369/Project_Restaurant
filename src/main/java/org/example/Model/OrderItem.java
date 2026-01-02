package org.example.Model;

import org.example.BaseEntity;

import java.util.List;

public class OrderItem extends BaseEntity {
    private Integer quantity;
    private Double price;
    private Order order;
    private List<MenuItem> menuItem;

    public OrderItem(Integer quantity, Double price, Order order, List<MenuItem> menuItem) {
        this.quantity = quantity;
        this.price = price;
        this.order = order;
        this.menuItem = menuItem;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<MenuItem> getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(List<MenuItem> menuItem) {
        this.menuItem = menuItem;
    }
}
