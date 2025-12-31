package org.example.Service;

import org.example.DTO.Request.UserRequest;
import org.example.DTO.Response.UserResponse;

import java.util.List;
public interface ServiceUser {
    UserResponse CreateUser(UserRequest userRequest);
    UserResponse FindByUserId(UserRequest userRequest);
    UserResponse UpdateUser(UserRequest userRequest);
    void DeleteUser(UserRequest userRequest);
    List<UserResponse> findAllUsers();

}
