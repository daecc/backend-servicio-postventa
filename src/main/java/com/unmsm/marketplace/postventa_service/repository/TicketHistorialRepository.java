package com.unmsm.marketplace.postventa_service.repository;

import com.unmsm.marketplace.postventa_service.model.TicketHistorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketHistorialRepository extends JpaRepository<TicketHistorial, Long> {
    List<TicketHistorial> findByTicketPostventa_IdTicketOrderByFechaCambioDesc(Long idTicket);
}