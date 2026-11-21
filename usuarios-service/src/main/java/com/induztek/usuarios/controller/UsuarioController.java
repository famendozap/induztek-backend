package com.induztek.usuarios.controller;

import com.induztek.usuarios.dto.CambiarContrasenaRequest;
import com.induztek.usuarios.dto.PerfilRequest;
import com.induztek.usuarios.dto.UsuarioResponse;
import com.induztek.usuarios.model.Rol;
import com.induztek.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios")
public class UsuarioController {

    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Lista usuarios, opcionalmente filtrados por rol")
    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) Rol rol) {
        return servicio.listarPorRol(rol);
    }

    @Operation(summary = "Obtiene un usuario por su id")
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @Operation(summary = "Actualiza el perfil de un usuario")
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody PerfilRequest solicitud) {
        return servicio.actualizarPerfil(id, solicitud);
    }

    @Operation(summary = "Cambia la contraseña de un usuario")
    @PutMapping("/{id}/contrasena")
    public ResponseEntity<Void> cambiarContrasena(@PathVariable Long id,
                                                  @Valid @RequestBody CambiarContrasenaRequest solicitud) {
        servicio.cambiarContrasena(id, solicitud);
        return ResponseEntity.noContent().build();
    }
}
