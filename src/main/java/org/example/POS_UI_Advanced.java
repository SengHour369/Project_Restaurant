//package org.example;
//
//import org.example.DTO.Request.OrderItemRequest;
//import org.example.DTO.Request.OrderRequest;
//import org.example.DTO.Request.PaymentRequest;
//import org.example.DTO.Response.PaymentResponse;
//import org.example.DTO.Response.RestaurantResponse;
//import org.example.Exception.MessageException;
//import org.example.Model.Payment;
//import org.example.Model.Restaurant;
//import org.example.Model.User;
//import org.example.Service.ServiceImplement.ServiceOrderImp;
//import org.example.Service.ServiceImplement.ServiceOrderItemImpl;
//import org.example.Service.ServiceImplement.ServicePaymentImp;
//
//import javax.swing.*;
//import java.awt.*;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//public class POS_UI_Advanced extends JFrame {
//
//    private DefaultListModel<String> orderListModel;
//    private JLabel subtotalLabel;
//    private double subtotal = 0;
//
//    private int currentOrderId = -1;
//    private int currentPaymentId = -1;
//
//    private final ServiceOrderImp orderService = new ServiceOrderImp();
//    private final ServiceOrderItemImpl orderItemService = new ServiceOrderItemImpl();
//    private final ServicePaymentImp paymentService = new ServicePaymentImp();
//
//    private final Map<String, Integer> menuItemMap = new HashMap<>();
//
//    public POS_UI_Advanced() {
//        setTitle("POS System");
//        setSize(1100, 650);
//        setDefaultCloseOperation(EXIT_ON_CLOSE);
//        setLayout(new BorderLayout());
//
//        menuItemMap.put("Burger", 1);
//        menuItemMap.put("Fries", 2);
//        menuItemMap.put("Cola", 3);
//
//        JPanel menuPanel = new JPanel(new GridLayout(1, 3));
//        add(menuPanel, BorderLayout.CENTER);
//
//        addMenuButton(menuPanel, "Burger", 10);
//        addMenuButton(menuPanel, "Fries", 5);
//        addMenuButton(menuPanel, "Cola", 3);
//
//        JPanel orderPanel = new JPanel(new BorderLayout());
//        orderPanel.setPreferredSize(new Dimension(350, 0));
//
//        orderListModel = new DefaultListModel<>();
//        JList<String> orderList = new JList<>(orderListModel);
//        orderPanel.add(new JScrollPane(orderList), BorderLayout.CENTER);
//
//        subtotalLabel = new JLabel("Subtotal: $0.00");
//        subtotalLabel.setFont(new Font("Arial", Font.BOLD, 16));
//        orderPanel.add(subtotalLabel, BorderLayout.NORTH);
//
//        JButton payBtn = new JButton("PAY");
//        payBtn.addActionListener(e -> pay());
//        orderPanel.add(payBtn, BorderLayout.SOUTH);
//
//        add(orderPanel, BorderLayout.EAST);
//        setVisible(true);
//    }
//
//    private void addMenuButton(JPanel panel, String name, double price) {
//        JButton btn = new JButton(name + " $" + price);
//        btn.addActionListener(e -> addItem(name, price));
//        panel.add(btn);
//    }
//
//    private void addItem(String itemName, double price) {
//        orderListModel.addElement(itemName + " - $" + price);
//        subtotal += price;
//        subtotalLabel.setText("Subtotal: $" + subtotal);
//
//        try {
//            if (currentOrderId == -1) {
//                createOrderWithPayment();
//            }
//
//            OrderItemRequest req = new OrderItemRequest();
//            req.setOrderId(currentOrderId);
//            req.setMenuItemId(menuItemMap.get(itemName));
//            req.setQuantity(1);
//            req.setPrice(price);
//
//            orderItemService.createOrderItem(req);
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
////    private void createOrderWithPayment() throws MessageException {
////        // 1️⃣ Create payment FIRST
////        PaymentRequest paymentReq = new PaymentRequest();
////        paymentReq.setType("CASH");
////        paymentReq.setAmount(0D);
////
////        PaymentResponse paymentResp = paymentService.createPayment(paymentReq);
////        currentPaymentId = paymentResp.getId();
////
////        // 2️⃣ Create order
////        User user = new User();
////        user.setId(1);
////
////        RestaurantResponse restaurant = new RestaurantResponse();
////        restaurant.setId(1);
////
////        Payment payment = new Payment();
////        payment.setId(currentPaymentId);
////
////        OrderRequest orderReq = new OrderRequest();
////        orderReq.setOrderDate(LocalDateTime.now());
////        orderReq.setTotalPrice(0D);
////        orderReq.setUser(user);
////        orderReq.setRestaurant(restaurant);
////        orderReq.setPayment(payment);
////
////        currentOrderId = orderService.createOrder(orderReq)
////                .getId(); // ONLY works if BaseEntity has id
////    }
//
//    private void pay() {
//        if (currentOrderId == -1) return;
//
//        PaymentRequest updatePayment = new PaymentRequest();
//        updatePayment.setType("CASH");
//        updatePayment.setAmount(subtotal);
//
//        paymentService.updatePayment(currentPaymentId, updatePayment);
//
//        JOptionPane.showMessageDialog(this, "Payment Successful!");
//
//        orderListModel.clear();
//        subtotal = 0;
//        subtotalLabel.setText("Subtotal: $0.00");
//        currentOrderId = -1;
//        currentPaymentId = -1;
//    }
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(POS_UI_Advanced::new);
//    }
//}
