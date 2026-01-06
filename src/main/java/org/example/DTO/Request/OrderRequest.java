package org.example.DTO.Request;

import org.example.BaseEntity;
import org.example.DTO.Response.RestaurantResponse;
import org.example.DTO.Response.UserResponse;
import org.example.Model.OrderItem;
import org.example.Model.Payment;
import org.example.Model.Restaurant;
import org.example.Model.User;

import java.time.LocalDateTime;
import java.util.List;

public class OrderRequest extends BaseEntity {
    private LocalDateTime orderDate;
    private Double totalPrice;
    private UserResponse user;
    private RestaurantResponse restaurant;
    private List<OrderItemRequest> orderItems;
    private Payment payment;

    public OrderRequest(LocalDateTime orderDate, Double totalPrice,
                        UserResponse user, RestaurantResponse restaurant, List<OrderItemRequest> orderItems,
                        Payment payment) {
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.user = user;
        this.restaurant = restaurant;
        this.orderItems = orderItems;
        this.payment = payment;
    }

    public OrderRequest() {
    }

    // ---------- Getters & Setters ----------
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }

    public RestaurantResponse getRestaurant() { return restaurant; }
    public void setRestaurant(RestaurantResponse restaurant) { this.restaurant = restaurant; }

    public List<OrderItemRequest> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemRequest> orderItems) { this.orderItems = orderItems; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
}
