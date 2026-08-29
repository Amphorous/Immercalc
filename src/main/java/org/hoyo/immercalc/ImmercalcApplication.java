package org.hoyo.immercalc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ImmercalcApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImmercalcApplication.class, args);
    }

}
