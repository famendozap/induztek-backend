package com.induztek.equipos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.induztek.equipos.dto.EquipoResponse;
import com.induztek.equipos.exception.ApiException;
import com.induztek.equipos.exception.GlobalExceptionHandler;
import com.induztek.equipos.model.TipoEquipo;
import com.induztek.equipos.service.EquipoService;
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
class EquipoControllerTest {

    private static final String CUERPO_VALIDO =
            "{\"codigo\":\"TR-6271\",\"tipo\":\"TRANSFORMADOR\",\"instalacionCliente\":\"Planta Lampa\",\"potenciaKVA\":500}";

    @Mock
    private EquipoService servicio;

    @InjectMocks
    private EquipoController controlador;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(controlador).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private EquipoResponse respuesta() {
        return new EquipoResponse(1L, "TR-6271", TipoEquipo.TRANSFORMADOR, "Planta Lampa", "ABB", "T-500", "S-1", 500.0,
                13.2, 3L, "Minera Andes", null);
    }

    @Test
    void listarDevuelveLosEquipos() throws Exception {
        when(servicio.listar(null)).thenReturn(List.of(respuesta()));

        mvc.perform(get("/api/equipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("TR-6271"))
                .andExpect(jsonPath("$[0].tipo").value("TRANSFORMADOR"));
    }

    @Test
    void listarPuedeFiltrarPorCliente() throws Exception {
        when(servicio.listar(3L)).thenReturn(List.of(respuesta()));

        mvc.perform(get("/api/equipos").param("clienteId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void obtenerDevuelveElEquipo() throws Exception {
        when(servicio.obtener(1L)).thenReturn(respuesta());

        mvc.perform(get("/api/equipos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instalacionCliente").value("Planta Lampa"));
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        when(servicio.obtener(9L)).thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Equipo no encontrado"));

        mvc.perform(get("/api/equipos/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Equipo no encontrado"));
    }

    @Test
    void crearDevuelve201() throws Exception {
        when(servicio.crear(any())).thenReturn(respuesta());

        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crearConDatosInvalidosDevuelve400() throws Exception {
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"\",\"instalacionCliente\":\"\",\"potenciaKVA\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.codigo").value("El código del equipo es obligatorio"))
                .andExpect(jsonPath("$.errores.tipo").value("El tipo de equipo es obligatorio"))
                .andExpect(jsonPath("$.errores.potenciaKVA").value("La potencia debe ser mayor que 0"));
    }

    @Test
    void crearDuplicadoDevuelve409() throws Exception {
        when(servicio.crear(any())).thenThrow(new ApiException(HttpStatus.CONFLICT, "Ya existe el equipo TR-6271"));

        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizarDevuelveElEquipoModificado() throws Exception {
        when(servicio.actualizar(eq(1L), any())).thenReturn(respuesta());

        mvc.perform(put("/api/equipos/1").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marca").value("ABB"));
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mvc.perform(delete("/api/equipos/1")).andExpect(status().isNoContent());

        verify(servicio).eliminar(1L);
    }

    @Test
    void unJsonMalFormadoDevuelve400() throws Exception {
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content("{roto"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la solicitud no es válido"));
    }
}
