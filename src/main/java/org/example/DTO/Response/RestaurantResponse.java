package org.example.DTO.Response;

import org.example.BaseEntity;

import java.awt.event.ActionListener;

public class RestaurantResponse extends BaseEntity {
    private String name;
    private String category;
    private Integer rating;
    private String phone_number;
    private String location;
    private String image_path;


    public RestaurantResponse(int id, String name, String category, Integer rating,
                              String phone_number, String location, String image_path) {
        this.setId(id);
        this.name = name;
        this.category = category;
        this.rating = rating;
        this.phone_number = phone_number;
        this.location = location;
        this.image_path = image_path;

    }

    public RestaurantResponse(int restaurantId) {
        this.setId(restaurantId);
    }

    public RestaurantResponse(RestaurantResponse restaurantResponse) {
        this.setId(restaurantResponse.getId());
        this.setName(restaurantResponse.getName());
        this.setCategory(restaurantResponse.getCategory());
        this.setRating(restaurantResponse.getRating());
        this.setPhone_number(restaurantResponse.getPhone_number());
        this.setLocation(restaurantResponse.getLocation());
        this.setImage_path(restaurantResponse.getImage_path());
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

    @Override
    public String toString() {
        return "RestaurantResponse{" +
                "id = " +getId()+
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", rating=" + rating +
                ", phone_number='" + phone_number + '\'' +
                ", location='" + location + '\'' +
                ", image_path='" + image_path + '\'' +
                '}';
    }
}