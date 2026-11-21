package com.induztek.protocolos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.induztek.protocolos.client.EquipoClient;
import com.induztek.protocolos.client.EquipoDto;
import com.induztek.protocolos.dto.FormalizarRequest;
import com.induztek.protocolos.dto.MedicionDto;
import com.induztek.protocolos.dto.ProtocoloRequest;
import com.induztek.protocolos.dto.ProtocoloResponse;
import com.induztek.protocolos.exception.ApiException;
import com.induztek.protocolos.model.EstadoProtocolo;
import com.induztek.protocolos.model.Medicion;
import com.induztek.protocolos.model.Protocolo;
import com.induztek.protocolos.repository.ProtocoloRepository;
import feign.FeignException;
import feign.Request;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ProtocoloServiceTest {

    @Mock
    private ProtocoloRepository repositorio;

    @Mock
    private EquipoClient clienteEquipos;

    private ProtocoloService servicio;

    @BeforeEach
    void preparar() {
        servicio = new ProtocoloService(repositorio, clienteEquipos);
    }

    private ProtocoloRequest solicitud(LocalDateTime fecha, String resultado) {
        return new ProtocoloRequest(5L, fecha,
                List.of(new MedicionDto(" R 1 min ", "6,1", " MΩ "), new MedicionDto("Tensión", "5", null)),
                "Sin novedades", new byte[]{1}, " Pedro Pérez ", 22.5, 45.0, "Megger MIT515", resultado, "-33.3,-70.8");
    }

    private Protocolo protocolo(Long id, EstadoProtocolo estado) {
        Protocolo p = new Protocolo();
        p.setId(id);
        p.setEquipoId(5L);
        p.setFechaHora(LocalDateTime.of(2026, 11, 19, 10, 30));
        p.setEstado(estado);
        p.setTecnicoNombre("Pedro Pérez");
        p.setMediciones(List.of(new Medicion("R 1 min", "6100", "MΩ")));
        return p;
    }

    private FeignException errorFeign(int estado) {
        Request peticion = Request.create(Request.HttpMethod.GET, "http://equipos-service/api/equipos/5", Map.of(), null,
                StandardCharsets.UTF_8, null);
        Response respuesta = Response.builder().status(estado).reason("error").request(peticion).headers(Map.of()).build();
        return FeignException.errorStatus("EquipoClient#obtener(Long)", respuesta);
    }

    @Test
    void crearRegistraElProtocoloEnTerrenoConLosDatosNormalizados() {
        when(clienteEquipos.obtener(5L)).thenReturn(new EquipoDto(5L, "TR-1"));
        when(repositorio.save(any(Protocolo.class))).thenAnswer(i -> i.getArgument(0));

        ProtocoloResponse respuesta = servicio.crear(solicitud(null, null));

        assertEquals(EstadoProtocolo.REGISTRADO_EN_TERRENO, respuesta.estado());
        assertNotNull(respuesta.fechaHora());
        assertEquals("Pedro Pérez", respuesta.tecnicoNombre());
        assertEquals("CONFORME", respuesta.resultadoGeneral());
        assertEquals(2, respuesta.mediciones().size());
        assertEquals("R 1 min", respuesta.mediciones().get(0).nombre());
        assertEquals("6.1", respuesta.mediciones().get(0).valor());
        assertEquals("MΩ", respuesta.mediciones().get(0).unidad());
        assertEquals("", respuesta.mediciones().get(1).unidad());
        assertEquals(22.5, respuesta.temperaturaAmbiente());
        assertEquals(45.0, respuesta.humedadRelativa());
        assertEquals("Megger MIT515", respuesta.instrumentosUsados());
        assertEquals("-33.3,-70.8", respuesta.ubicacionGPS());
        assertEquals(1, respuesta.fotoEvidenciaBytes().length);
    }

    @Test
    void crearConservaLaFechaYElResultadoIndicados() {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 18, 9, 0);
        when(clienteEquipos.obtener(5L)).thenReturn(new EquipoDto(5L, "TR-1"));
        when(repositorio.save(any(Protocolo.class))).thenAnswer(i -> i.getArgument(0));

        ProtocoloResponse respuesta = servicio.crear(solicitud(fecha, "NO CONFORME"));

        assertEquals(fecha, respuesta.fechaHora());
        assertEquals("NO CONFORME", respuesta.resultadoGeneral());
    }

    @Test
    void crearFallaSiElEquipoNoExiste() {
        when(clienteEquipos.obtener(5L)).thenThrow(errorFeign(404));

        ApiException ex = assertThrows(ApiException.class, () -> servicio.crear(solicitud(null, null)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getEstado());
        verify(repositorio, never()).save(any(Protocolo.class));
    }

    @Test
    void crearFallaSiElServicioDeEquiposNoResponde() {
        when(clienteEquipos.obtener(5L)).thenThrow(errorFeign(500));

        ApiException ex = assertThrows(ApiException.class, () -> servicio.crear(solicitud(null, null)));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getEstado());
        verify(repositorio, never()).save(any(Protocolo.class));
    }

    @Test
    void listarFiltraPorEstadoSoloSiSeIndica() {
        when(repositorio.findByEstado(EstadoProtocolo.FORMALIZADO))
                .thenReturn(List.of(protocolo(1L, EstadoProtocolo.FORMALIZADO)));
        when(repositorio.findAll()).thenReturn(List.of(protocolo(1L, EstadoProtocolo.FORMALIZADO),
                protocolo(2L, EstadoProtocolo.REGISTRADO_EN_TERRENO)));

        assertEquals(1, servicio.listar(EstadoProtocolo.FORMALIZADO).size());
        assertEquals(2, servicio.listar(null).size());
    }

    @Test
    void listarPorEquipoDevuelveElHistorial() {
        when(repositorio.findByEquipoIdOrderByFechaHoraDesc(5L))
                .thenReturn(List.of(protocolo(2L, EstadoProtocolo.REGISTRADO_EN_TERRENO), protocolo(1L, EstadoProtocolo.FORMALIZADO)));

        List<ProtocoloResponse> historial = servicio.listarPorEquipo(5L);

        assertEquals(2, historial.size());
        assertEquals(2L, historial.get(0).id());
    }

    @Test
    void obtenerDevuelveElProtocolo() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(protocolo(1L, EstadoProtocolo.REGISTRADO_EN_TERRENO)));

        ProtocoloResponse respuesta = servicio.obtener(1L);

        assertEquals("6100", respuesta.mediciones().get(0).valor());
    }

    @Test
    void obtenerFallaSiNoExiste() {
        when(repositorio.findById(9L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> servicio.obtener(9L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getEstado());
    }

    @Test
    void formalizarCambiaElEstadoYRegistraAlIngeniero() {
        Protocolo existente = protocolo(1L, EstadoProtocolo.REGISTRADO_EN_TERRENO);
        when(repositorio.findById(1L)).thenReturn(Optional.of(existente));
        when(repositorio.save(existente)).thenReturn(existente);

        ProtocoloResponse respuesta = servicio.formalizar(1L, new FormalizarRequest(" Ana Rojas "));

        assertEquals(EstadoProtocolo.FORMALIZADO, respuesta.estado());
        assertEquals("Ana Rojas", respuesta.ingenieroNombre());
    }

    @Test
    void formalizarRechazaUnProtocoloYaFormalizado() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(protocolo(1L, EstadoProtocolo.FORMALIZADO)));

        ApiException ex = assertThrows(ApiException.class, () -> servicio.formalizar(1L, new FormalizarRequest("Ana Rojas")));

        assertEquals(HttpStatus.CONFLICT, ex.getEstado());
        verify(repositorio, never()).save(any(Protocolo.class));
    }

    @Test
    void formalizarFallaSiNoExiste() {
        when(repositorio.findById(9L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> servicio.formalizar(9L, new FormalizarRequest("Ana Rojas")));

        assertEquals(HttpStatus.NOT_FOUND, ex.getEstado());
    }
}
