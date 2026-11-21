package com.induztek.protocolos.controller;

import com.induztek.protocolos.dto.FormalizarRequest;
import com.induztek.protocolos.dto.ProtocoloRequest;
import com.induztek.protocolos.dto.ProtocoloResponse;
import com.induztek.protocolos.model.EstadoProtocolo;
import com.induztek.protocolos.service.ProtocoloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/protocolos")
@Tag(name = "Protocolos")
public class ProtocoloController {

    private final ProtocoloService servicio;

    public ProtocoloController(ProtocoloService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Lista los protocolos, opcionalmente por estado")
    @GetMapping
    public List<ProtocoloResponse> listar(@RequestParam(required = false) EstadoProtocolo estado) {
        return servicio.listar(estado);
    }

    @Operation(summary = "Lista el historial de protocolos de un equipo, del más reciente al más antiguo")
    @GetMapping("/equipo/{equipoId}")
    public List<ProtocoloResponse> listarPorEquipo(@PathVariable Long equipoId) {
        return servicio.listarPorEquipo(equipoId);
    }

    @Operation(summary = "Obtiene un protocolo por su id")
    @GetMapping("/{id}")
    public ProtocoloResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @Operation(summary = "Registra un protocolo en terreno")
    @PostMapping
    public ResponseEntity<ProtocoloResponse> crear(@Valid @RequestBody ProtocoloRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(solicitud));
    }

    @Operation(summary = "Formaliza un protocolo registrado en terreno")
    @PutMapping("/{id}/formalizar")
    public ProtocoloResponse formalizar(@PathVariable Long id, @Valid @RequestBody FormalizarRequest solicitud) {
        return servicio.formalizar(id, solicitud);
    }
}
