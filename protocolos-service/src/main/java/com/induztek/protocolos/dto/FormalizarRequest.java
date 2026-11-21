package com.induztek.protocolos.dto;

import jakarta.validation.constraints.NotBlank;

public record FormalizarRequest(@NotBlank(message = "El nombre del ingeniero es obligatorio") String ingenieroNombre) {
}
