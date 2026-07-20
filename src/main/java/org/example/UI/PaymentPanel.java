package org.example.UI;

import org.example.DTO.Request.PaymentRequest;
import org.example.DTO.Response.PaymentResponse;
import org.example.Service.ServiceImplement.ServicePaymentImp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Font;
import java.util.List;

public class PaymentPanel extends JPanel {

    JTextField txtId = new JTextField();
    JTextField txtType = new JTextField();
    JTextField txtAmount = new JTextField();

    DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Type", "Amount"}, 0);

    JTable table = new JTable(model);
    ServicePaymentImp service = new ServicePaymentImp();

    public PaymentPanel() {
        setLayout(null);
        setBounds(0,0,800,500);
        setBackground(UITheme.BACKGROUND);

        JLabel title = new JLabel("💳 Payments");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        title.setBounds(10, 10, 300, 30);
        add(title);

        addLabel("ID",10,60); addField(txtId,120,60);
        addLabel("Type",10,90); addField(txtType,120,90);
        addLabel("Amount",10,120); addField(txtAmount,120,120);

        JButton btnAdd = UITheme.createButton("Add", UITheme.SUCCESS);
        JButton btnUpdate = UITheme.createButton("Update", UITheme.PRIMARY);
        JButton btnDelete = UITheme.createButton("Delete", UITheme.SECONDARY);
        JButton btnLoad = UITheme.createButton("Load", UITheme.NEUTRAL);

        btnAdd.setBounds(10,160,80,35);
        btnUpdate.setBounds(100,160,80,35);
        btnDelete.setBounds(190,160,80,35);
        btnLoad.setBounds(280,160,80,35);

        add(btnAdd); add(btnUpdate); add(btnDelete); add(btnLoad);

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(380,10,400,400);
        add(sp);

        btnAdd.addActionListener(e -> {
            service.createPayment(readForm());
            loadTable();
        });

        btnUpdate.addActionListener(e -> {
            int id = Integer.parseInt(txtId.getText());
            service.updatePayment(id, readForm());
            loadTable();
        });

        btnDelete.addActionListener(e -> {
            int id = Integer.parseInt(txtId.getText());
            service.deletePayment(id);
            loadTable();
        });

        btnLoad.addActionListener(e -> loadTable());
    }

    private PaymentRequest readForm() {
        return new PaymentRequest(
                txtType.getText(),
                Double.parseDouble(txtAmount.getText())
        );
    }

    private void loadTable() {
        model.setRowCount(0);
        List<PaymentResponse> payments = service.findAllPayments();
        for (PaymentResponse p : payments) {
            model.addRow(new Object[]{
                    p.getId(),
                    p.getType(),
                    p.getAmount()
            });
        }
    }

    private void addLabel(String text, int x, int y){
        JLabel l = new JLabel(text);
        l.setBounds(x, y, 100, 25);
        l.setForeground(UITheme.TEXT_DARK);
        l.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
        add(l);
    }

    private void addField(JTextField f, int x, int y){
        f.setBounds(x, y, 200, 28);
        UITheme.styleField(f);
        add(f);
    }
}