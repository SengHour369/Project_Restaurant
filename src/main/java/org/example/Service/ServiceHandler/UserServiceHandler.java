package org.example.Service.ServiceHandler;

import org.example.DTO.Request.UserRequest;
import org.example.DTO.Response.UserResponse;
import org.example.Exception.MessageException;
import org.example.Model.User;

import java.util.Optional;

public class UserServiceHandler {
   public UserResponse ConversionUserToUserResponse(User user){
        UserResponse userResponse = new UserResponse();
        userResponse.setStatus(user.getStatus());
        userResponse.setAddress(user.getAddress());
        userResponse.setEmail(user.getEmail());
        userResponse.setData_of_birth(user.getData_of_birth());
        userResponse.setPhone_number(user.getPhone_number());
        userResponse.setName(user.getUsername());
        return userResponse;
    }
    public User ConversionUserRequestToUser(UserRequest userRequest){
        User user = new User();
        user.setAddress(userRequest.getAddress());
        user.setEmail(userRequest.getEmail());
        user.setData_of_birth(userRequest.getData_of_birth());
        user.setPhone_number(userRequest.getPhone_number());
        user.setUsername(userRequest.getName());
        return user;
    }
   public Boolean HasValidEmail(String email) throws MessageException {
       if(email != null){
        return true;
       }
       throw new MessageException(" Email is not blank");
   }
   public Boolean HasValidUsername(String username) throws MessageException {
        if(username != null){
            return true;
        }
        throw new MessageException("Username is not blank");
   }
   public Boolean HasValidPhone_number(String Phone_number) throws MessageException {
       if(Phone_number != null){
           return true;
       }
       throw new MessageException("Phone number is not blank");
   }
}
