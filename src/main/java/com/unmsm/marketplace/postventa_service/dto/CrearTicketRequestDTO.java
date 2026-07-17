package com.unmsm.marketplace.postventa_service.dto;

import java.math.BigDecimal;
import java.util.List;

public record CrearTicketRequestDTO(
    Long idOMaestraRef,
    String dniCliente,
    Integer tipoSolicitud,
    String motivoReclamo,
    String nuevaTalla,
    String idProductoNuevoRef,
    Long nuevoIdVendedor,
    String nuevoProductoNombre,
    BigDecimal nuevoProductoPrecio,
    Integer nuevoProductoId,
    List<ItemReclamoDTO> items,
    List<Long> subOrdenesAAnular,
    List<Long> itemsAAnular
) {
    public record ItemReclamoDTO(
        Long idOItemRef,
        Integer cantidadAfectada,
        BigDecimal montoAfectado,
        String nombreProducto,
        String sku
    ) {}
}
