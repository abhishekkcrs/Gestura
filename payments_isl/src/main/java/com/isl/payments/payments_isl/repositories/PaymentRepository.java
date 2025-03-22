package com.isl.payments.payments_isl.repositories;

import com.isl.payments.payments_isl.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Payment findByOrderId(String orderId);

    Payment findByTransactionId(String paymentIntentId);
    List<Payment> findByUsername(String username);
}
