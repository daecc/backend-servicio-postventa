package com.unmsm.marketplace.postventa_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "vendor-service", url = "${vendor.service.url}")
public interface VendorClient {

    @GetMapping("/api/vendors/{vendorId}/name")
    Map<String, Object> obtenerNombreVendor(@PathVariable("vendorId") Long vendorId);
}
