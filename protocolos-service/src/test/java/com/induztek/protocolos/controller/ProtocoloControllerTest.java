package com.induztek.protocolos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.induztek.protocolos.dto.MedicionDto;
import com.induztek.protocolos.dto.ProtocoloResponse;
import com.induztek.protocolos.exception.ApiException;
import com.induztek.protocolos.exception.GlobalExceptionHandler;
import com.induztek.protocolos.model.EstadoProtocolo;
import com.induztek.protocolos.service.ProtocoloService;
import java.time.LocalDateTime;
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
class ProtocoloControllerTest {

    private static final String CUERPO_VALIDO =
            "{\"equipoId\":5,\"tecnicoNombre\":\"Pedro Pérez\",\"mediciones\":[{\"nombre\":\"R 1 min\",\"valor\":\"6100\",\"unidad\":\"MΩ\"}]}";

    @Mock
    private ProtocoloService servicio;

    @InjectMocks
    private ProtocoloController controlador;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(controlador).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private ProtocoloResponse respuesta(Long id, EstadoProtocolo estado) {
        return new ProtocoloResponse(id, 5L, LocalDateTime.of(2026, 11, 19, 10, 30),
                List.of(new MedicionDto("R 1 min", "6100", "MΩ")), estado, "", null, "Pedro Pérez", null, 22.5, 45.0,
                "Megger", "CONFORME", null);
    }

    @Test
    void listarDevuelveLosProtocolos() throws Exception {
        when(servicio.listar(null)).thenReturn(List.of(respuesta(1L, EstadoProtocolo.REGISTRADO_EN_TERRENO)));

        mvc.perform(get("/api/protocolos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("REGISTRADO_EN_TERRENO"));
    }

    @Test
    void listarPuedeFiltrarPorEstado() throws Exception {
        when(servicio.listar(EstadoProtocolo.FORMALIZADO)).thenReturn(List.of(respuesta(2L, EstadoProtocolo.FORMALIZADO)));

        mvc.perform(get("/api/protocolos").param("estado", "FORMALIZADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void listarPorEquipoDevuelveElHistorial() throws Exception {
        when(servicio.listarPorEquipo(5L)).thenReturn(List.of(respuesta(2L, EstadoProtocolo.FORMALIZADO),
                respuesta(1L, EstadoProtocolo.FORMALIZADO)));

        mvc.perform(get("/api/protocolos/equipo/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].mediciones[0].valor").value("6100"));
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        when(servicio.obtener(9L)).thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Protocolo no encontrado"));

        mvc.perform(get("/api/protocolos/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Protocolo no encontrado"));
    }

    @Test
    void obtenerDevuelveElProtocolo() throws Exception {
        when(servicio.obtener(1L)).thenReturn(respuesta(1L, EstadoProtocolo.REGISTRADO_EN_TERRENO));

        mvc.perform(get("/api/protocolos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tecnicoNombre").value("Pedro Pérez"));
    }

    @Test
    void crearDevuelve201() throws Exception {
        when(servicio.crear(any())).thenReturn(respuesta(1L, EstadoProtocolo.REGISTRADO_EN_TERRENO));

        mvc.perform(post("/api/protocolos").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crearConDatosInvalidosDevuelve400() throws Exception {
        mvc.perform(post("/api/protocolos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"temperaturaAmbiente\":120,\"mediciones\":[{\"nombre\":\"R\",\"valor\":\"abc\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.equipoId").value("El equipo es obligatorio"))
                .andExpect(jsonPath("$.errores.tecnicoNombre").value("El técnico es obligatorio"))
                .andExpect(jsonPath("$.errores.temperaturaAmbiente").value("La temperatura máxima es 80 °C"))
                .andExpect(jsonPath("$.errores['mediciones[0].valor']").value("El valor debe ser numérico"));
    }

    @Test
    void crearSinMedicionesDevuelve400() throws Exception {
        mvc.perform(post("/api/protocolos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"equipoId\":5,\"tecnicoNombre\":\"Pedro\",\"mediciones\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.mediciones").value("Agrega al menos una medición"));
    }

    @Test
    void crearConEquipoInexistenteDevuelve404() throws Exception {
        when(servicio.crear(any())).thenThrow(new ApiException(HttpStatus.NOT_FOUND, "El equipo 5 no existe"));

        mvc.perform(post("/api/protocolos").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void formalizarDevuelveElProtocoloFormalizado() throws Exception {
        when(servicio.formalizar(eq(1L), any())).thenReturn(respuesta(1L, EstadoProtocolo.FORMALIZADO));

        mvc.perform(put("/api/protocolos/1/formalizar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ingenieroNombre\":\"Ana Rojas\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FORMALIZADO"));
    }

    @Test
    void formalizarSinIngenieroDevuelve400() throws Exception {
        mvc.perform(put("/api/protocolos/1/formalizar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ingenieroNombre\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.ingenieroNombre").value("El nombre del ingeniero es obligatorio"));
    }

    @Test
    void unJsonMalFormadoDevuelve400() throws Exception {
        mvc.perform(post("/api/protocolos").contentType(MediaType.APPLICATION_JSON).content("{roto"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la solicitud no es válido"));
    }
}
