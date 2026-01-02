package org.example.UI;

import org.example.DTO.Request.OrderRequest;
import org.example.DTO.Response.OrderResponse;
import org.example.Model.Payment;
import org.example.Model.Restaurant;
import org.example.Model.User;
import org.example.Service.ServiceImplement.ServiceOrderImp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.util.List;

public class OrderPanel extends JPanel {

    JTextField txtId = new JTextField();
    JTextField txtTotal = new JTextField();
    JTextField txtUserId = new JTextField();
    JTextField txtRestaurantId = new JTextField();
    JTextField txtPaymentId = new JTextField();

    DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID","Order Date","Total Price","User ID","Restaurant ID","Payment ID"}, 0
    );
    JTable table = new JTable(model);
    ServiceOrderImp service = new ServiceOrderImp();

    public OrderPanel() {
        setLayout(null);

        addLabel("Order ID",10,10); addField(txtId,120,10);
        addLabel("Total Price",10,40); addField(txtTotal,120,40);
        addLabel("User ID",10,70); addField(txtUserId,120,70);
        addLabel("Restaurant ID",10,100); addField(txtRestaurantId,120,100);
        addLabel("Payment ID",10,130); addField(txtPaymentId,120,130);

        JButton btnAdd = new JButton("Create");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnLoad = new JButton("Load All");

        btnAdd.setBounds(10,180,100,30);
        btnUpdate.setBounds(120,180,100,30);
        btnDelete.setBounds(230,180,100,30);
        btnLoad.setBounds(340,180,100,30);

        add(btnAdd); add(btnUpdate); add(btnDelete); add(btnLoad);

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(450,10,520,430);
        add(sp);

        btnAdd.addActionListener(e -> { service.createOrder(readForm()); loadTable(); });
        btnUpdate.addActionListener(e -> { service.updateOrder(Integer.parseInt(txtId.getText()), readForm()); loadTable(); });
        btnDelete.addActionListener(e -> { service.deleteOrder(Integer.parseInt(txtId.getText())); loadTable(); });
        btnLoad.addActionListener(e -> loadTable());
    }

    private OrderRequest readForm() {
        User user = new User(); user.setId(Integer.parseInt(txtUserId.getText()));
        Restaurant restaurant = new Restaurant(); restaurant.setId(Integer.parseInt(txtRestaurantId.getText()));
        Payment payment = new Payment(); payment.setId(Integer.parseInt(txtPaymentId.getText()));

        return new OrderRequest(
                LocalDateTime.now(),
                Double.parseDouble(txtTotal.getText()),
                user,
                restaurant,
                null,
                payment
        );
    }

    private void loadTable() {
        model.setRowCount(0);
        List<OrderResponse> orders = service.findAllOrders();
        for (OrderResponse o : orders) {
            model.addRow(new Object[]{
                    o.getUser().getId(), // replace with actual orderId if available
                    o.getOrderDate(),
                    o.getTotalPrice(),
                    o.getUser().getId(),
                    o.getRestaurant().getId(),
                    o.getPayment().getId()
            });
        }
    }

    private void addLabel(String t,int x,int y){
        JLabel l=new JLabel(t);
        l.setBounds(x,y,100,25);
        add(l);
    }

    private void addField(JTextField f,int x,int y){
        f.setBounds(x,y,200,25);
        add(f);
    }
}
