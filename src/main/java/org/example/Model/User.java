package org.example.Model;

import org.example.Enumeration.Gender;

public class User {
    private String name;
    private Gender gender;
    private String data_of_birth;
    private String phone_number;
    private String Address;
    private String email;
    private String Status;

    public User(String name, Gender gender, String data_of_birth,
                String phone_number, String address, String email, String status) {
        this.name = name;
        this.gender = gender;
        this.data_of_birth = data_of_birth;
        this.phone_number = phone_number;
        Address = address;
        this.email = email;
        Status = status;
    }

    public String getName() {
        return name;
    }

    public Gender getGender() {
        return gender;
    }

    public String getData_of_birth() {
        return data_of_birth;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public String getAddress() {
        return Address;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return Status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setData_of_birth(String data_of_birth) {
        this.data_of_birth = data_of_birth;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(String status) {
        Status = status;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", data_of_birth='" + data_of_birth + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", Address='" + Address + '\'' +
                ", email='" + email + '\'' +
                ", Status='" + Status + '\'' +
                '}';
    }
}
