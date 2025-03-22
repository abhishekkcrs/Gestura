package com.isl.payments.payments_isl.services;

import com.isl.payments.payments_isl.entities.Payment;
import com.isl.payments.payments_isl.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    @Autowired
    PaymentRepository paymentRepository;

    public List<Payment> getAllOrders(String username){
        return paymentRepository.findByUsername(username);
    }

}
