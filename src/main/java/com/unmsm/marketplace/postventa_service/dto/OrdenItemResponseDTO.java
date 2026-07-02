
package com.unmsm.marketplace.postventa_service.dto;

import java.math.BigDecimal;

public record OrdenItemResponseDTO(
    Long idOItem,
    String idProducto,
    Integer cantidad,
    BigDecimal precioUnitario,
    Integer estadoItem
) {}