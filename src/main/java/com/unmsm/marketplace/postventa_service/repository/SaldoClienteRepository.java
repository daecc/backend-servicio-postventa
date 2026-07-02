package com.unmsm.marketplace.postventa_service.repository;

import com.unmsm.marketplace.postventa_service.model.SaldoCliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaldoClienteRepository extends JpaRepository<SaldoCliente, Long> {
}