
package com.unmsm.marketplace.postventa_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SubOrdenResponseDTO(
    Long idSOrden,
    Long idOMaestra,
    Long idSeller,
    Long idVendedor,
    String nombreVendedor,
    String direccionEnvio,
    String distritoEnvio,
    String metodoEnvio,
    String telefonoContacto,
    Integer estadoParcialVendedor,
    BigDecimal montoSubTotalVendedor,
    LocalDateTime fechaCreacionSub,
    List<OrdenItemResponseDTO> items,
    Boolean activo
) {}