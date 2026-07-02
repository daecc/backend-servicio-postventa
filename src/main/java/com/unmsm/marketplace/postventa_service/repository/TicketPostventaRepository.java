package com.unmsm.marketplace.postventa_service.repository;

import com.unmsm.marketplace.postventa_service.model.TicketPostventa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketPostventaRepository extends JpaRepository<TicketPostventa, Long> {
}