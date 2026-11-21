package com.induztek.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.induztek.usuarios.dto.CambiarContrasenaRequest;
import com.induztek.usuarios.dto.UsuarioResponse;
import com.induztek.usuarios.exception.ApiException;
import com.induztek.usuarios.exception.GlobalExceptionHandler;
import com.induztek.usuarios.model.Rol;
import com.induztek.usuarios.service.UsuarioService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService servicio;

    @InjectMocks
    private UsuarioController controlador;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(controlador).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private UsuarioResponse respuesta(Rol rol) {
        return new UsuarioResponse(1L, "pedro", "Pedro Pérez", rol, null, "¿Ciudad de nacimiento?", null, null, null,
                null, null, null, "Minera Andes", null, null, null);
    }

    @Test
    void listarFiltraPorRol() throws Exception {
        when(servicio.listarPorRol(Rol.CLIENTE)).thenReturn(List.of(respuesta(Rol.CLIENTE)));

        mvc.perform(get("/api/usuarios").param("rol", "CLIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].empresaNombre").value("Minera Andes"));
    }

    @Test
    void listarSinRolDevuelveTodos() throws Exception {
        when(servicio.listarPorRol(null)).thenReturn(List.of(respuesta(Rol.TECNICO), respuesta(Rol.CLIENTE)));

        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void obtenerDevuelveElUsuario() throws Exception {
        when(servicio.obtener(1L)).thenReturn(respuesta(Rol.TECNICO));

        mvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("pedro"));
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        when(servicio.obtener(99L)).thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        mvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Usuario no encontrado"));
    }

    @Test
    void actualizarPerfilDevuelveElUsuarioActualizado() throws Exception {
        when(servicio.actualizarPerfil(eq(1L), any())).thenReturn(respuesta(Rol.CLIENTE));

        mvc.perform(put("/api/usuarios/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Pedro Pérez\",\"empresaNombre\":\"Minera Andes\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pedro Pérez"));
    }

    @Test
    void actualizarPerfilSinNombreDevuelve400() throws Exception {
        mvc.perform(put("/api/usuarios/1").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").value("El nombre completo es obligatorio"));
    }

    @Test
    void cambiarContrasenaDevuelve204() throws Exception {
        mvc.perform(put("/api/usuarios/1/contrasena").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contrasenaActual\":\"Induztek#2026\",\"nuevaContrasena\":\"Nueva#Clave9\"}"))
                .andExpect(status().isNoContent());

        verify(servicio).cambiarContrasena(1L, new CambiarContrasenaRequest("Induztek#2026", "Nueva#Clave9"));
    }
}
