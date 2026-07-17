package com.unmsm.marketplace.postventa_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TicketResponseDTO(
    Long idTicket,
    Long idOMaestraRef,
    String dniCliente,
    Integer tipoSolicitud,
    String motivoReclamo,
    Integer estadoTicket,
    LocalDateTime fechaApertura,
    LocalDateTime fechaCierre,
    List<ItemTicketDTO> items,
    List<Long> subOrdenesAAnular,
    List<Long> itemsAAnular,
    BigDecimal montoSaldoGenerado,
    Long nuevoIdVendedor,
    String nuevoNombreVendedor,
    String nuevoProductoNombre,
    BigDecimal nuevoProductoPrecio,
    Integer nuevoProductoId
) {
    public record ItemTicketDTO(
        Long idOItemRef,
        Integer cantidadAfectada,
        BigDecimal montoAfectado,
        String nuevaTalla,
        String idProductoNuevoRef,
        String nombreProducto,
        String sku
    ) {}
}
