package com.induztek.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

public record RecuperarRequest(
        @NotBlank(message = "El usuario es obligatorio") String username,
        @NotBlank(message = "La respuesta de seguridad es obligatoria") String respuestaSeguridad,
        @NotBlank(message = "La nueva contraseña es obligatoria") String nuevaContrasena) {
}
