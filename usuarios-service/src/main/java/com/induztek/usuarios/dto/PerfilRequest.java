package com.induztek.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

public record PerfilRequest(
        @NotBlank(message = "El nombre completo es obligatorio") String nombre,
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
