package org.example.Service;

import org.example.DTO.Request.PaymentRequest;
import org.example.DTO.Response.PaymentResponse;

import java.util.List;

public interface ServicePayment {
    PaymentResponse createPayment(PaymentRequest paymentRequest);
    PaymentResponse updatePayment(int id, PaymentRequest paymentRequest);
    void deletePayment(int id);
    PaymentResponse findPaymentById(int id);
    List<PaymentResponse> findAllPayments();
}
