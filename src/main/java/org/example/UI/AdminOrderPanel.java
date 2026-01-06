package org.example.UI;

import org.example.DTO.Response.OrderResponse;
import org.example.Service.ServiceImplement.ServiceOrderImp;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class AdminOrderPanel extends JPanel {

    private ServiceOrderImp serviceOrder = new ServiceOrderImp();
    private DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Order ID","User","Restaurant","Total","Payment"},0
    );
    private JTable table = new JTable(tableModel);

    public AdminOrderPanel(){
        setLayout(null);
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(10,10,800,400); add(sp);
        loadOrders();
    }

    private void loadOrders(){
        tableModel.setRowCount(0);
        List<OrderResponse> list = serviceOrder.findAllOrders();
        for(OrderResponse o: list){
            tableModel.addRow(new Object[]{
                    o.getId(),
                    o.getUser().getId(),
                    o.getRestaurant().getName(),
                    o.getTotalPrice(),
                    o.getPayment().getAmount()
            });
        }
    }
}
