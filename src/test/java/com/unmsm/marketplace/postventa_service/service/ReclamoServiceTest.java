package com.unmsm.marketplace.postventa_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unmsm.marketplace.postventa_service.client.AnalyticsClient;
import com.unmsm.marketplace.postventa_service.client.OrdenesClient;
import com.unmsm.marketplace.postventa_service.dto.*;
import com.unmsm.marketplace.postventa_service.model.*;
import com.unmsm.marketplace.postventa_service.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReclamoServiceTest {

    @Mock private TicketPostventaRepository ticketRepository;
    @Mock private TicketItemDetalleRepository itemDetalleRepository;
    @Mock private TicketHistorialRepository historialRepository;
    @Mock private SaldoClienteRepository saldoClienteRepository;
    @Mock private LogisticaInversaRepository logisticaInversaRepository;
    @Mock private OrdenesClient ordenesClient;
    @Mock private AnalyticsClient analyticsClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ReclamoService reclamoService;

    @Test
    void buscarOrdenesPorDni_debeRetornarOrdenesDelCliente() {
        String dni = "12345678";
        List<OrdenMaestraResponseDTO> stubList = List.of(
            new OrdenMaestraResponseDTO(1L, "Cliente Test", dni, "Yape", 1,
                new BigDecimal("100.00"), null, List.of())
        );
        when(ordenesClient.buscarOrdenesPorDni(dni)).thenReturn(stubList);

        List<OrdenMaestraResponseDTO> resultado = reclamoService.buscarOrdenesPorDni(dni);

        assertEquals(1, resultado.size());
        assertEquals("Cliente Test", resultado.get(0).clienteNombre());
    }

    @Test
    void anularItem_debeLlamarAlFeignClient() {
        Long idItem = 1L;

        reclamoService.anularItem(idItem);

        verify(ordenesClient, times(1)).anularItem(idItem);
    }

    @Test
    void crearTicket_Tipo1_StubMockSpy() throws Exception {
        List<CrearTicketRequestDTO.ItemReclamoDTO> items = List.of(
            new CrearTicketRequestDTO.ItemReclamoDTO(1L, 1, new BigDecimal("50.00"), "Producto A", "SKU-001")
        );
        CrearTicketRequestDTO request = new CrearTicketRequestDTO(
            100L, "12345678", 1, "Producto defectuoso",
            null, null, items, List.of(1L, 2L), null
        );

        TicketPostventa ticketGuardado = new TicketPostventa();
        ticketGuardado.setIdTicket(1L);
        ticketGuardado.setIdOMaestraRef(100L);
        ticketGuardado.setIdClienteRef(12345678L);
        ticketGuardado.setTipoSolicitud(1);
        ticketGuardado.setMotivoReclamo("Producto defectuoso");
        ticketGuardado.setEstadoTicket(1);
        ticketGuardado.setFechaApertura(LocalDateTime.now());

        when(ticketRepository.save(any())).thenReturn(ticketGuardado);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketGuardado));
        TicketItemDetalle detalle = new TicketItemDetalle();
        detalle.setIdOItemRef(1L);
        detalle.setCantidadAfectada(1);
        detalle.setMontoAfectado(new BigDecimal("50.00"));
        detalle.setNombreProducto("Producto A");
        detalle.setSku("SKU-001");
        when(itemDetalleRepository.saveAll(any())).thenReturn(List.of(detalle));

        TicketResponseDTO resultado = reclamoService.crearTicket(request);

        assertNotNull(resultado);
        verify(analyticsClient, times(1)).sendEvent(any(), any(AnalyticsEvent.class));
        verify(ordenesClient, times(1)).marcarItemsEnReclamo(anyList());
        verify(historialRepository, times(1)).save(any());
        verify(objectMapper, atLeastOnce()).writeValueAsString(any());
    }

    @Test
    void aprobarTicket_tipo4_debeEjecutarAnulaciones() throws Exception {
        Long idTicket = 1L;
        TicketPostventa ticket = new TicketPostventa();
        ticket.setIdTicket(idTicket);
        ticket.setTipoSolicitud(4);
        ticket.setEstadoTicket(1);
        ticket.setSubOrdenesAnularJson("[1,2]");
        ticket.setIdClienteRef(1L);

        when(ticketRepository.findById(idTicket)).thenReturn(Optional.of(ticket));
        when(itemDetalleRepository.findByTicketPostventa_IdTicket(idTicket)).thenReturn(List.of());
        when(ticketRepository.save(any())).thenReturn(ticket);

        List<Long> subOrdenes = List.of(1L, 2L);
        doReturn(subOrdenes).when(objectMapper).readValue(anyString(), any(TypeReference.class));

        AprobarRechazarRequestDTO request = new AprobarRechazarRequestDTO(1L, "Aprobado - anulación total");

        TicketResponseDTO resultado = reclamoService.aprobarTicket(idTicket, request);

        assertNotNull(resultado);
        verify(ordenesClient, times(2)).anularSubOrden(anyLong());
        verify(historialRepository, times(1)).save(any());
    }
}
