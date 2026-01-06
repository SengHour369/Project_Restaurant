package org.example.UI;

import org.example.DTO.Response.UserResponse;
import org.example.Service.ServiceImplement.ServiceUserImp;

import javax.swing.*;

public class LoginFrame extends JFrame {

    private JTextField txtEmail = new JTextField();
    private JPasswordField txtPassword = new JPasswordField();
    private JButton btnLogin = new JButton("Login");
    private JButton btnRegister = new JButton("Register");
    private ServiceUserImp service = new ServiceUserImp();

    public LoginFrame() {
        setTitle("Login");
        setSize(350, 250);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        addLabel("Email",30,40); addField(txtEmail,120,40);
        addLabel("Password",30,80); addPassword(txtPassword,120,80);

        btnLogin.setBounds(50,140,100,30);
        btnRegister.setBounds(180,140,100,30);
        add(btnLogin); add(btnRegister);

        btnLogin.addActionListener(e -> login());
        btnRegister.addActionListener(e -> openRegister());
    }

    private void login() {
        String email = txtEmail.getText();
        String password = new String(txtPassword.getPassword());
        UserResponse user = service.login(email,password);

        if(user != null){
            JOptionPane.showMessageDialog(this,"Welcome "+user.getName());
            if("ADMIN".equalsIgnoreCase(user.getStatus())){
                System.out.println("ddddddddddddddddddddddd");
                new AdminDashboard(user).setVisible(true);
            } else {
                new MainAppUI(user).setVisible(true);
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,"Invalid email or password");
        }
    }

    private void openRegister(){
        JFrame frame = new JFrame("Register User");
        frame.setSize(600,500);
        frame.add(new UserPanel(null));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void addLabel(String t,int x,int y){ JLabel l = new JLabel(t); l.setBounds(x,y,80,25); add(l);}
    private void addField(JTextField f,int x,int y){ f.setBounds(x,y,170,25); add(f);}
    private void addPassword(JPasswordField f,int x,int y){ f.setBounds(x,y,170,25); add(f);}
}
