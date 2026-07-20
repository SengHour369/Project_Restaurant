package org.example.Service.ServiceImplement;

import org.example.Database.DatabaseConnection;
import org.example.DTO.Request.UserRequest;
import org.example.DTO.Response.UserResponse;
import org.example.Exception.MessageException;
import org.example.Service.ServiceHandler.UserServiceHandler;
import org.example.Service.ServiceUser;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUserImp implements ServiceUser {
    UserServiceHandler userServiceHandler =  new UserServiceHandler();
    @Override
    public void create(UserRequest r) throws MessageException {

//        userServiceHandler.usernameExists(r.getName());
//        userServiceHandler.phone_numberExists(r.getPhone());
//        userServiceHandler.EmailExists(r.getEmail());
//        userServiceHandler.HasValidUsername(r.getName());
        String sql = """
            INSERT INTO users
            (name, gender, date_of_birth, phone_number, address, email, password_hash, status)
            VALUES (?, ?::user_gender, ?::date, ?, ?, ?, ?, ?::user_status)
        """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, r.getName());
            ps.setString(2, blankToNull(r.getGender()));
            ps.setString(3, blankToNull(r.getDateOfBirth()));
            ps.setString(4, r.getPhone());
            ps.setString(5, r.getAddress());
            ps.setString(6, r.getEmail());
            ps.setString(7, r.getPassword());
            ps.setString(8, r.getStatus());
            ps.executeUpdate();

        } catch (Exception e) {
            throw new MessageException("Could not create user: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(int id, UserRequest r) {
        String sql = """
            UPDATE users SET
            name=?, gender=?::user_gender, date_of_birth=?::date, phone_number=?,
            address=?, email=?, password_hash=?, status=?::user_status
            WHERE id=?
        """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, r.getName());
            ps.setString(2, blankToNull(r.getGender()));
            ps.setString(3, blankToNull(r.getDateOfBirth()));
            ps.setString(4, r.getPhone());
            ps.setString(5, r.getAddress());
            ps.setString(6, r.getEmail());
            ps.setString(7, r.getPassword());
            ps.setString(8, r.getStatus());
            ps.setInt(9, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     c.prepareStatement("DELETE FROM users WHERE id=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserResponse findById(int id) {
        String sql = "SELECT * FROM users WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();


            if (rs.next()) return map(rs);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<UserResponse> findAll() {
        List<UserResponse> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users")) {

            while (rs.next()) list.add(map(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public UserResponse login(String username, String password) {
        String sql = "SELECT * FROM users WHERE email=? AND password_hash=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return map(rs);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private UserResponse map(ResultSet rs) throws SQLException {
        UserResponse u = new UserResponse();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setGender(rs.getString("gender"));
        u.setDateOfBirth(rs.getString("date_of_birth"));
        u.setPhone(rs.getString("phone_number"));
        u.setAddress(rs.getString("address"));
        u.setEmail(rs.getString("email"));
        u.setStatus(rs.getString("status"));
        return u;
    }
}