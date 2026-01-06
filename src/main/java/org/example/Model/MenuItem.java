package org.example.Model;

import org.example.BaseEntity;

public class MenuItem extends BaseEntity {
    private Restaurant restaurant;
    private Double price;
    private String name;
    private String description;
    private Boolean active;

    public MenuItem(Restaurant restaurant, Double code,
                    String name, String description, Boolean active) {
        this.restaurant = restaurant;
        this.price = code;
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
