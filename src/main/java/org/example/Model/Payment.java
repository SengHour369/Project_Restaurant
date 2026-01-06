package org.example.Model;

import org.example.BaseEntity;

public class Payment extends BaseEntity {
    private String type;
    private Double amount;

    public Payment(int paymentId) {
       this.setId(paymentId);
    }

    public Payment() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }


}
