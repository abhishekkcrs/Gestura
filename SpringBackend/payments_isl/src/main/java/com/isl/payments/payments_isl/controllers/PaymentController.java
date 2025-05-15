package com.isl.payments.payments_isl.controllers;

import com.isl.payments.payments_isl.DTO.ProductRequest;
import com.isl.payments.payments_isl.DTO.ProductResponse;
import com.isl.payments.payments_isl.entities.Payment;
import com.isl.payments.payments_isl.services.PaymentService;
import com.isl.payments.payments_isl.services.StripeService;
import com.stripe.exception.StripeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    Logger logger= LoggerFactory.getLogger(PaymentController.class);

    private final StripeService stripeService;

    public PaymentController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/create")
    public ProductResponse createPaymentIntent(@RequestBody ProductRequest productRequest) throws StripeException {
        logger.info("Received Payment Request:");
        return stripeService.createPayment(productRequest);
    }

    @GetMapping(value = "/fetchorders", produces = "application/json")
    @ResponseBody
    public List<Payment> getPayments(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("Fetching Orders of {}",username);
        return paymentService.getAllOrders(username);
    }
}
