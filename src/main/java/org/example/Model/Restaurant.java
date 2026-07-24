package org.example.Model;

import org.example.BaseEntity;

import java.time.LocalDateTime;

public class Restaurant extends BaseEntity {
    private String name;
    private String category;
    private Integer rating;
    private String phone_number;
    private String location;
    private String image_path;


    public Restaurant( String name, String category, Integer rating,
                       String phone_number, String location,
                       LocalDateTime opening_date, LocalDateTime closing_date) {

        this.name = name;
        this.category = category;
        this.rating = rating;
        this.phone_number = phone_number;
        this.location = location;

    }

    public Restaurant() {

    }

    public Restaurant(int restaurantId) {
        this.setId(restaurantId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

}