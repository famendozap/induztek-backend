package com.induztek.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.induztek.usuarios.dto.LoginResponse;
import com.induztek.usuarios.dto.RecuperarRequest;
import com.induztek.usuarios.dto.RegistroRequest;
import com.induztek.usuarios.dto.UsuarioResponse;
import com.induztek.usuarios.exception.ApiException;
import com.induztek.usuarios.exception.GlobalExceptionHandler;
import com.induztek.usuarios.model.Rol;
import com.induztek.usuarios.service.UsuarioService;
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
class AuthControllerTest {

    @Mock
    private UsuarioService servicio;

    @InjectMocks
    private AuthController controlador;

    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(controlador).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void loginDevuelve200ConElToken() throws Exception {
        when(servicio.login(any())).thenReturn(new LoginResponse(1L, "pedro", "Pedro", Rol.TECNICO, "token-123"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"pedro\",\"contrasena\":\"Induztek#2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.rol").value("TECNICO"));
    }

    @Test
    void loginSinCamposDevuelve400ConLosErrores() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"contrasena\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.username").value("Ingresa tu usuario"))
                .andExpect(jsonPath("$.errores.contrasena").value("Ingresa tu contraseña"));
    }

    @Test
    void loginConCredencialesInvalidasDevuelve401() throws Exception {
        when(servicio.login(any())).thenThrow(new ApiException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"pedro\",\"contrasena\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos"));
    }

    @Test
    void registroDevuelve201ConElUsuarioCreado() throws Exception {
        RegistroRequest solicitud = new RegistroRequest("pedro", "Pedro Pérez", "Induztek#2026", Rol.TECNICO,
                "¿Ciudad de nacimiento?", "Santiago", null, "SEC-1234", null, null, null, null, null, null, null, null, null);
        when(servicio.registrar(any())).thenReturn(new UsuarioResponse(1L, "pedro", "Pedro Pérez", Rol.TECNICO, null,
                "¿Ciudad de nacimiento?", "SEC-1234", null, null, null, null, null, null, null, null, null));

        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.secCertificacion").value("SEC-1234"));
    }

    @Test
    void registroSinRolDevuelve400() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"pedro\",\"nombre\":\"Pedro\",\"contrasena\":\"x\",\"preguntaSeguridad\":\"p\",\"respuestaSeguridad\":\"r\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.rol").value("El rol es obligatorio"));
    }

    @Test
    void recuperarDevuelve204() throws Exception {
        RecuperarRequest solicitud = new RecuperarRequest("pedro", "Santiago", "Nueva#Clave9");

        mvc.perform(post("/api/auth/recuperar").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(solicitud)))
                .andExpect(status().isNoContent());

        verify(servicio).recuperarContrasena(solicitud);
    }

    @Test
    void unJsonMalFormadoDevuelve400() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la solicitud no es válido"));
    }
}
