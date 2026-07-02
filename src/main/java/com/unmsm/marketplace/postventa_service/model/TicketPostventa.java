
package com.unmsm.marketplace.postventa_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ticket_postventa")
public class TicketPostventa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ticket")
    private Long idTicket;

    @Column(name = "id_omaestra_ref", nullable = false)
    private Long idOMaestraRef;

    @Column(name = "id_cliente_ref", nullable = false)
    private Long idClienteRef;

    @Column(name = "tipo_solicitud", nullable = false)
    private Integer tipoSolicitud;

    @Column(name = "motivo_reclamo", nullable = false, length = 255)
    private String motivoReclamo;

    @Column(name = "estado_ticket", nullable = false)
    private Integer estadoTicket;

    @Column(name = "fecha_apertura")
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "sub_ordenes_anular_json", columnDefinition = "TEXT")
    private String subOrdenesAnularJson;

    @Column(name = "items_anular_json", columnDefinition = "TEXT")
    private String itemsAnularJson;

}
