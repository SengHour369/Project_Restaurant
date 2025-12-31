package org.example.DTO.Response;

import org.example.Model.OrderItem;
import org.example.Model.Payment;
import org.example.Model.Restaurant;
import org.example.Model.User;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    private LocalDateTime orderDate;
    private Double totalPrice;
    private User user;
    private Restaurant restaurant;
    private List<OrderItem> orderItems;
    private Payment payment;

    public OrderResponse(LocalDateTime orderDate, Double totalPrice,
                 User user, Restaurant restaurant, List<OrderItem> orderItems,
                 Payment payment) {
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.user = user;
        this.restaurant = restaurant;
        this.orderItems = orderItems;
        this.payment = payment;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}
