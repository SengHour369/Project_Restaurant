package org.example.Service.ServiceImplement;

import org.example.Database.DatabaseConnection;
import org.example.DTO.Request.RestaurantRequest;
import org.example.DTO.Response.RestaurantResponse;

import org.example.Exception.MessageException;
import org.example.Service.ServiceHandler.RestaurantServiceHandler;
import org.example.Service.ServiceRestaurant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRestaurantImp implements ServiceRestaurant {
    RestaurantServiceHandler restaurantServiceHandler =  new RestaurantServiceHandler();
    @Override
    public void CreateRestaurant(RestaurantRequest r) throws MessageException {
        restaurantServiceHandler.hasValidCategory(r.getCategory());
        restaurantServiceHandler.hasValidPhone(r.getPhone_number());
        restaurantServiceHandler.CategoryExists(r.getCategory());
        restaurantServiceHandler.phoneExist(r.getPhone_number());

        String sql = """
        INSERT INTO restaurants
        ( name, category, rating, phone_number, location, image_path)
        VALUES ( ?, ?, ?, ?, ?, ?)
    """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getName());
            ps.setString(2, r.getCategory());
            ps.setInt(3, r.getRating());
            ps.setString(4, r.getPhone_number());
            ps.setString(5, r.getLocation());
            ps.setString(6, r.getImage_path());
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
            name=?, category=?, rating=?, phone_number=?, location=?, image_path=?
            WHERE id=?
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, r.getName());
            ps.setString(2, r.getCategory());
            ps.setInt(3, r.getRating());
            ps.setString(4, r.getPhone_number());
            ps.setString(5, r.getLocation());
            ps.setString(6, r.getImage_path());
            ps.setInt(7, id);

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
                rs.getString("location"),
                rs.getString("image_path")
        );
    }
}