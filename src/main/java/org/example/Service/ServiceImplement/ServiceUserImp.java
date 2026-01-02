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
            (name, gender, date_of_birth, phone_number, address, email, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, r.getName());
            ps.setString(2, r.getGender());
            ps.setString(3, r.getData_of_birth());
            ps.setString(4, r.getPhone_number());
            ps.setString(5, r.getAddress());
            ps.setString(6, r.getEmail());
            ps.setString(7, r.getStatus());
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
            address=?, email=?, status=?
            WHERE id=?
        """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, r.getName());
            ps.setString(2, r.getGender());
            ps.setString(3, r.getData_of_birth());
            ps.setString(4, r.getPhone_number());
            ps.setString(5, r.getAddress());
            ps.setString(6, r.getEmail());
            ps.setString(7, r.getStatus());
            ps.setInt(8, id);
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
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     c.prepareStatement("SELECT * FROM users WHERE id=?")) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                UserResponse u = new UserResponse();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setGender(rs.getString("gender"));
                u.setData_of_birth(rs.getString("date_of_birth"));
                u.setPhone_number(rs.getString("phone_number"));
                u.setAddress(rs.getString("address"));
                u.setEmail(rs.getString("email"));
                u.setStatus(rs.getString("status"));
                return u;
            }
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

            while (rs.next()) {
                UserResponse u = new UserResponse();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setGender(rs.getString("gender"));
                u.setData_of_birth(rs.getString("date_of_birth"));
                u.setPhone_number(rs.getString("phone_number"));
                u.setAddress(rs.getString("address"));
                u.setEmail(rs.getString("email"));
                u.setStatus(rs.getString("status"));
                list.add(u);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("ncjdbcf" +list);
        return list;
    }
}
