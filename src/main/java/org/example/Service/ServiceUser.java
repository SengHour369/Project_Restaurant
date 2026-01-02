package org.example.Service;

import org.example.DTO.Request.UserRequest;
import org.example.DTO.Response.UserResponse;

import java.util.List;

public interface ServiceUser {
    void create(UserRequest request);
    void update(int id, UserRequest request);
    void delete(int id);
    UserResponse findById(int id);
    List<UserResponse> findAll();
}
