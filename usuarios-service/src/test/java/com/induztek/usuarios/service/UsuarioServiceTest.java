package com.induztek.usuarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.induztek.usuarios.dto.CambiarContrasenaRequest;
import com.induztek.usuarios.dto.LoginRequest;
import com.induztek.usuarios.dto.LoginResponse;
import com.induztek.usuarios.dto.PerfilRequest;
import com.induztek.usuarios.dto.RecuperarRequest;
import com.induztek.usuarios.dto.RegistroRequest;
import com.induztek.usuarios.dto.UsuarioResponse;
import com.induztek.usuarios.exception.ApiException;
import com.induztek.usuarios.model.Rol;
import com.induztek.usuarios.model.Usuario;
import com.induztek.usuarios.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final String CLAVE = "Induztek#2026";

    @Mock
    private UsuarioRepository repositorio;

    private final PasswordEncoder codificador = new BCryptPasswordEncoder(4);
    private UsuarioService servicio;

    @BeforeEach
    void preparar() {
        servicio = new UsuarioService(repositorio, codificador);
    }

    private RegistroRequest registro(String clave, Rol rol, String campoRol) {
        return new RegistroRequest("Pedro", "Pedro Pérez", clave, rol, "¿Ciudad de nacimiento?", "Santiago", null,
                rol == Rol.TECNICO ? campoRol : null, null,
                rol == Rol.INGENIERO ? campoRol : null, null,
                rol == Rol.JEFE ? campoRol : null, null,
                rol == Rol.CLIENTE ? campoRol : null, null, null, null);
    }

    private Usuario usuario(Long id, Rol rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setUsername("pedro");
        u.setNombre("Pedro Pérez");
        u.setContrasenaHash(codificador.encode(CLAVE));
        u.setRol(rol);
        u.setPreguntaSeguridad("¿Ciudad de nacimiento?");
        u.setRespuestaSeguridadHash(codificador.encode("santiago"));
        return u;
    }

    private ApiException error(Runnable accion) {
        return assertThrows(ApiException.class, accion::run);
    }

    @Test
    void registrarGuardaElUsuarioConLasClavesCifradas() {
        when(repositorio.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponse respuesta = servicio.registrar(registro(CLAVE, Rol.TECNICO, "SEC-1234"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repositorio).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals("pedro", respuesta.username());
        assertEquals(Rol.TECNICO, respuesta.rol());
        assertEquals("SEC-1234", respuesta.secCertificacion());
        assertNotEquals(CLAVE, guardado.getContrasenaHash());
        assertTrue(codificador.matches(CLAVE, guardado.getContrasenaHash()));
        assertTrue(codificador.matches("santiago", guardado.getRespuestaSeguridadHash()));
    }

    @Test
    void registrarAceptaCadaRolConSuCampoObligatorio() {
        when(repositorio.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        for (Rol rol : Rol.values()) {
            UsuarioResponse respuesta = servicio.registrar(registro(CLAVE, rol, "dato requerido"));
            assertEquals(rol, respuesta.rol());
        }
    }

    @Test
    void registrarRechazaUnUsuarioRepetido() {
        when(repositorio.existsByUsername("pedro")).thenReturn(true);

        ApiException ex = error(() -> servicio.registrar(registro(CLAVE, Rol.TECNICO, "SEC-1234")));

        assertEquals(HttpStatus.CONFLICT, ex.getEstado());
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void registrarRechazaUnaContrasenaDebil() {
        ApiException ex = error(() -> servicio.registrar(registro("clave123", Rol.TECNICO, "SEC-1234")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getEstado());
        assertTrue(ex.getMessage().contains("mayúscula"));
    }

    @Test
    void registrarExigeElCampoPropioDeCadaRol() {
        for (Rol rol : Rol.values()) {
            ApiException ex = error(() -> servicio.registrar(registro(CLAVE, rol, "  ")));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getEstado());
        }
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void loginDevuelveLosDatosYUnTokenNuevo() {
        when(repositorio.findByUsername("pedro")).thenReturn(Optional.of(usuario(7L, Rol.INGENIERO)));

        LoginResponse respuesta = servicio.login(new LoginRequest(" Pedro ", CLAVE));

        assertEquals(7L, respuesta.id());
        assertEquals(Rol.INGENIERO, respuesta.rol());
        assertFalse(respuesta.token().isBlank());
    }

    @Test
    void loginFallaConContrasenaIncorrecta() {
        when(repositorio.findByUsername("pedro")).thenReturn(Optional.of(usuario(7L, Rol.INGENIERO)));

        ApiException ex = error(() -> servicio.login(new LoginRequest("pedro", "otraClave#1")));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getEstado());
    }

    @Test
    void loginFallaConUsuarioInexistente() {
        when(repositorio.findByUsername("nadie")).thenReturn(Optional.empty());

        ApiException ex = error(() -> servicio.login(new LoginRequest("nadie", CLAVE)));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getEstado());
    }

    @Test
    void recuperarContrasenaGuardaLaClaveNuevaCifrada() {
        Usuario existente = usuario(7L, Rol.TECNICO);
        when(repositorio.findByUsername("pedro")).thenReturn(Optional.of(existente));

        servicio.recuperarContrasena(new RecuperarRequest("pedro", " SANTIAGO ", "Nueva#Clave9"));

        verify(repositorio).save(existente);
        assertTrue(codificador.matches("Nueva#Clave9", existente.getContrasenaHash()));
    }

    @Test
    void recuperarContrasenaFallaSiElUsuarioNoExiste() {
        when(repositorio.findByUsername("nadie")).thenReturn(Optional.empty());

        ApiException ex = error(() -> servicio.recuperarContrasena(new RecuperarRequest("nadie", "x", "Nueva#Clave9")));

        assertEquals(HttpStatus.NOT_FOUND, ex.getEstado());
    }

    @Test
    void recuperarContrasenaFallaConRespuestaIncorrecta() {
        when(repositorio.findByUsername("pedro")).thenReturn(Optional.of(usuario(7L, Rol.TECNICO)));

        ApiException ex = error(() -> servicio.recuperarContrasena(new RecuperarRequest("pedro", "Valparaíso", "Nueva#Clave9")));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getEstado());
    }

    @Test
    void recuperarContrasenaRechazaUnaClaveNuevaDebil() {
        when(repositorio.findByUsername("pedro")).thenReturn(Optional.of(usuario(7L, Rol.TECNICO)));

        ApiException ex = error(() -> servicio.recuperarContrasena(new RecuperarRequest("pedro", "santiago", "corta")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getEstado());
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void cambiarContrasenaActualizaLaClave() {
        Usuario existente = usuario(7L, Rol.TECNICO);
        when(repositorio.findById(7L)).thenReturn(Optional.of(existente));

        servicio.cambiarContrasena(7L, new CambiarContrasenaRequest(CLAVE, "Nueva#Clave9"));

        verify(repositorio).save(existente);
        assertTrue(codificador.matches("Nueva#Clave9", existente.getContrasenaHash()));
    }

    @Test
    void cambiarContrasenaFallaSiLaActualEsIncorrecta() {
        when(repositorio.findById(7L)).thenReturn(Optional.of(usuario(7L, Rol.TECNICO)));

        ApiException ex = error(() -> servicio.cambiarContrasena(7L, new CambiarContrasenaRequest("mala", "Nueva#Clave9")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getEstado());
    }

    @Test
    void cambiarContrasenaRechazaUnaClaveNuevaDebil() {
        when(repositorio.findById(7L)).thenReturn(Optional.of(usuario(7L, Rol.TECNICO)));

        ApiException ex = error(() -> servicio.cambiarContrasena(7L, new CambiarContrasenaRequest(CLAVE, "sinSimbolo1")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getEstado());
    }

    @Test
    void obtenerDevuelveElUsuario() {
        when(repositorio.findById(7L)).thenReturn(Optional.of(usuario(7L, Rol.CLIENTE)));

        UsuarioResponse respuesta = servicio.obtener(7L);

        assertEquals("Pedro Pérez", respuesta.nombre());
        assertEquals("¿Ciudad de nacimiento?", respuesta.preguntaSeguridad());
    }

    @Test
    void obtenerFallaSiNoExiste() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        ApiException ex = error(() -> servicio.obtener(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getEstado());
    }

    @Test
    void actualizarPerfilCambiaElNombreYLosDatosDelRol() {
        Usuario existente = usuario(7L, Rol.CLIENTE);
        when(repositorio.findById(7L)).thenReturn(Optional.of(existente));
        when(repositorio.save(existente)).thenReturn(existente);

        PerfilRequest perfil = new PerfilRequest(" Pedro Nuevo ", new byte[]{1, 2}, null, null, null, null, null, null,
                "Minera Andes", "76.123.456-7", "Av. Norte 100", "+56911112222");
        UsuarioResponse respuesta = servicio.actualizarPerfil(7L, perfil);

        assertEquals("Pedro Nuevo", respuesta.nombre());
        assertEquals("Minera Andes", respuesta.empresaNombre());
        assertEquals("+56911112222", respuesta.telefonoContacto());
        assertEquals(2, respuesta.foto().length);
    }

    @Test
    void listarPorRolFiltraSoloSiSeIndicaElRol() {
        when(repositorio.findByRol(Rol.CLIENTE)).thenReturn(List.of(usuario(1L, Rol.CLIENTE)));
        when(repositorio.findAll()).thenReturn(List.of(usuario(1L, Rol.CLIENTE), usuario(2L, Rol.TECNICO)));

        assertEquals(1, servicio.listarPorRol(Rol.CLIENTE).size());
        assertEquals(2, servicio.listarPorRol(null).size());
    }
}
