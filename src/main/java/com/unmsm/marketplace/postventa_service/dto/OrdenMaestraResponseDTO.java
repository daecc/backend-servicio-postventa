
package com.unmsm.marketplace.postventa_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenMaestraResponseDTO(
    Long idOMaestra,
    String clienteNombre,
    String clienteDni,
    String metodoPago,
    Integer estadoGlobal,
    BigDecimal montoTotalMaestro,
    LocalDateTime fechaCreacion,
    List<SubOrdenResponseDTO> subOrdenes // Aquí metemos las cajas medianas
) {}
