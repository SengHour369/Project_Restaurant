package org.example.DTO.Request;

public class UserRequest {
    private String name;
    private String gender;
    private String dateOfBirth;
    private String phone;
    private String address;
    private String email;
    private String password;
    private String status;
    private String image_path;

    public UserRequest(String name, String gender, String dateOfBirth,
                       String phone, String address,
                       String email, String password, String status, String image_path) {
        this.name = name;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.password = password;
        this.status = status;
        this.image_path = image_path;
    }

    public String getName() { return name; }
    public String getGender() { return gender; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getStatus() { return status; }
    public String getImage_path() { return image_path; }
}