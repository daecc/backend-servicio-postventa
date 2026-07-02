
package com.unmsm.marketplace.postventa_service.model;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "ticket_item_detalle")
public class TicketItemDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_titem")
    private Long idTItem;

    // Relación de llave foránea hacia TicketPostventa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ticket", nullable = false)
    private TicketPostventa ticketPostventa;

    @Column(name = "id_oitem_ref", nullable = false)
    private Long idOItemRef;

    @Column(name = "cantidad_afectada", nullable = false)
    private Integer cantidadAfectada;

    @Column(name = "monto_afectado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoAfectado;

    @Column(name = "nueva_talla", length = 50)
    private String nuevaTalla;

    @Column(name = "id_productonuevo_ref", length = 100)
    private String idProductoNuevoRef;

    @Column(name = "nombre_producto", length = 255)
    private String nombreProducto;

    @Column(name = "sku", length = 100)
    private String sku;
}