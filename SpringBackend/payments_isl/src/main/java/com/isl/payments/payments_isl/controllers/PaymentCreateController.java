package com.isl.payments.payments_isl.controllers;

import com.isl.payments.payments_isl.security.JwtHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@CrossOrigin(origins = "http://localhost:8081")
@Controller
@RequestMapping("/payment")
public class PaymentCreateController {

    @Autowired
    JwtHelper jwtHelper;

    Logger logger= LoggerFactory.getLogger(PaymentCreateController.class);

    @GetMapping
    public String paymentPage(@RequestParam("username") String username,
                              @RequestParam("orderId") String orderId,
                              @RequestParam("amount") Long amount,
                              @RequestParam("currency") String currency,
                              Model model) {

        // ✅ Log payment details for debugging
        System.out.println("Username: " + username);
        System.out.println("Order ID: " + orderId);
        System.out.println("Amount: " + amount);
        System.out.println("Currency: " + currency);

        // ✅ Pass payment details to frontend
        model.addAttribute("username", username);
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        model.addAttribute("currency", currency);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info(authentication.getName());
        return "payment"; // ✅ Load payment.html
    }




    @GetMapping("/success")
    public String successHandler(){
        return "success";
    }

    @GetMapping("/cancelled")
    public String cancelHandler(){
        return "cancelled";
    }

    @GetMapping("/home")
    public String gotoHome(){
        return "redirect:http://localhost:8081/user/dashboard";
    }

    @GetMapping("/orders")
    public String gotoOrders(){
        return "redirect:http://localhost:8081/user/orders";
    }

}
