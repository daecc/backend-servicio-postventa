package com.unmsm.marketplace.postventa_service.client;

import com.unmsm.marketplace.postventa_service.dto.EjecutarCambioProductoRequestDTO;
import com.unmsm.marketplace.postventa_service.dto.OrdenMaestraResponseDTO;
import com.unmsm.marketplace.postventa_service.dto.OrdenItemResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "ordenes-service", url = "${ordenes.service.url}")
public interface OrdenesClient {

    @GetMapping("/cliente/{dni}")
    List<OrdenMaestraResponseDTO> buscarOrdenesPorDni(@PathVariable("dni") String dni);

    @PutMapping("/suborden/{id}/estado/anulado")
    void anularSubOrden(@PathVariable("id") Long idSubOrden);

    @PutMapping("/suborden/{id}/estado/devuelto")
    void devolverSubOrden(@PathVariable("id") Long idSubOrden);

    @PutMapping("/items/{id}/estado/anulado")
    void anularItem(@PathVariable("id") Long idOItem);

    @PutMapping("/items/{id}/estado/devuelto")
    void devolverItem(@PathVariable("id") Long idOItem, @RequestParam(value = "cantidad", required = false) Integer cantidad);

    @PutMapping("/items/estado-reclamo")
    void marcarItemsEnReclamo(@RequestBody List<Long> ids);

    @PutMapping("/items/{id}/estado-activo")
    void restaurarItem(@PathVariable("id") Long idOItem);

    @GetMapping("/vendedor/{id}/items-ids")
    List<Long> obtenerIdItemsPorVendedor(@PathVariable("id") Long idVendedor);

    @GetMapping("/vendedor/{id}/suborden-ids")
    List<Long> obtenerIdSubOrdenesPorVendedor(@PathVariable("id") Long idVendedor);

    @PostMapping("/items/detalle")
    List<OrdenItemResponseDTO> obtenerDetalleItemsPorIds(@RequestBody List<Long> ids);

    @PutMapping("/cambio-producto")
    void ejecutarCambioProducto(@RequestBody EjecutarCambioProductoRequestDTO request);
}