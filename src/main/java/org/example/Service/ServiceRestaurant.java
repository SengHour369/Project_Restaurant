package org.example.Service;

import org.example.DTO.Request.RestaurantRequest;
import org.example.DTO.Response.RestaurantResponse;

import java.util.List;

public interface ServiceRestaurant {
    RestaurantResponse CreateRestaurant(RestaurantRequest restaurantRequest);
    List<RestaurantResponse> findAllRestaurants();
    RestaurantResponse findRestaurantById(RestaurantRequest restaurantRequest);
    RestaurantResponse updateRestaurant(RestaurantRequest restaurantRequest);
    void deleteRestaurant(RestaurantRequest restaurantRequest);
}
