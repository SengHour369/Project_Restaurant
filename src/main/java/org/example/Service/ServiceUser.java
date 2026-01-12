package org.example.Service;

import org.example.DTO.Request.UserRequest;
import org.example.DTO.Response.UserResponse;
import org.example.Exception.MessageException;

import java.util.List;

public interface ServiceUser {
    void create(UserRequest r) throws MessageException;
    void update(int id, UserRequest r);
    void delete(int id);
    UserResponse findById(int id);
    List<UserResponse> findAll();
    UserResponse login(String email, String password);
}
