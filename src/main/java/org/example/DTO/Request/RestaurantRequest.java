package org.example.DTO.Request;

import java.time.LocalDateTime;

public class RestaurantRequest {
    private String code;
    private String name;
    private String category;
    private Integer rating;
    private String phone_number;
    private String location;
    private LocalDateTime opening_date;
    private LocalDateTime closing_date;

    public RestaurantRequest(String code, String name, String category, Integer rating,
                      String phone_number, String location,
                      LocalDateTime opening_date, LocalDateTime closing_date) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.rating = rating;
        this.phone_number = phone_number;
        this.location = location;
        this.opening_date = opening_date;
        this.closing_date = closing_date;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public LocalDateTime getOpening_date() {
        return opening_date;
    }

    public void setOpening_date(LocalDateTime opening_date) {
        this.opening_date = opening_date;
    }

    public LocalDateTime getClosing_date() {
        return closing_date;
    }

    public void setClosing_date(LocalDateTime closing_date) {
        this.closing_date = closing_date;
    }
}
