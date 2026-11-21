package com.induztek.usuarios.dto;

import com.induztek.usuarios.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "El usuario es obligatorio")
        @Size(min = 3, max = 60, message = "El usuario debe tener entre 3 y 60 caracteres")
        String username,
        @NotBlank(message = "El nombre completo es obligatorio") String nombre,
        @NotBlank(message = "La contraseña es obligatoria") String contrasena,
        @NotNull(message = "El rol es obligatorio") Rol rol,
        @NotBlank(message = "La pregunta de seguridad es obligatoria") String preguntaSeguridad,
        @NotBlank(message = "La respuesta de seguridad es obligatoria") String respuestaSeguridad,
        byte[] foto,
        String secCertificacion,
        String especialidadTecnica,
        String rutColegiatura,
        String registroProfesional,
        String departamentoSucursal,
        String codigoAutorizacion,
        String empresaNombre,
        String rutEmpresa,
        String direccionEmpresa,
        String telefonoContacto) {
}
