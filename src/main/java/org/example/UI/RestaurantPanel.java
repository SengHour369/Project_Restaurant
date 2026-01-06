package org.example.UI;

import org.example.DTO.Request.RestaurantRequest;
import org.example.DTO.Response.RestaurantResponse;
import org.example.Exception.MessageException;
import org.example.Service.ServiceImplement.ServiceRestaurantImp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class RestaurantPanel extends JPanel {

    JTextField txtId = new JTextField();
    JTextField txtName = new JTextField();
    JTextField txtCategory = new JTextField();
    JTextField txtRating = new JTextField();
    JTextField txtPhone = new JTextField();
    JTextField txtLocation = new JTextField();


    DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID","Name","Category","Rating","Phone","Location"}, 0
    );

    JTable table = new JTable(model);
    ServiceRestaurantImp service = new ServiceRestaurantImp();

    public RestaurantPanel() {
        setLayout(null);

        addLabel("ID",10,10); addField(txtId,120,10);
        addLabel("Name",10,70); addField(txtName,120,70);
        addLabel("Category",10,100); addField(txtCategory,120,100);
        addLabel("Rating",10,130); addField(txtRating,120,130);
        addLabel("Phone",10,160); addField(txtPhone,120,160);
        addLabel("Location",10,190); addField(txtLocation,120,190);


        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnLoad = new JButton("Load");

        btnAdd.setBounds(10,290,80,30);
        btnUpdate.setBounds(100,290,80,30);
        btnDelete.setBounds(190,290,80,30);
        btnLoad.setBounds(280,290,80,30);

        add(btnAdd); add(btnUpdate); add(btnDelete); add(btnLoad);

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(450,10,520,430);
        add(sp);


        btnAdd.addActionListener(e -> {
            try {
                service.CreateRestaurant(readForm());
            } catch (MessageException ex) {
                throw new RuntimeException(ex);
            }
            loadTable(); });
        btnUpdate.addActionListener(e -> { service.updateRestaurant(readForm(), Integer.parseInt(txtId.getText())); loadTable(); });
        btnDelete.addActionListener(e -> { service.deleteRestaurant(Integer.parseInt(txtId.getText())); loadTable(); });
        btnLoad.addActionListener(e -> loadTable());
    }

    private RestaurantRequest readForm() {

        return new RestaurantRequest(

                txtName.getText(),
                txtCategory.getText(),
                txtRating.getText().isEmpty() ? 0 : Integer.parseInt(txtRating.getText()),
                txtPhone.getText(),
                txtLocation.getText()
        );
    }

    private void loadTable() {
        model.setRowCount(0);
        List<RestaurantResponse> restaurants = service.findAllRestaurants();
        for (RestaurantResponse r : restaurants) {
            model.addRow(new Object[]{
                    r.getId(), r.getName(), r.getCategory(),
                    r.getRating(), r.getPhone_number(), r.getLocation(),

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
