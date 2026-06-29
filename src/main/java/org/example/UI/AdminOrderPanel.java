package org.example.UI;



import org.example.DTO.Response.OrderResponse;
import org.example.Service.ServiceImplement.ServiceOrderImp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class AdminOrderPanel extends JPanel {

    private ServiceOrderImp orderService = new ServiceOrderImp();
    private DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{" Order ID", " Customer", " Restaurant", " Total", " Payment"}, 0
    );
    private JTable orderTable = new JTable(tableModel);

    public AdminOrderPanel() {
        setupPanel();
        loadOrderData();
    }

    private void setupPanel() {
        setLayout(null);
        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBounds(10, 10, 800, 400);
        add(scrollPane);
    }

    private void loadOrderData() {
        tableModel.setRowCount(0);
        List<OrderResponse> orders = orderService.findAllOrders();

        for (OrderResponse order : orders) {
            tableModel.addRow(new Object[]{
                    order.getId(),
                    order.getUser().getName(),
                    order.getRestaurant().getName(),
                    String.format("$%.2f", order.getTotalPrice()),
                    order.getPayment().getId()
            });
        }
    }
}