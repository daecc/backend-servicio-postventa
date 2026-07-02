
package com.unmsm.marketplace.postventa_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "saldo_cliente")
public class SaldoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_saldo")
    private Long idSaldo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ticket", nullable = false)
    private TicketPostventa ticketPostventa;

    @Column(name = "id_cliente_ref", nullable = false)
    private Long idClienteRef;

    @Column(name = "monto_disponible", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoDisponible;

    @Column(name = "estado_saldo", nullable = false)
    private Integer estadoSaldo;
}