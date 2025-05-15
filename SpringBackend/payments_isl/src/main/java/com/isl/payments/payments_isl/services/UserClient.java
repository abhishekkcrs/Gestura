package com.isl.payments.payments_isl.services;

import com.isl.payments.payments_isl.configs.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "ISL-MAIN", configuration = FeignClientConfig.class)
public interface UserClient {

    @PostMapping("/user/success")
    public void updateKey(@RequestHeader("Authorization") String token);
}
