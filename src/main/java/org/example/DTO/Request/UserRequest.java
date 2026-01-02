package org.example.DTO.Request;

public class UserRequest {
    private String name;
    private String gender;
    private String data_of_birth;
    private String phone_number;
    private String address;
    private String email;
    private String status;

    public UserRequest(String name, String gender, String dob,
                       String phone, String address,
                       String email, String status) {
        this.name = name;
        this.gender = gender;
        this.data_of_birth = dob;
        this.phone_number = phone;
        this.address = address;
        this.email = email;
        this.status = status;
    }

    // getters
    public String getName() { return name; }
    public String getGender() { return gender; }
    public String getData_of_birth() { return data_of_birth; }
    public String getPhone_number() { return phone_number; }
    public String getAddress() { return address; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }
}
