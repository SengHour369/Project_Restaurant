package org.example.UI;

import org.example.DTO.Request.MenuItemRequest;
import org.example.DTO.Response.MenuItemResponse;
import org.example.DTO.Response.RestaurantResponse;
import org.example.Service.ServiceImplement.ServiceMenuItemImp;
import org.example.Service.ServiceImplement.ServiceRestaurantImp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MenuItemPanel extends JPanel {

    private final ServiceMenuItemImp menuService = new ServiceMenuItemImp();
    private final ServiceRestaurantImp restaurantService = new ServiceRestaurantImp();

    private final DefaultTableModel model =
            new DefaultTableModel(new String[]{"ID","Name","Price","Restaurant"},0);
    private final JTable table = new JTable(model);

    private final JTextField txtName = new JTextField();
    private final JTextField txtPrice = new JTextField();
    private final JComboBox<RestaurantResponse> cbRestaurant = new JComboBox<>();

    public MenuItemPanel() {
        setLayout(null);

        // ===== TABLE =====
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(10,10,600,300);
        add(sp);

        // ===== FORM =====
        addLabel("Name",10,330);
        txtName.setBounds(120,330,150,25);
        add(txtName);

        addLabel("Price",10,360);
        txtPrice.setBounds(120,360,150,25);
        add(txtPrice);

        addLabel("Restaurant",10,390);
        cbRestaurant.setBounds(120,390,150,25);
        add(cbRestaurant);

        // ===== BUTTONS =====
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");

        btnAdd.setBounds(300,330,100,25);
        btnUpdate.setBounds(300,360,100,25);
        btnDelete.setBounds(300,390,100,25);

        add(btnAdd);
        add(btnUpdate);
        add(btnDelete);

        // ===== LOAD DATA =====
        loadRestaurants();
        loadMenuItems();

        // ===== EVENTS =====
        table.getSelectionModel().addListSelectionListener(e -> fillForm());
        btnAdd.addActionListener(e -> addMenuItem());
        btnUpdate.addActionListener(e -> updateMenuItem());
        btnDelete.addActionListener(e -> deleteMenuItem());
    }

    // ================= ADD =================
    private void addMenuItem(){
        try{
            RestaurantResponse r =
                    (RestaurantResponse) cbRestaurant.getSelectedItem();
            if(r == null) return;

            MenuItemRequest req = new MenuItemRequest();
            req.setName(txtName.getText());
            req.setPrice(txtPrice.getText());
            req.setRestaurant(r.getId()); // ✅ restaurant ID
            req.setActive(true);

            menuService.createMenuItem(req);
            loadMenuItems();
            clearForm();
        }catch (Exception ex){
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    // ================= UPDATE =================
    private void updateMenuItem(){
        int row = table.getSelectedRow();
        if(row < 0) return;

        try{
            RestaurantResponse r =
                    (RestaurantResponse) cbRestaurant.getSelectedItem();

            MenuItemRequest req = new MenuItemRequest();
            req.setId((int) model.getValueAt(row,0));
            req.setName(txtName.getText());
            req.setPrice(txtPrice.getText());
            req.setRestaurant(r.getId()); // ✅
            req.setActive(true);

            menuService.updateMenuItem(req);
            loadMenuItems();
            clearForm();
        }catch (Exception ex){
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    // ================= DELETE =================
    private void deleteMenuItem(){
        int row = table.getSelectedRow();
        if(row < 0) return;

        MenuItemRequest req = new MenuItemRequest();
        req.setId((int) model.getValueAt(row,0));

        menuService.deleteMenuItem(req);
        loadMenuItems();
        clearForm();
    }

    // ================= LOAD =================
    private void loadRestaurants(){
        cbRestaurant.removeAllItems();
        for(RestaurantResponse r : restaurantService.findAllRestaurants()){
            cbRestaurant.addItem(r);
        }
    }

    private void loadMenuItems(){
        model.setRowCount(0);
        for(MenuItemResponse m : menuService.getAllMenuItems()){
            model.addRow(new Object[]{
                    m.getId(),
                    m.getName(),
                    m.getPrice(),
                    m.getRestaurant()
            });
        }
    }

    // ================= FORM =================
    private void fillForm(){
        int row = table.getSelectedRow();
        if(row < 0) return;

        txtName.setText(model.getValueAt(row,1).toString());
        txtPrice.setText(model.getValueAt(row,2).toString());

        String resName = model.getValueAt(row,3).toString();
        for(int i=0;i<cbRestaurant.getItemCount();i++){
            if(cbRestaurant.getItemAt(i).getName().equals(resName)){
                cbRestaurant.setSelectedIndex(i);
                break;
            }
        }
    }

    private void clearForm(){
        txtName.setText("");
        txtPrice.setText("");
        if(cbRestaurant.getItemCount() > 0)
            cbRestaurant.setSelectedIndex(0);
    }

    private void addLabel(String t,int x,int y){
        JLabel l = new JLabel(t);
        l.setBounds(x,y,100,25);
        add(l);
    }
}
