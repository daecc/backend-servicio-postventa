package com.unmsm.marketplace.postventa_service.repository;

import com.unmsm.marketplace.postventa_service.model.TicketItemDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketItemDetalleRepository extends JpaRepository<TicketItemDetalle, Long> {
    List<TicketItemDetalle> findByTicketPostventa_IdTicket(Long idTicket);
}