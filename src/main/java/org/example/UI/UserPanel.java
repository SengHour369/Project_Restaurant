package org.example.UI;

import org.example.DTO.Request.UserRequest;
import org.example.DTO.Response.UserResponse;
import org.example.Model.User;
import org.example.Service.ServiceImplement.ServiceUserImp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class UserPanel extends JPanel {

    JTextField txtId = new JTextField();
    JTextField txtName = new JTextField();
    JTextField txtGender = new JTextField();
    JTextField txtDob = new JTextField();
    JTextField txtPhone = new JTextField();
    JTextField txtAddress = new JTextField();
    JTextField txtEmail = new JTextField();
    JPasswordField txtPassword = new JPasswordField();
    JTextField txtStatus = new JTextField();

    DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID","Name","Gender","DOB","Phone","Address","Email","Status"}, 0
    );
    JTable table = new JTable(model);

    ServiceUserImp service = new ServiceUserImp();

    public UserPanel(UserResponse user) {
        setLayout(null);

        txtId.setEditable(false);

        addLabel("ID",10,10); addField(txtId,120,10);
        addLabel("Name",10,40); addField(txtName,120,40);
        addLabel("Gender",10,70); addField(txtGender,120,70);
        addLabel("DOB",10,100); addField(txtDob,120,100);
        addLabel("Phone",10,130); addField(txtPhone,120,130);
        addLabel("Address",10,160); addField(txtAddress,120,160);
        addLabel("Email",10,190); addField(txtEmail,120,190);
        addLabel("Password",10,220); addPasswordField(txtPassword,120,220);
        addLabel("Status",10,250); addField(txtStatus,120,250);

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnLoad = new JButton("Load");

        btnAdd.setBounds(10,290,80,30);
        btnUpdate.setBounds(100,290,80,30);
        btnDelete.setBounds(190,290,80,30);
        btnLoad.setBounds(280,290,80,30);

        add(btnAdd);
        add(btnUpdate);
        add(btnDelete);
        add(btnLoad);

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(350,10,520,430);
        add(sp);

        btnAdd.addActionListener(e -> {
            service.create(readForm());
            loadTable();
            clearForm();
        });

        btnUpdate.addActionListener(e -> {
            service.update(Integer.parseInt(txtId.getText()), readForm());
            loadTable();
        });

        btnDelete.addActionListener(e -> {
            service.delete(Integer.parseInt(txtId.getText()));
            loadTable();
            clearForm();
        });

        btnLoad.addActionListener(e -> loadTable());

        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());

        loadTable();
    }

    private UserRequest readForm() {
        return new UserRequest(
                txtName.getText(),
                txtGender.getText(),
                txtDob.getText(),
                txtPhone.getText(),
                txtAddress.getText(),
                txtEmail.getText(),
                new String(txtPassword.getPassword()),
                txtStatus.getText()
        );
    }

    private void loadTable() {
        model.setRowCount(0);
        List<UserResponse> users = service.findAll();
        for (UserResponse u : users) {
            model.addRow(new Object[]{
                    u.getId(),
                    u.getName(),
                    u.getGender(),
                    u.getDateOfBirth(),
                    u.getPhone(),
                    u.getAddress(),
                    u.getEmail(),
                    u.getStatus()
            });
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        txtId.setText(model.getValueAt(row,0).toString());
        txtName.setText(model.getValueAt(row,1).toString());
        txtGender.setText(model.getValueAt(row,2).toString());
        txtDob.setText(model.getValueAt(row,3).toString());
        txtPhone.setText(model.getValueAt(row,4).toString());
        txtAddress.setText(model.getValueAt(row,5).toString());
        txtEmail.setText(model.getValueAt(row,6).toString());
        txtStatus.setText(model.getValueAt(row,7).toString());
        txtPassword.setText(""); // security
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtGender.setText("");
        txtDob.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
        txtStatus.setText("");
    }

    private void addLabel(String t,int x,int y){
        JLabel l = new JLabel(t);
        l.setBounds(x,y,100,25);
        add(l);
    }

    private void addField(JTextField f,int x,int y){
        f.setBounds(x,y,200,25);
        add(f);
    }

    private void addPasswordField(JPasswordField f,int x,int y){
        f.setBounds(x,y,200,25);
        add(f);
    }
}
