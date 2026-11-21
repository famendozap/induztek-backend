package com.induztek.usuarios.controller;

import com.induztek.usuarios.dto.LoginRequest;
import com.induztek.usuarios.dto.LoginResponse;
import com.induztek.usuarios.dto.RecuperarRequest;
import com.induztek.usuarios.dto.RegistroRequest;
import com.induztek.usuarios.dto.UsuarioResponse;
import com.induztek.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación")
public class AuthController {

    private final UsuarioService servicio;

    public AuthController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Inicia sesión con usuario y contraseña")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest solicitud) {
        return ResponseEntity.ok(servicio.login(solicitud));
    }

    @Operation(summary = "Registra un usuario nuevo con su rol")
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registro(@Valid @RequestBody RegistroRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrar(solicitud));
    }

    @Operation(summary = "Recupera la contraseña con la respuesta de seguridad")
    @PostMapping("/recuperar")
    public ResponseEntity<Void> recuperar(@Valid @RequestBody RecuperarRequest solicitud) {
        servicio.recuperarContrasena(solicitud);
        return ResponseEntity.noContent().build();
    }
}
