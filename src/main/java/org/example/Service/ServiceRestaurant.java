package org.example.Service;

import org.example.DTO.Request.RestaurantRequest;
import org.example.DTO.Response.RestaurantResponse;
import org.example.Exception.MessageException;

import java.util.List;

public interface ServiceRestaurant {
    void CreateRestaurant(RestaurantRequest restaurantRequest) throws MessageException;
    List<RestaurantResponse> findAllRestaurants();
    RestaurantResponse findRestaurantById(int id);
    RestaurantResponse updateRestaurant(RestaurantRequest restaurantRequest,int id);
    void deleteRestaurant(int id);
}
