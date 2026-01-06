package org.example.Model;


import org.example.BaseEntity;

public class User extends BaseEntity {
    private java.lang.String username;
    private String gender;
    private java.lang.String data_of_birth;
    private java.lang.String phone_number;
    private java.lang.String Address;
    private java.lang.String email;
    private java.lang.String Status;

    public User(java.lang.String name, String gender, java.lang.String data_of_birth,
                java.lang.String phone_number, java.lang.String address, java.lang.String email, java.lang.String status) {
        this.username = name;
        this.gender = gender;
        this.data_of_birth = data_of_birth;
        this.phone_number = phone_number;
        Address = address;
        this.email = email;
        Status = status;
    }

    public User() {
    }

    public User(int i) {
        this.setId(i);
    }

    public User(int id, String name) {
        this.setId(id);
        this.setUsername(name);

    }

    public java.lang.String getUsername() {
        return username;
    }

    public String getGender() {
        return gender;
    }

    public java.lang.String getData_of_birth() {
        return data_of_birth;
    }

    public java.lang.String getPhone_number() {
        return phone_number;
    }

    public java.lang.String getAddress() {
        return Address;
    }

    public java.lang.String getEmail() {
        return email;
    }

    public java.lang.String getStatus() {
        return Status;
    }

    public void setUsername(java.lang.String username) {
        this.username = username;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setData_of_birth(java.lang.String data_of_birth) {
        this.data_of_birth = data_of_birth;
    }

    public void setPhone_number(java.lang.String phone_number) {
        this.phone_number = phone_number;
    }

    public void setAddress(java.lang.String address) {
        Address = address;
    }

    public void setEmail(java.lang.String email) {
        this.email = email;
    }

    public void setStatus(java.lang.String status) {
        Status = status;
    }


    @Override
    public java.lang.String toString() {
        return "User{" +
                "name='" + username + '\'' +
                ", gender='" + gender + '\'' +
                ", data_of_birth='" + data_of_birth + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", Address='" + Address + '\'' +
                ", email='" + email + '\'' +
                ", Status='" + Status + '\'' +
                '}';
    }
}
