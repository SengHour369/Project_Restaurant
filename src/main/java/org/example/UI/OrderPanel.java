package org.example.UI;

import org.example.DTO.Request.OrderItemRequest;
import org.example.DTO.Request.OrderRequest;
import org.example.DTO.Response.MenuItemResponse;
import org.example.DTO.Response.OrderResponse;
import org.example.DTO.Response.RestaurantResponse;
import org.example.DTO.Response.UserResponse;
import org.example.Exception.MessageException;
import org.example.Model.Payment;
import org.example.Model.Restaurant;
import org.example.Model.User;
import org.example.Service.ServiceImplement.ServiceMenuItemImp;
import org.example.Service.ServiceImplement.ServiceOrderImp;
import org.example.Service.ServiceImplement.ServiceRestaurantImp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderPanel extends JPanel {

    private JTextField txtRestaurantId = new JTextField();
    private JTextField txtMenuItemId = new JTextField();
    private JTextField txtQty = new JTextField();
    private JTextField txtTotal = new JTextField();

    private DefaultTableModel itemModel = new DefaultTableModel(
            new String[]{"Menu","Qty","Price","Subtotal"},0
    );
    private JTable itemTable = new JTable(itemModel);

    private DefaultTableModel orderModel = new DefaultTableModel(
            new String[]{"Order ID","Restaurant","Total"},0
    );
    private JTable orderTable = new JTable(orderModel);

    private List<OrderItemRequest> orderItems = new ArrayList<>();

    private User currentUser;
    private ServiceOrderImp orderService = new ServiceOrderImp();
    private ServiceMenuItemImp menuService = new ServiceMenuItemImp();
    private ServiceRestaurantImp restaurantService = new ServiceRestaurantImp();

    public OrderPanel(UserResponse user) {
        setLayout(null);

        currentUser = new User();
        currentUser.setId(user.getId());

        // --- Restaurant ID Input ---
        addLabel("Restaurant ID",10,10);
        txtRestaurantId.setBounds(120,10,200,25);
        add(txtRestaurantId);

        // --- Menu Item ID Input ---
        addLabel("MenuItem ID",10,40);
        txtMenuItemId.setBounds(120,40,200,25);
        add(txtMenuItemId);

        // --- Quantity ---
        addLabel("Qty",10,70);
        txtQty.setBounds(120,70,80,25);
        add(txtQty);

        JButton btnAdd = new JButton("Add");
        btnAdd.setBounds(210,70,70,25);
        add(btnAdd);

        // --- Order Items Table ---
        JScrollPane spItem = new JScrollPane(itemTable);
        spItem.setBounds(10,110,400,150);
        add(spItem);

        // --- Total ---
        addLabel("Total",10,270);
        txtTotal.setBounds(120,270,200,25);
        txtTotal.setEditable(false);
        add(txtTotal);

        // --- Create Order Button ---
        JButton btnCreate = new JButton("Create Order");
        btnCreate.setBounds(10,310,150,30);
        add(btnCreate);

        // --- Existing Orders Table ---
        JScrollPane spOrder = new JScrollPane(orderTable);
        spOrder.setBounds(430,10,520,330);
        add(spOrder);

        loadOrders();

        // --- Button Actions ---
        btnAdd.addActionListener(e -> addItem());
        btnCreate.addActionListener(e -> {
            try {
                paymentAndCreate();
            } catch (MessageException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
    }

    // ================= LOGIC =================

    private void addItem(){
        try {
            int menuId = Integer.parseInt(txtMenuItemId.getText());
            int qty = Integer.parseInt(txtQty.getText());

            MenuItemResponse menu = menuService.getAllMenuItems().stream()
                    .filter(m -> m.getId() == menuId)
                    .findFirst()
                    .orElse(null);

            if(menu == null){
                JOptionPane.showMessageDialog(this,"Menu item not found!");
                return;
            }

            double price = Double.parseDouble(menu.getPrice());

            orderItems.add(new OrderItemRequest(null, menuId, qty, price));
            itemModel.addRow(new Object[]{menu.getName(), qty, price, price*qty});
            updateTotal();
            txtMenuItemId.setText("");
            txtQty.setText("");

        } catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this,"Invalid number input!");
        }
    }

    private void updateTotal(){
        double total = orderItems.stream()
                .mapToDouble(i -> i.getPrice()*i.getQuantity()).sum();
        txtTotal.setText(String.valueOf(total));
    }

    private void paymentAndCreate() throws MessageException {
        if(orderItems.isEmpty()){
            JOptionPane.showMessageDialog(this,"No items in order");
            return;
        }

        try {
            int restaurantId = Integer.parseInt(txtRestaurantId.getText());

            RestaurantResponse restaurant = restaurantService.findAllRestaurants().stream()
                    .filter(r -> r.getId() == restaurantId)
                    .findFirst()
                    .orElse(null);

            if(restaurant == null){
                JOptionPane.showMessageDialog(this,"Restaurant not found!");
                return;
            }

            double total = Double.parseDouble(txtTotal.getText());
            JTextField txtPay = new JTextField();

            Object[] msg = {"Total: "+total,"Payment:",txtPay};
            int option = JOptionPane.showConfirmDialog(this,msg,"Payment",JOptionPane.OK_CANCEL_OPTION);
            if(option != JOptionPane.OK_OPTION) return;

            double pay = Double.parseDouble(txtPay.getText());
            if(pay < total){
                JOptionPane.showMessageDialog(this,"Not enough payment");
                return;
            }

            Payment payment = new Payment();
            payment.setAmount(pay);
            payment.setType("CASH");

            OrderRequest req = new OrderRequest(
                    java.time.LocalDateTime.now(),
                    total,
                    currentUser,  // User object
                    restaurant,   // Restaurant ob11ject
                    orderItems,
                    payment
            );


            orderService.createOrder(req);
            JOptionPane.showMessageDialog(this,"Order Success");

            orderItems.clear();
            itemModel.setRowCount(0);
            txtTotal.setText("");
            txtRestaurantId.setText("");
            txtMenuItemId.setText("");
            loadOrders();

        } catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this,"Invalid ID input!");
        }
    }

    private void loadOrders(){
        orderModel.setRowCount(0);
        for(OrderResponse o : orderService.findAllOrders()){
            orderModel.addRow(new Object[]{
                    o.getId(),
                    o.getRestaurant().getName(),
                    o.getTotalPrice()
            });
        }
    }

    private void addLabel(String t,int x,int y){
        JLabel l=new JLabel(t);
        l.setBounds(x,y,100,25); add(l);
    }
}
