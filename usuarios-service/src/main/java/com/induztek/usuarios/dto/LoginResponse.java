package com.induztek.usuarios.dto;

import com.induztek.usuarios.model.Rol;

public record LoginResponse(Long id, String username, String nombre, Rol rol, String token) {
}
