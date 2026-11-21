package com.induztek.equipos.dto;

import com.induztek.equipos.model.TipoEquipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EquipoRequest(
        @NotBlank(message = "El código del equipo es obligatorio") String codigo,
        @NotNull(message = "El tipo de equipo es obligatorio") TipoEquipo tipo,
        @NotBlank(message = "La instalación del cliente es obligatoria") String instalacionCliente,
        String marca,
        String modelo,
        String numeroSerie,
        @Positive(message = "La potencia debe ser mayor que 0") Double potenciaKVA,
        @Positive(message = "La tensión debe ser mayor que 0") Double tensionNominalKV,
        Long clienteId,
        String clienteNombre,
        String ubicacionGPS) {
}
