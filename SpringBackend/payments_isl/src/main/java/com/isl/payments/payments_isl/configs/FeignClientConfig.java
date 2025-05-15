package com.isl.payments.payments_isl.configs;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attributes != null) {
                    var request = attributes.getRequest();

                    if (request.getCookies() != null) {
                        for (var cookie : request.getCookies()) {
                            if ("Authorization".equals(cookie.getName())) {
                                String token = cookie.getValue();
                                template.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                                break;
                            }
                        }
                    }
                }
            }
        };
    }
}
