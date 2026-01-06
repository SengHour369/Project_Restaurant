package org.example.Service.ServiceHandler;

import org.example.Exception.MessageException;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.example.Database.DatabaseConnection.getConnection;

public class RestaurantServiceHandler {
    public boolean CategoryExists(String category){
        String sql = "SELECT id FROM restaurants WHERE category = ?";
       try( Connection con = getConnection();
        PreparedStatement post = con.prepareStatement(sql)){
           post.setString(1, category);
           ResultSet rs = post.executeQuery();
           if(rs.next()){
               JOptionPane.showMessageDialog(null, "Category already exists");
               throw new MessageException("Category already exists");
           }
       }catch (SQLException e)
           {
           e.printStackTrace();
           } catch (MessageException e) {
           throw new RuntimeException(e);
       }
        return false;
    }
    public boolean phoneExist(String phone ){
        String sql = "SELECT ID FROM restaurants WHERE phone_number = ?";
        try(Connection con = getConnection();
       PreparedStatement rs = con.prepareStatement(sql)){
            rs.setString(1,phone);
            ResultSet rs1 = rs.executeQuery();
            if(rs1.next()){
                JOptionPane.showMessageDialog(null, "Phone already exists");
                throw new MessageException("Phone already exists");
            }

        }catch(SQLException e){
            e.printStackTrace();
    } catch (MessageException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    public boolean hasValidPhone(String phone ) throws MessageException {
        if(!phone.isEmpty()){
            return true;
        }
        JOptionPane.showMessageDialog(null, "Phone number is not blank");
         throw new MessageException("Phone number is not blank");
    }
    public boolean hasValidCategory(String category) throws MessageException {
        if(!category.isEmpty()){
            return true;
        }
        JOptionPane.showMessageDialog(null, "Category is not blank");
        throw new MessageException("Category is not blank");
    }
}
