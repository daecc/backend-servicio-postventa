package com.unmsm.marketplace.postventa_service.dto;

import java.time.LocalDateTime;

public record HistorialResponseDTO(
    Long idTHistorial,
    Long idAdminAccion,
    Integer estadoAnterior,
    Integer estadoNuevo,
    String comentario,
    LocalDateTime fechaCambio
) {}