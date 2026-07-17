package com.unmsm.marketplace.postventa_service.controller;

import com.unmsm.marketplace.postventa_service.dto.*;
import com.unmsm.marketplace.postventa_service.service.ReclamoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reclamos")
@CrossOrigin(origins = "*")
@Tag(name = "Reclamos / Postventa", description = "Endpoints para gestion de tickets de postventa (devoluciones, cambios, reclamos, anulaciones)")
public class ReclamoController {

    private final ReclamoService reclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @GetMapping("/buscar-orden/{dni}")
    @Operation(summary = "Buscar ordenes por DNI", description = "Consulta las ordenes activas de un cliente por su DNI para iniciar un reclamo/ticket")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de ordenes del cliente"),
        @ApiResponse(responseCode = "204", description = "El cliente no tiene ordenes activas")
    })
    public ResponseEntity<List<OrdenMaestraResponseDTO>> buscarOrdenParaReclamo(@Parameter(description = "DNI del cliente") @PathVariable String dni) {
        List<OrdenMaestraResponseDTO> ordenes = reclamoService.buscarOrdenesPorDni(dni);
        return ResponseEntity.ok(ordenes);
    }

    @PostMapping("/tickets")
    @Operation(summary = "Crear ticket de postventa", description = "Crea un nuevo ticket de tipo devolucion (1), cambio de talla (2), cambio de producto (3) o anulacion (4). Marca los items como EN_RECLAMO en ordenes-service.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ticket creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos")
    })
    public ResponseEntity<TicketResponseDTO> crearTicket(@RequestBody CrearTicketRequestDTO request) {
        TicketResponseDTO ticket = reclamoService.crearTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping("/tickets/vendedor/{idVendedor}")
    @Operation(summary = "Listar tickets por vendedor", description = "Retorna los tickets asociados a un vendedor (los que contienen items o sub-ordenes de ese vendedor)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de tickets del vendedor"),
        @ApiResponse(responseCode = "204", description = "El vendedor no tiene tickets asociados")
    })
    public ResponseEntity<List<TicketResponseDTO>> listarTicketsPorVendedor(@Parameter(description = "ID del vendedor") @PathVariable Long idVendedor) {
        List<TicketResponseDTO> tickets = reclamoService.listarTicketsPorVendedor(idVendedor);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/tickets")
    @Operation(summary = "Listar todos los tickets", description = "Retorna todos los tickets del sistema, sin filtro (para superadmin)")
    @ApiResponse(responseCode = "200", description = "Lista de todos los tickets")
    public ResponseEntity<List<TicketResponseDTO>> listarTodosLosTickets() {
        List<TicketResponseDTO> tickets = reclamoService.listarTodosLosTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/tickets/{id}/historial")
    @Operation(summary = "Obtener historial de un ticket", description = "Retorna la linea de tiempo de acciones (creacion, aprobacion, rechazo) de un ticket")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historial del ticket encontrado"),
        @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    public ResponseEntity<List<HistorialResponseDTO>> obtenerHistorial(@Parameter(description = "ID del ticket") @PathVariable Long id) {
        List<HistorialResponseDTO> historial = reclamoService.obtenerHistorial(id);
        return ResponseEntity.ok(historial);
    }

    @PutMapping("/tickets/{id}/aprobar")
    @Operation(summary = "Aprobar ticket", description = "Aprueba un ticket pendiente. Ejecuta la accion segun el tipo: devolucion (genera saldo), cambio/reclamo (logistica inversa), anulacion (ejecuta anulaciones).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket aprobado y acciones ejecutadas"),
        @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    public ResponseEntity<TicketResponseDTO> aprobarTicket(
            @Parameter(description = "ID del ticket") @PathVariable Long id,
            @RequestBody AprobarRechazarRequestDTO request) {
        TicketResponseDTO ticket = reclamoService.aprobarTicket(id, request);
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/tickets/{id}/rechazar")
    @Operation(summary = "Rechazar ticket", description = "Rechaza un ticket pendiente y restaura los items a ACTIVO en ordenes-service")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket rechazado e items restaurados"),
        @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    public ResponseEntity<TicketResponseDTO> rechazarTicket(
            @Parameter(description = "ID del ticket") @PathVariable Long id,
            @RequestBody AprobarRechazarRequestDTO request) {
        TicketResponseDTO ticket = reclamoService.rechazarTicket(id, request);
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/suborden/{id}/anular")
    @Operation(summary = "Anular sub-orden", description = "Proxy: anula una sub-orden directamente en ordenes-service")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sub-orden anulada"),
        @ApiResponse(responseCode = "404", description = "Sub-orden no encontrada")
    })
    public ResponseEntity<Void> anularSubOrden(@Parameter(description = "ID de la sub-orden") @PathVariable Long id) {
        reclamoService.anularSubOrden(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/items/{id}/anular")
    @Operation(summary = "Anular item", description = "Proxy: anula un item directamente en ordenes-service")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item anulado"),
        @ApiResponse(responseCode = "404", description = "Item no encontrado")
    })
    public ResponseEntity<Void> anularItem(@Parameter(description = "ID del item") @PathVariable Long id) {
        reclamoService.anularItem(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/catalogo-productos")
    @Operation(summary = "Obtener catálogo de productos", description = "Proxy: obtiene todos los productos del catalog-service para seleccionar producto de reemplazo en cambio de producto")
    @ApiResponse(responseCode = "200", description = "Lista de productos del catálogo")
    public ResponseEntity<List<Map<String, Object>>> obtenerCatalogoProductos() {
        List<Map<String, Object>> productos = reclamoService.obtenerCatalogoProductos();
        return ResponseEntity.ok(productos);
    }

    @PostMapping("/items/detalle")
    @Operation(summary = "Obtener detalle de items", description = "Obtiene datos de producto desde ordenes-service para los IDs de items dados (para enriquecer tickets)")
    @ApiResponse(responseCode = "200", description = "Lista de items con detalle encontrados")
    public ResponseEntity<List<OrdenItemResponseDTO>> obtenerDetalleItemsPorIds(@RequestBody List<Long> ids) {
        List<OrdenItemResponseDTO> items = reclamoService.obtenerDetalleItemsPorIds(ids);
        return ResponseEntity.ok(items);
    }
}
