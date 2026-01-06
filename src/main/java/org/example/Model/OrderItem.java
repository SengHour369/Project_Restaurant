package org.example.Model;

public class OrderItem {
    private Integer id;
    private Integer menuItemId;
    private Integer quantity;
    private Double price;


    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Integer menuItemId) { this.menuItemId = menuItemId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
