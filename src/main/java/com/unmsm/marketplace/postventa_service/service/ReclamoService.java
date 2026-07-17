package com.unmsm.marketplace.postventa_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unmsm.marketplace.postventa_service.client.AnalyticsClient;
import com.unmsm.marketplace.postventa_service.client.CatalogClient;
import com.unmsm.marketplace.postventa_service.client.OrdenesClient;
import com.unmsm.marketplace.postventa_service.client.VendorClient;
import com.unmsm.marketplace.postventa_service.dto.*;
import com.unmsm.marketplace.postventa_service.model.*;
import com.unmsm.marketplace.postventa_service.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReclamoService {

    private final OrdenesClient ordenesClient;
    private final TicketPostventaRepository ticketRepository;
    private final TicketItemDetalleRepository itemDetalleRepository;
    private final TicketHistorialRepository historialRepository;
    private final SaldoClienteRepository saldoClienteRepository;
    private final LogisticaInversaRepository logisticaInversaRepository;
    private final ObjectMapper objectMapper;
    private final AnalyticsClient analyticsClient;
    private final CatalogClient catalogClient;
    private final VendorClient vendorClient;

    @Value("${analytics.api.key}")
    private String analyticsApiKey;

    public ReclamoService(OrdenesClient ordenesClient,
                          TicketPostventaRepository ticketRepository,
                          TicketItemDetalleRepository itemDetalleRepository,
                          TicketHistorialRepository historialRepository,
                          SaldoClienteRepository saldoClienteRepository,
                          LogisticaInversaRepository logisticaInversaRepository,
                          ObjectMapper objectMapper,
                          AnalyticsClient analyticsClient,
                          CatalogClient catalogClient,
                          VendorClient vendorClient) {
        this.ordenesClient = ordenesClient;
        this.ticketRepository = ticketRepository;
        this.itemDetalleRepository = itemDetalleRepository;
        this.historialRepository = historialRepository;
        this.saldoClienteRepository = saldoClienteRepository;
        this.logisticaInversaRepository = logisticaInversaRepository;
        this.objectMapper = objectMapper;
        this.analyticsClient = analyticsClient;
        this.catalogClient = catalogClient;
        this.vendorClient = vendorClient;
    }

    public List<Map<String, Object>> obtenerCatalogoProductos() {
        try {
            Map<String, Object> response = catalogClient.obtenerProductos();
            if (response != null && response.containsKey("data")) {
                return (List<Map<String, Object>>) response.get("data");
            }
            return List.of();
        } catch (Exception e) {
            System.err.println("Error obteniendo catálogo: " + e.getMessage());
            return List.of();
        }
    }

    public List<OrdenMaestraResponseDTO> buscarOrdenesPorDni(String dni) {
        return ordenesClient.buscarOrdenesPorDni(dni);
    }

    @Transactional
    public TicketResponseDTO crearTicket(CrearTicketRequestDTO request) {
        TicketPostventa ticket = new TicketPostventa();
        ticket.setIdOMaestraRef(request.idOMaestraRef());
        ticket.setIdClienteRef(Long.parseLong(request.dniCliente()));
        ticket.setTipoSolicitud(request.tipoSolicitud());
        ticket.setMotivoReclamo(request.motivoReclamo());
        ticket.setEstadoTicket(1);
        ticket.setFechaApertura(LocalDateTime.now());
        try {
            ticket.setSubOrdenesAnularJson(
                request.subOrdenesAAnular() != null
                    ? objectMapper.writeValueAsString(request.subOrdenesAAnular())
                    : null
            );
            ticket.setItemsAnularJson(
                request.itemsAAnular() != null
                    ? objectMapper.writeValueAsString(request.itemsAAnular())
                    : null
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando listas de anulación", e);
        }
        if (request.tipoSolicitud() == 3) {
            ticket.setNuevoIdVendedor(request.nuevoIdVendedor());
            ticket.setNuevoProductoNombre(request.nuevoProductoNombre());
            ticket.setNuevoProductoPrecio(request.nuevoProductoPrecio());
            ticket.setNuevoProductoId(request.nuevoProductoId());
            if (request.nuevoIdVendedor() != null) {
                try {
                    Map<String, Object> vendorData = vendorClient.obtenerNombreVendor(request.nuevoIdVendedor());
                    if (vendorData != null && vendorData.containsKey("data")) {
                        Map<String, Object> data = (Map<String, Object>) vendorData.get("data");
                        ticket.setNuevoNombreVendedor((String) data.get("vendor_name"));
                    }
                } catch (Exception e) {
                    System.err.println("Error obteniendo nombre del vendor: " + e.getMessage());
                }
            }
        }

        ticket = ticketRepository.save(ticket);

        Long finalIdTicket = ticket.getIdTicket();
        List<TicketItemDetalle> items = request.items().stream().map(itemReq -> {
            TicketItemDetalle detalle = new TicketItemDetalle();
            detalle.setTicketPostventa(ticketRepository.findById(finalIdTicket).orElseThrow());
            detalle.setIdOItemRef(itemReq.idOItemRef());
            detalle.setCantidadAfectada(itemReq.cantidadAfectada());
            detalle.setMontoAfectado(itemReq.montoAfectado());
            detalle.setNuevaTalla(request.nuevaTalla());
            detalle.setIdProductoNuevoRef(request.idProductoNuevoRef());
            detalle.setNombreProducto(itemReq.nombreProducto());
            detalle.setSku(itemReq.sku());
            return detalle;
        }).toList();
        itemDetalleRepository.saveAll(items);

        // Marcar items como EN_RECLAMO(3) en ordenes-service
        List<Long> itemIds = request.items().stream()
            .map(CrearTicketRequestDTO.ItemReclamoDTO::idOItemRef)
            .toList();
        if (!itemIds.isEmpty()) {
            try {
                ordenesClient.marcarItemsEnReclamo(itemIds);
            } catch (Exception e) {
                System.err.println("Error marcando items como EN RECLAMO: " + e.getMessage());
            }
        }

        TicketHistorial historial = new TicketHistorial();
        historial.setTicketPostventa(ticket);
        historial.setIdAdminAccion(1L);
        historial.setEstadoAnterior(null);
        historial.setEstadoNuevo(1);
        historial.setComentario("Ticket creado");
        historialRepository.save(historial);

        try {
            analyticsClient.sendEvent(analyticsApiKey, new AnalyticsEvent(
                UUID.randomUUID().toString(),
                "TICKET_CREADO",
                "postventa-service",
                "ticket",
                String.valueOf(ticket.getIdTicket()),
                List.of(),
                Instant.now().toString(),
                Map.of(
                    "id_ticket", ticket.getIdTicket(),
                    "id_orden_maestra", ticket.getIdOMaestraRef(),
                    "tipo_solicitud", ticket.getTipoSolicitud(),
                    "motivo", ticket.getMotivoReclamo(),
                    "fecha", ticket.getFechaApertura().toString(),
                    "items_count", request.items().size()
                )
            ));
        } catch (Exception e) {
            System.err.println("[ANALYTICS] Error enviando TICKET_CREADO: " + e.getMessage());
        }

        return toTicketResponse(ticket, items);
    }

    public List<TicketResponseDTO> listarTicketsPorVendedor(Long idVendedor) {
        try {
            List<Long> itemIdsDelVendedor = ordenesClient.obtenerIdItemsPorVendedor(idVendedor);
            List<Long> subOrdenIdsDelVendedor = ordenesClient.obtenerIdSubOrdenesPorVendedor(idVendedor);
            List<TicketPostventa> tickets = ticketRepository.findAll();
            return tickets.stream()
                .filter(t -> ticketPerteneceAVendedor(t, itemIdsDelVendedor, subOrdenIdsDelVendedor))
                .map(t -> {
                    List<TicketItemDetalle> items = itemDetalleRepository.findByTicketPostventa_IdTicket(t.getIdTicket());
                    return toTicketResponse(t, items);
                })
                .toList();
        } catch (Exception e) {
            System.err.println("Error obteniendo datos del vendedor " + idVendedor + ": " + e.getMessage());
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<TicketResponseDTO> listarTodosLosTickets() {
        List<TicketPostventa> tickets = ticketRepository.findAll();
        return tickets.stream().map(t -> {
            List<TicketItemDetalle> items = itemDetalleRepository.findByTicketPostventa_IdTicket(t.getIdTicket());
            return toTicketResponse(t, items);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<HistorialResponseDTO> obtenerHistorial(Long idTicket) {
        return historialRepository.findByTicketPostventa_IdTicketOrderByFechaCambioDesc(idTicket)
            .stream()
            .map(h -> new HistorialResponseDTO(
                h.getIdTHistorial(),
                h.getIdAdminAccion(),
                h.getEstadoAnterior(),
                h.getEstadoNuevo(),
                h.getComentario(),
                h.getFechaCambio()
            ))
            .toList();
    }

    private boolean ticketPerteneceAVendedor(TicketPostventa ticket,
                                              List<Long> itemIdsDelVendedor,
                                              List<Long> subOrdenIdsDelVendedor) {
        List<TicketItemDetalle> items = itemDetalleRepository.findByTicketPostventa_IdTicket(ticket.getIdTicket());
        boolean tieneItemsDelVendedor = items.stream()
            .map(TicketItemDetalle::getIdOItemRef)
            .anyMatch(itemIdsDelVendedor::contains);
        if (tieneItemsDelVendedor) return true;

        try {
            if (ticket.getSubOrdenesAnularJson() != null && !ticket.getSubOrdenesAnularJson().isBlank()) {
                List<Long> subOrdenes = objectMapper.readValue(
                    ticket.getSubOrdenesAnularJson(),
                    new TypeReference<List<Long>>() {}
                );
                boolean tieneSubOrdenesDelVendedor = subOrdenes.stream().anyMatch(subOrdenIdsDelVendedor::contains);
                if (tieneSubOrdenesDelVendedor) return true;
            }
        } catch (Exception e) {
            System.err.println("Error deserializando subOrdenesAnularJson: " + e.getMessage());
        }

        try {
            if (ticket.getItemsAnularJson() != null && !ticket.getItemsAnularJson().isBlank()) {
                List<Long> itemsAAnular = objectMapper.readValue(
                    ticket.getItemsAnularJson(),
                    new TypeReference<List<Long>>() {}
                );
                boolean tieneItemsAAnularDelVendedor = itemsAAnular.stream().anyMatch(itemIdsDelVendedor::contains);
                if (tieneItemsAAnularDelVendedor) return true;
            }
        } catch (Exception e) {
            System.err.println("Error deserializando itemsAnularJson: " + e.getMessage());
        }

        return false;
    }

    @Transactional
    public TicketResponseDTO aprobarTicket(Long idTicket, AprobarRechazarRequestDTO request) {
        TicketPostventa ticket = ticketRepository.findById(idTicket)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + idTicket));

        int estadoAnterior = ticket.getEstadoTicket();
        ticket.setEstadoTicket(2);
        ticket.setFechaCierre(LocalDateTime.now());
        ticketRepository.save(ticket);

        List<TicketItemDetalle> items = itemDetalleRepository.findByTicketPostventa_IdTicket(idTicket);

        if (ticket.getTipoSolicitud() == 1 || ticket.getTipoSolicitud() == 4) {
            BigDecimal total = items.stream()
                .map(i -> {
                    BigDecimal monto = i.getMontoAfectado();
                    if (monto == null) monto = BigDecimal.ZERO;
                    return monto.multiply(BigDecimal.valueOf(i.getCantidadAfectada()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            SaldoCliente saldo = new SaldoCliente();
            saldo.setTicketPostventa(ticket);
            saldo.setIdClienteRef(ticket.getIdClienteRef());
            saldo.setMontoDisponible(total);
            saldo.setEstadoSaldo(1);
            saldoClienteRepository.save(saldo);
        }

        if (ticket.getTipoSolicitud() == 3) {
            LogisticaInversa log = new LogisticaInversa();
            log.setTicketPostventa(ticket);
            log.setMetodoRetorno("Courier");
            log.setEstadoRetorno(1);
            logisticaInversaRepository.save(log);

            for (TicketItemDetalle detalle : items) {
                try {
                    ordenesClient.ejecutarCambioProducto(new EjecutarCambioProductoRequestDTO(
                        detalle.getIdOItemRef(),
                        String.valueOf(ticket.getNuevoProductoId() != null
                            ? ticket.getNuevoProductoId() : detalle.getIdOItemRef()),
                        ticket.getNuevoProductoPrecio() != null
                            ? ticket.getNuevoProductoPrecio() : detalle.getMontoAfectado(),
                        1,
                        ticket.getNuevoIdVendedor(),
                        ticket.getNuevoNombreVendedor()
                    ));
                } catch (Exception e) {
                    System.err.println("***** ERROR CAMBIO PRODUCTO item " + detalle.getIdOItemRef() + ": " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
        }

        ejecutarAnulaciones(ticket);

        TicketHistorial historial = new TicketHistorial();
        historial.setTicketPostventa(ticket);
        historial.setIdAdminAccion(request.idAdminAccion());
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(2);
        historial.setComentario(request.comentario());
        historialRepository.save(historial);

        try {
            analyticsClient.sendEvent(analyticsApiKey, new AnalyticsEvent(
                UUID.randomUUID().toString(),
                "TICKET_APROBADO",
                "postventa-service",
                "ticket",
                String.valueOf(ticket.getIdTicket()),
                List.of(),
                Instant.now().toString(),
                Map.of(
                    "id_ticket", ticket.getIdTicket(),
                    "tipo_solicitud", ticket.getTipoSolicitud(),
                    "fecha", ticket.getFechaApertura().toString(),
                    "fecha_aprobacion", ticket.getFechaCierre().toString()
                )
            ));
        } catch (Exception e) {
            System.err.println("[ANALYTICS] Error enviando TICKET_APROBADO: " + e.getMessage());
        }

        return toTicketResponse(ticket, items);
    }

    @Transactional
    public TicketResponseDTO rechazarTicket(Long idTicket, AprobarRechazarRequestDTO request) {
        TicketPostventa ticket = ticketRepository.findById(idTicket)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + idTicket));

        int estadoAnterior = ticket.getEstadoTicket();
        ticket.setEstadoTicket(3);
        ticket.setFechaCierre(LocalDateTime.now());
        ticketRepository.save(ticket);

        List<TicketItemDetalle> items = itemDetalleRepository.findByTicketPostventa_IdTicket(idTicket);

        // Restaurar items a ACTIVO(1) en ordenes-service
        List<Long> itemIds = items.stream()
            .map(TicketItemDetalle::getIdOItemRef)
            .toList();
        for (Long idOItem : itemIds) {
            try {
                ordenesClient.restaurarItem(idOItem);
            } catch (Exception e) {
                System.err.println("Error restaurando item " + idOItem + " a ACTIVO: " + e.getMessage());
            }
        }

        TicketHistorial historial = new TicketHistorial();
        historial.setTicketPostventa(ticket);
        historial.setIdAdminAccion(request.idAdminAccion());
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(3);
        historial.setComentario(request.comentario());
        historialRepository.save(historial);

        return toTicketResponse(ticket, items);
    }

    public void anularSubOrden(Long idSubOrden) {
        ordenesClient.anularSubOrden(idSubOrden);
    }

    public void anularItem(Long idOItem) {
        ordenesClient.anularItem(idOItem);
    }

    public List<OrdenItemResponseDTO> obtenerDetalleItemsPorIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return ordenesClient.obtenerDetalleItemsPorIds(ids);
    }

    private void ejecutarAnulaciones(TicketPostventa ticket) {
        if (ticket.getTipoSolicitud() == 4) {
            ejecutarSobreSubOrdenes(ticket, true);
            ejecutarSobreItems(ticket, true);
        } else if (ticket.getTipoSolicitud() == 1) {
            List<TicketItemDetalle> items = itemDetalleRepository.findByTicketPostventa_IdTicket(ticket.getIdTicket());
            for (TicketItemDetalle detalle : items) {
                ordenesClient.devolverItem(detalle.getIdOItemRef(), detalle.getCantidadAfectada());
            }
            ejecutarSobreSubOrdenes(ticket, false);
            ejecutarSobreItems(ticket, false);
        }
    }

    private void ejecutarSobreSubOrdenes(TicketPostventa ticket, boolean anular) {
        try {
            if (ticket.getSubOrdenesAnularJson() != null && !ticket.getSubOrdenesAnularJson().isBlank()) {
                List<Long> subOrdenes = objectMapper.readValue(
                    ticket.getSubOrdenesAnularJson(),
                    new TypeReference<List<Long>>() {}
                );
                for (Long id : subOrdenes) {
                    if (anular) {
                        ordenesClient.anularSubOrden(id);
                    } else {
                        ordenesClient.devolverSubOrden(id);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error procesando sub-ordenes: " + e.getMessage());
        }
    }

    private void ejecutarSobreItems(TicketPostventa ticket, boolean anular) {
        try {
            if (ticket.getItemsAnularJson() != null && !ticket.getItemsAnularJson().isBlank()) {
                List<Long> items = objectMapper.readValue(
                    ticket.getItemsAnularJson(),
                    new TypeReference<List<Long>>() {}
                );
                for (Long id : items) {
                    if (anular) {
                        ordenesClient.anularItem(id);
                    } else {
                        ordenesClient.devolverItem(id, null);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error procesando items: " + e.getMessage());
        }
    }

    private TicketResponseDTO toTicketResponse(TicketPostventa ticket, List<TicketItemDetalle> items) {
        List<Long> subOrdenesAAnular = Collections.emptyList();
        List<Long> itemsAAnular = Collections.emptyList();
        try {
            if (ticket.getSubOrdenesAnularJson() != null && !ticket.getSubOrdenesAnularJson().isBlank()) {
                subOrdenesAAnular = objectMapper.readValue(
                    ticket.getSubOrdenesAnularJson(),
                    new TypeReference<List<Long>>() {}
                );
            }
            if (ticket.getItemsAnularJson() != null && !ticket.getItemsAnularJson().isBlank()) {
                itemsAAnular = objectMapper.readValue(
                    ticket.getItemsAnularJson(),
                    new TypeReference<List<Long>>() {}
                );
            }
        } catch (JsonProcessingException e) {
            System.err.println("Error deserializando listas de anulación: " + e.getMessage());
        }

        // Construir lista base de ItemTicketDTOs
        List<TicketResponseDTO.ItemTicketDTO> itemDTOs = items.stream().map(item -> new TicketResponseDTO.ItemTicketDTO(
            item.getIdOItemRef(),
            item.getCantidadAfectada(),
            item.getMontoAfectado(),
            item.getNuevaTalla(),
            item.getIdProductoNuevoRef(),
            item.getNombreProducto(),
            item.getSku()
        )).collect(Collectors.toList());

        // Enriquecer items con montoAfectado == null o 0 consultando ordenes-service
        List<Long> idsSinPrecio = items.stream()
            .filter(i -> i.getMontoAfectado() == null || i.getMontoAfectado().compareTo(BigDecimal.ZERO) == 0)
            .map(TicketItemDetalle::getIdOItemRef)
            .toList();

        if (!idsSinPrecio.isEmpty()) {
            try {
                List<OrdenItemResponseDTO> detalle = ordenesClient.obtenerDetalleItemsPorIds(idsSinPrecio);
                Map<Long, BigDecimal> precioMap = detalle.stream()
                    .collect(Collectors.toMap(OrdenItemResponseDTO::idOItem, OrdenItemResponseDTO::precioUnitario));
                itemDTOs = itemDTOs.stream().map(dto -> {
                    if (dto.montoAfectado() == null || dto.montoAfectado().compareTo(BigDecimal.ZERO) == 0) {
                        BigDecimal precioReal = precioMap.get(dto.idOItemRef());
                        if (precioReal != null) {
                            return new TicketResponseDTO.ItemTicketDTO(
                                dto.idOItemRef(), dto.cantidadAfectada(), precioReal,
                                dto.nuevaTalla(), dto.idProductoNuevoRef(),
                                dto.nombreProducto(), dto.sku()
                            );
                        }
                    }
                    return dto;
                }).toList();
            } catch (Exception e) {
                System.err.println("Error enriqueciendo precios desde ordenes-service: " + e.getMessage());
            }
        }

        BigDecimal montoSaldo = itemDTOs.stream()
            .map(i -> {
                BigDecimal monto = i.montoAfectado();
                if (monto == null) monto = BigDecimal.ZERO;
                return monto.multiply(BigDecimal.valueOf(i.cantidadAfectada()));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TicketResponseDTO(
            ticket.getIdTicket(),
            ticket.getIdOMaestraRef(),
            String.valueOf(ticket.getIdClienteRef()),
            ticket.getTipoSolicitud(),
            ticket.getMotivoReclamo(),
            ticket.getEstadoTicket(),
            ticket.getFechaApertura(),
            ticket.getFechaCierre(),
            itemDTOs,
            subOrdenesAAnular,
            itemsAAnular,
            montoSaldo,
            ticket.getNuevoIdVendedor(),
            ticket.getNuevoNombreVendedor(),
            ticket.getNuevoProductoNombre(),
            ticket.getNuevoProductoPrecio(),
            ticket.getNuevoProductoId()
        );
    }
}
