
package com.unmsm.marketplace.postventa_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ticket_historial")
public class TicketHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_thistorial")
    private Long idTHistorial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ticket", nullable = false)
    private TicketPostventa ticketPostventa;

    @Column(name = "id_adminaccion", nullable = true)
    private Long idAdminAccion;

    @Column(name = "estado_anterior")
    private Integer estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false)
    private Integer estadoNuevo;

    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "fecha_cambio", insertable = false, updatable = false)
    private LocalDateTime fechaCambio;
}
