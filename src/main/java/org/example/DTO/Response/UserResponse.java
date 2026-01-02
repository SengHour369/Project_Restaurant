package org.example.DTO.Response;

import org.example.BaseEntity;

public class UserResponse extends BaseEntity {

    private String name;
    private String gender;
    private String data_of_birth;
    private String phone_number;
    private String address;
    private String email;
    private String status;

    // setters
    public void setName(String name) { this.name = name; }
    public void setGender(String gender) { this.gender = gender; }
    public void setData_of_birth(String data_of_birth) { this.data_of_birth = data_of_birth; }
    public void setPhone_number(String phone_number) { this.phone_number = phone_number; }
    public void setAddress(String address) { this.address = address; }
    public void setEmail(String email) { this.email = email; }
    public void setStatus(String status) { this.status = status; }

    // getters

    public String getName() { return name; }
    public String getGender() { return gender; }
    public String getData_of_birth() { return data_of_birth; }
    public String getPhone_number() { return phone_number; }
    public String getAddress() { return address; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + getId() +
                "name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", data_of_birth='" + data_of_birth + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
