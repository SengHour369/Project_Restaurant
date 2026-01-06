package org.example.Service.ServiceHandler;

import org.example.Exception.MessageException;


import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.example.Database.DatabaseConnection.getConnection;

public class UserServiceHandler extends Component {
    public boolean usernameExists(String username) {
        String sql = "SELECT id FROM users WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                JOptionPane.showInternalMessageDialog(null, "Username is has already");
                throw new MessageException("Username is has already");

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (MessageException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    public boolean EmailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
              //  System.out.println("dmdjf"+  rs.next());
                JOptionPane.showInternalMessageDialog(null, "Email is has already");
                throw new MessageException("Email is has already");


            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (MessageException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
   public boolean phone_numberExists(String phone)  {
        String sql = "SELECT id  FROM users WHERE phone_number  =  ?";
       try( Connection con   = getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
           ps.setString(1,phone);
           ResultSet rs = ps.executeQuery();
           if(rs.next()){
               JOptionPane.showMessageDialog(null, "Phone number is has already");
               throw new MessageException("Phone number is has already");
           }
       }catch(SQLException e){
           e.printStackTrace();
       }
       catch(MessageException e){
          throw new RuntimeException(e);
       }
       return false;
   }


    public Boolean HasValidEmail(String email) throws MessageException {
       if(email != null){
        return true;
       }
       JOptionPane.showMessageDialog(null, "Please enter valid email");
       throw new MessageException(" Email is not blank");
   }
   public Boolean HasValidUsername(String username) throws MessageException {
        if(!username.isEmpty()){
            return true;
        }
        JOptionPane.showMessageDialog(null, "Please enter valid username");
        throw new MessageException("Username is not blank");
   }
   public Boolean HasValidPhone_number(String Phone_number) throws MessageException {
       if(!Phone_number.isEmpty()){
           return true;
       }
       JOptionPane.showMessageDialog(null, "Phone number is not blank");
       throw new MessageException("Phone number is not blank");
   }
}
