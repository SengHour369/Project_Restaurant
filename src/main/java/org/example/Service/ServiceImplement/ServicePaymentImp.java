package org.example.Service.ServiceImplement;

import org.example.Database.DatabaseConnection;
import org.example.DTO.Request.PaymentRequest;
import org.example.DTO.Response.PaymentResponse;
import org.example.Service.ServicePayment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePaymentImp implements ServicePayment {

    @Override
    public PaymentResponse createPayment(PaymentRequest p) {
        String sql = "INSERT INTO payments (type, amount) VALUES (?, ?) RETURNING id";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getType());
            ps.setDouble(2, p.getAmount());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                return new PaymentResponse(id, p.getType(), p.getAmount());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public PaymentResponse updatePayment(int id, PaymentRequest p) {
        String sql = "UPDATE payments SET type=?, amount=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getType());
            ps.setDouble(2, p.getAmount());
            ps.setInt(3, id);

            int updated = ps.executeUpdate();
            if (updated > 0) {
                return findPaymentById(id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deletePayment(int id) {
        String sql = "DELETE FROM payments WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public PaymentResponse findPaymentById(int id) {
        String sql = "SELECT * FROM payments WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new PaymentResponse(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getDouble("amount")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<PaymentResponse> findAllPayments() {
        List<PaymentResponse> list = new ArrayList<>();
        String sql = "SELECT * FROM payments";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new PaymentResponse(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getDouble("amount")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
