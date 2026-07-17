package com.unmsm.marketplace.postventa_service.dto;

import java.math.BigDecimal;

public record EjecutarCambioProductoRequestDTO(
    Long idOItem,
    String idProductoNuevo,
    BigDecimal precioUnitarioNuevo,
    Integer cantidad,
    Long idVendedorNuevo,
    String nombreVendedorNuevo
) {}
