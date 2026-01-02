package org.example.UI;

import org.example.DTO.Request.PaymentRequest;
import org.example.DTO.Response.PaymentResponse;
import org.example.Service.ServiceImplement.ServicePaymentImp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

        addLabel("ID",10,10); addField(txtId,120,10);
        addLabel("Type",10,40); addField(txtType,120,40);
        addLabel("Amount",10,70); addField(txtAmount,120,70);

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnLoad = new JButton("Load");

        btnAdd.setBounds(10,110,80,30);
        btnUpdate.setBounds(100,110,80,30);
        btnDelete.setBounds(190,110,80,30);
        btnLoad.setBounds(280,110,80,30);

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
        add(l);
    }

    private void addField(JTextField f, int x, int y){
        f.setBounds(x, y, 200, 25);
        add(f);
    }
}
