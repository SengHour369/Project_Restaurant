package org.example.DTO.Request;

import org.example.BaseEntity;
import org.example.Model.Restaurant;

public class MenuItemRequest extends BaseEntity {
    private int restaurant;
    private String price;
    private String name;
    private String description;
    private Boolean active;
    private String image_path;
    private Boolean isVeg;

    public MenuItemRequest(int restaurant, String code,
                           String name, String description, Boolean active) {
        this.restaurant = restaurant;
        this.price = code;
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public MenuItemRequest() {

    }

    public int getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(int restaurant) {
        this.restaurant = restaurant;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
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

    public String getImagePath() {
        return image_path;
    }

    public void setImagePath(String image_path) {
        this.image_path = image_path;
    }

    public Boolean getIsVeg() {
        return isVeg;
    }

    public void setIsVeg(Boolean isVeg) {
        this.isVeg = isVeg;
    }
}