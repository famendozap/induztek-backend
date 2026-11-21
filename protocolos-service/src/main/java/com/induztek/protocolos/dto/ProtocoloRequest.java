package com.induztek.protocolos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record ProtocoloRequest(
        @NotNull(message = "El equipo es obligatorio") Long equipoId,
        LocalDateTime fechaHora,
        @NotEmpty(message = "Agrega al menos una medición") List<@Valid MedicionDto> mediciones,
        String observaciones,
        byte[] fotoEvidenciaBytes,
        @NotBlank(message = "El técnico es obligatorio") String tecnicoNombre,
        @DecimalMin(value = "-30.0", message = "La temperatura mínima es -30 °C")
        @DecimalMax(value = "80.0", message = "La temperatura máxima es 80 °C") Double temperaturaAmbiente,
        @DecimalMin(value = "0.0", message = "La humedad mínima es 0 %")
        @DecimalMax(value = "100.0", message = "La humedad máxima es 100 %") Double humedadRelativa,
        String instrumentosUsados,
        String resultadoGeneral,
        String ubicacionGPS) {
}
