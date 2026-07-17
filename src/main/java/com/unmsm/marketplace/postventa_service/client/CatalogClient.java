package com.unmsm.marketplace.postventa_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "catalog-service", url = "${catalog.service.url}")
public interface CatalogClient {

    @GetMapping("/api/vendor-products")
    Map<String, Object> obtenerProductos();
}
