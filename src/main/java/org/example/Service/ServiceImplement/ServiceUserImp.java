package org.example.Service.ServiceImplement;

import org.example.Database.DatabaseConnection;
import org.example.DTO.Request.UserRequest;
import org.example.DTO.Response.UserResponse;
import org.example.Service.ServiceUser;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUserImp implements ServiceUser {

    @Override
    public void create(UserRequest r) {
        String sql = """
            INSERT INTO users
            (name, gender, date_of_birth, phone_number, address, email, password, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, r.getName());
            ps.setString(2, r.getGender());
            ps.setString(3, r.getDateOfBirth());
            ps.setString(4, r.getPhone());
            ps.setString(5, r.getAddress());
            ps.setString(6, r.getEmail());
            ps.setString(7, r.getPassword());
            ps.setString(8, r.getStatus());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(int id, UserRequest r) {
        String sql = """
            UPDATE users SET
            name=?, gender=?, date_of_birth=?, phone_number=?,
            address=?, email=?, password=?, status=?
            WHERE id=?
        """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, r.getName());
            ps.setString(2, r.getGender());
            ps.setString(3, r.getDateOfBirth());
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
    public UserResponse login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email=? AND password=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return map(rs);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
