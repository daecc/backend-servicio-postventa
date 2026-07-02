package com.unmsm.marketplace.postventa_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
// Aquí le decimos la ruta exacta de la carpeta client:
@EnableFeignClients(basePackages = "com.unmsm.marketplace.postventa_service.client") 
public class PostventaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostventaServiceApplication.class, args);
    }

}