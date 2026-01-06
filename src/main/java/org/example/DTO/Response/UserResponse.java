package org.example.DTO.Response;

public class UserResponse {
    private int id;
    private String name;
    private String gender;
    private String dateOfBirth;
    private String phone;
    private String address;
    private String email;
    private String status;

    public UserResponse(UserResponse user) {
        this.id = user.id;
        this.name = user.name;
        this.gender = user.gender;
    }

    public UserResponse() {

    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
