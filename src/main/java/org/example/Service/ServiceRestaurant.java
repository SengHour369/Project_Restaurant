package org.example.Service;

import org.example.DTO.Request.RestaurantRequest;
import org.example.DTO.Response.RestaurantResponse;

import java.util.List;

public interface ServiceRestaurant {
    void CreateRestaurant(RestaurantRequest restaurantRequest);
    List<RestaurantResponse> findAllRestaurants();
    RestaurantResponse findRestaurantById(int id);
    RestaurantResponse updateRestaurant(RestaurantRequest restaurantRequest,int id);
    void deleteRestaurant(int id);
}
