package com.induztek.usuarios.dto;

import com.induztek.usuarios.model.Rol;

public record UsuarioResponse(
        Long id,
        String username,
        String nombre,
        Rol rol,
        byte[] foto,
        String preguntaSeguridad,
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
