package com.induztek.protocolos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MedicionDto(
        @NotBlank(message = "El parámetro es obligatorio") String nombre,
        @NotBlank(message = "El valor es obligatorio")
        @Pattern(regexp = "^-?\\d+([.,]\\d+)?$", message = "El valor debe ser numérico") String valor,
        String unidad) {
}
