package com.isl.payments.payments_isl.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private Long amount;
    private String currency;
    private String orderId;
    private String username;
}
