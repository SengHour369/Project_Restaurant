package org.example.DTO.Response;

import org.example.BaseEntity;

public class PaymentResponse extends BaseEntity {
    private String type;
    private Double amount;

    public PaymentResponse(int id, String type, Double amount) {
        setId(id);
        this.type = type;
        this.amount = amount;
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

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "id=" + getId() +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                '}';
    }
}
