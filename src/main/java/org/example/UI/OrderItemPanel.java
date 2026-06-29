package org.example.UI;

import org.example.DTO.Request.OrderItemRequest;
import org.example.DTO.Response.MenuItemResponse;
import org.example.Service.ServiceImplement.ServiceMenuItemImp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class OrderItemPanel extends JPanel {

    private final ServiceMenuItemImp menuService = new ServiceMenuItemImp();

    private final DefaultTableModel menuModel =
            new DefaultTableModel(new String[]{"ID","Name","Price"},0);
    private final JTable tblMenu = new JTable(menuModel);

    private final DefaultTableModel orderModel =
            new DefaultTableModel(new String[]{"Menu","Qty","Price","Total"},0);
    private final JTable tblOrderItems = new JTable(orderModel);

    private final JTextField txtQuantity = new JTextField();
    private final JLabel lblTotal = new JLabel("0.00");

    private final List<OrderItemRequest> tempItems = new ArrayList<>();

    public OrderItemPanel() {
        setLayout(null);

        // ===== MENU TABLE =====
        JScrollPane spMenu = new JScrollPane(tblMenu);
        spMenu.setBounds(10,10,400,200);
        add(spMenu);

        // ===== ORDER TABLE =====
        JScrollPane spOrder = new JScrollPane(tblOrderItems);
        spOrder.setBounds(10,260,500,200);
        add(spOrder);

        // ===== FORM =====
        JLabel lbQty = new JLabel("Quantity:");
        lbQty.setBounds(430,20,80,25);
        add(lbQty);

        txtQuantity.setBounds(510,20,80,25);
        add(txtQuantity);

        JButton btnAdd = new JButton("Add");
        btnAdd.setBounds(430,60,160,30);
        add(btnAdd);

        JLabel lbTotal = new JLabel("Total:");
        lbTotal.setBounds(430,110,80,25);
        add(lbTotal);

        lblTotal.setBounds(510,110,100,25);
        add(lblTotal);

        loadMenuItems();

        // ===== EVENTS =====
        btnAdd.addActionListener(e -> addItem());
    }

    // ================= LOGIC =================

    private void addItem(){
        int row = tblMenu.getSelectedRow();
        if(row < 0) return;

        int qty = Integer.parseInt(txtQuantity.getText());
        int menuId = (int) menuModel.getValueAt(row,0);
        String name = menuModel.getValueAt(row,1).toString();
        double price = Double.parseDouble(menuModel.getValueAt(row,2).toString());

        OrderItemRequest item = new OrderItemRequest();
        item.setMenuItemId(menuId);
        item.setQuantity(qty);
        item.setPrice(price);

        tempItems.add(item);

        orderModel.addRow(new Object[]{
                name, qty, price, qty * price
        });

        updateTotal();
        txtQuantity.setText("");
    }

    private void updateTotal(){
        double total = 0;
        for(OrderItemRequest i : tempItems){
            total += i.getPrice() * i.getQuantity();
        }
        lblTotal.setText(String.valueOf(total));
    }

    private void loadMenuItems(){
        menuModel.setRowCount(0);
        for(MenuItemResponse m : menuService.getAllMenuItems()){
            menuModel.addRow(new Object[]{
                    m.getId(), m.getName(), m.getPrice()
            });
        }
    }

    // ================= ACCESS =================
    public List<OrderItemRequest> getOrderItems(){
        return tempItems;
    }

    public double getTotal(){
        return Double.parseDouble(lblTotal.getText());
    }
}