package org.example.Service.ServiceImplement;

import org.example.Database.DatabaseConnection;
import org.example.DTO.Request.MenuItemRequest;
import org.example.DTO.Response.MenuItemResponse;
import org.example.Service.ServiceMenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMenuItemImp implements ServiceMenuItem {

    @Override
    public MenuItemResponse createMenuItem(MenuItemRequest req) {
        String sql = "INSERT INTO menu_items (restaurant_id, code, name, description, active, image_path, is_veg) VALUES (?,?,?,?,?,?,?) RETURNING id";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, req.getRestaurant());
            ps.setString(2, req.getPrice());
            ps.setString(3, req.getName());
            ps.setString(4, req.getDescription());
            ps.setBoolean(5, req.getActive());
            ps.setString(6, req.getImagePath());
            ps.setBoolean(7, req.getIsVeg() != null ? req.getIsVeg() : true);

            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                int id = rs.getInt("id");
                MenuItemResponse m = new MenuItemResponse(req.getRestaurant(), req.getPrice(), req.getName(), req.getDescription(), req.getActive());
                m.setId(id);
                m.setImagePath(req.getImagePath());
                m.setIsVeg(req.getIsVeg());
                return m;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public MenuItemResponse updateMenuItem(MenuItemRequest req) {
        String sql = "UPDATE menu_items SET restaurant_id=?, code=?, name=?, description=?, active=?, image_path=?, is_veg=? WHERE id=?";
        try(Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, req.getRestaurant());
            ps.setString(2, req.getPrice());
            ps.setString(3, req.getName());
            ps.setString(4, req.getDescription());
            ps.setBoolean(5, req.getActive());
            ps.setString(6, req.getImagePath());
            ps.setBoolean(7, req.getIsVeg() != null ? req.getIsVeg() : true);
            ps.setInt(8, req.getId());

            ps.executeUpdate();
            MenuItemResponse m = new MenuItemResponse(req.getRestaurant(), req.getPrice(), req.getName(), req.getDescription(), req.getActive());
            m.setId(req.getId());
            m.setImagePath(req.getImagePath());
            m.setIsVeg(req.getIsVeg());
            return m;

        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteMenuItem(MenuItemRequest req) {
        String sql = "DELETE FROM menu_items WHERE id=?";
        try(Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, req.getId());
            ps.executeUpdate();

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public MenuItemResponse getFindByIdMenuItem(int req) {
        String sql = "SELECT * FROM menu_items WHERE id=?";
        try(Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, req);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                MenuItemResponse m = new MenuItemResponse(rs.getInt("restaurant_id"), rs.getString("code"), rs.getString("name"), rs.getString("description"), rs.getBoolean("active"));
                m.setId(rs.getInt("id"));
                m.setImagePath(rs.getString("image_path"));
                m.setIsVeg(rs.getBoolean("is_veg"));
                return m;
            }

        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<MenuItemResponse> getAllMenuItems() {
        List<MenuItemResponse> list = new ArrayList<>();
        String sql = "SELECT * FROM menu_items";
        try(Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()){


                MenuItemResponse m = new MenuItemResponse( rs.getInt("restaurant_id"), rs.getString("code"), rs.getString("name"), rs.getString("description"), rs.getBoolean("active"));
                m.setId(rs.getInt("id"));
                m.setImagePath(rs.getString("image_path"));
                m.setIsVeg(rs.getBoolean("is_veg"));
                list.add(m);
            }

        } catch(Exception e){
            e.printStackTrace();
        }
        return list;
    }
}