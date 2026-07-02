
package com.unmsm.marketplace.postventa_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "logistica_inversa")
public class LogisticaInversa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_loginversa")
    private Long idLogInversa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ticket", nullable = false)
    private TicketPostventa ticketPostventa;

    @Column(name = "trackingid_retorno", length = 100)
    private String trackingIdRetorno;

    @Column(name = "metodo_retorno", nullable = false, length = 100)
    private String metodoRetorno;

    @Column(name = "estado_retorno", nullable = false)
    private Integer estadoRetorno;
}