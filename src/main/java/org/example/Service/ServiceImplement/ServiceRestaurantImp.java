package org.example.Service.ServiceImplement;

import org.example.Database.DatabaseConnection;
import org.example.DTO.Request.RestaurantRequest;
import org.example.DTO.Response.RestaurantResponse;

import org.example.Service.ServiceRestaurant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRestaurantImp implements ServiceRestaurant {

    @Override
    public void CreateRestaurant(RestaurantRequest r) {
        String sql = """
        INSERT INTO restaurants
        (code, name, category, rating, phone_number, location)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, r.getName());
            ps.setString(2, r.getCategory());
            ps.setInt(3, r.getRating());
            ps.setString(4, r.getPhone_number());
            ps.setString(5, r.getLocation());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<RestaurantResponse> findAllRestaurants() {
        List<RestaurantResponse> list = new ArrayList<>();
        String sql = "SELECT * FROM restaurants";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapToResponse(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
      System.out.println("s dnfbj" + list);
        return list;
    }

    @Override
    public RestaurantResponse findRestaurantById(int id) {
        String sql = "SELECT * FROM restaurants WHERE id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapToResponse(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public RestaurantResponse updateRestaurant(RestaurantRequest r, int id) {
        String sql = """
            UPDATE restaurants SET
            code=?, name=?, category=?, rating=?, phone_number=?, location=?
            WHERE id=?
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, r.getName());
            ps.setString(2, r.getCategory());
            ps.setInt(3, r.getRating());
            ps.setString(4, r.getPhone_number());
            ps.setString(5, r.getLocation());

            ps.setInt(9, id);

            ps.executeUpdate();
            return findRestaurantById(id);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void deleteRestaurant(int id) {
        String sql = "DELETE FROM restaurants WHERE id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private RestaurantResponse mapToResponse(ResultSet rs) throws SQLException {
        return new RestaurantResponse(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getInt("rating"),
                rs.getString("phone_number"),
                rs.getString("location")
        );
    }
}
