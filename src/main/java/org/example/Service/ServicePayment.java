package org.example.Service;

import org.example.DTO.Request.PaymentRequest;
import org.example.DTO.Response.PaymentResponse;

public interface ServicePayment {
    PaymentResponse pay(PaymentRequest paymentRequest);
}
