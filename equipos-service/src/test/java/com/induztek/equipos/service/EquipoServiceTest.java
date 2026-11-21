package com.induztek.equipos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.induztek.equipos.dto.EquipoRequest;
import com.induztek.equipos.dto.EquipoResponse;
import com.induztek.equipos.exception.ApiException;
import com.induztek.equipos.model.Equipo;
import com.induztek.equipos.model.TipoEquipo;
import com.induztek.equipos.repository.EquipoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class EquipoServiceTest {

    @Mock
    private EquipoRepository repositorio;

    private EquipoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new EquipoService(repositorio);
    }

    private EquipoRequest solicitud(String codigo, String marca) {
        return new EquipoRequest(codigo, TipoEquipo.TRANSFORMADOR, " Planta Lampa ", marca, null, "  ", 500.0, 13.2, 3L,
                "Minera Andes", "-33.30,-70.80");
    }

    private Equipo equipo(Long id, String codigo, String instalacion) {
        Equipo e = new Equipo();
        e.setId(id);
        e.setCodigo(codigo);
        e.setTipo(TipoEquipo.TRANSFORMADOR);
        e.setInstalacionCliente(instalacion);
        e.setMarca("ABB");
        e.setModelo("T-500");
        e.setNumeroSerie("S-1");
        e.setClienteId(3L);
        return e;
    }

    @Test
    void crearNormalizaElCodigoYAplicaValoresPredeterminados() {
        when(repositorio.save(any(Equipo.class))).thenAnswer(i -> i.getArgument(0));

        EquipoResponse respuesta = servicio.crear(solicitud(" tr-6271 ", null));

        assertEquals("TR-6271", respuesta.codigo());
        assertEquals("Planta Lampa", respuesta.instalacionCliente());
        assertEquals("Genérica", respuesta.marca());
        assertEquals("Estándar", respuesta.modelo());
        assertEquals("N/A", respuesta.numeroSerie());
        assertEquals(500.0, respuesta.potenciaKVA());
        assertEquals(13.2, respuesta.tensionNominalKV());
        assertEquals("Minera Andes", respuesta.clienteNombre());
        assertEquals("-33.30,-70.80", respuesta.ubicacionGPS());
    }

    @Test
    void crearConservaLaMarcaIndicada() {
        when(repositorio.save(any(Equipo.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals("Siemens", servicio.crear(solicitud("TR-1", "Siemens")).marca());
    }

    @Test
    void crearRechazaUnCodigoRepetidoEnLaMismaInstalacion() {
        when(repositorio.existsByCodigoAndInstalacionCliente("TR-1", "Planta Lampa")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> servicio.crear(solicitud("tr-1", null)));

        assertEquals(HttpStatus.CONFLICT, ex.getEstado());
        verify(repositorio, never()).save(any(Equipo.class));
    }

    @Test
    void crearTraduceUnConflictoDeLaBaseDeDatosEnUn409() {
        when(repositorio.save(any(Equipo.class))).thenThrow(new DataIntegrityViolationException("duplicado"));

        ApiException ex = assertThrows(ApiException.class, () -> servicio.crear(solicitud("TR-1", null)));

        assertEquals(HttpStatus.CONFLICT, ex.getEstado());
    }

    @Test
    void listarFiltraPorClienteSoloSiSeIndica() {
        when(repositorio.findByClienteId(3L)).thenReturn(List.of(equipo(1L, "TR-1", "Planta Lampa")));
        when(repositorio.findAll()).thenReturn(List.of(equipo(1L, "TR-1", "Planta Lampa"), equipo(2L, "TR-2", "Planta Lampa")));

        assertEquals(1, servicio.listar(3L).size());
        assertEquals(2, servicio.listar(null).size());
    }

    @Test
    void obtenerDevuelveElEquipo() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(equipo(1L, "TR-1", "Planta Lampa")));

        assertEquals("TR-1", servicio.obtener(1L).codigo());
    }

    @Test
    void obtenerFallaSiNoExiste() {
        when(repositorio.findById(9L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> servicio.obtener(9L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getEstado());
    }

    @Test
    void actualizarCambiaLosDatosDelEquipo() {
        Equipo existente = equipo(1L, "TR-1", "Planta Lampa");
        when(repositorio.findById(1L)).thenReturn(Optional.of(existente));
        when(repositorio.findByCodigoAndInstalacionCliente("TR-9", "Planta Lampa")).thenReturn(Optional.empty());
        when(repositorio.save(existente)).thenReturn(existente);

        EquipoResponse respuesta = servicio.actualizar(1L, solicitud("tr-9", "Siemens"));

        assertEquals("TR-9", respuesta.codigo());
        assertEquals("Siemens", respuesta.marca());
    }

    @Test
    void actualizarPermiteConservarElMismoCodigo() {
        Equipo existente = equipo(1L, "TR-1", "Planta Lampa");
        when(repositorio.findById(1L)).thenReturn(Optional.of(existente));
        when(repositorio.findByCodigoAndInstalacionCliente("TR-1", "Planta Lampa")).thenReturn(Optional.of(existente));
        when(repositorio.save(existente)).thenReturn(existente);

        assertEquals("TR-1", servicio.actualizar(1L, solicitud("TR-1", null)).codigo());
    }

    @Test
    void actualizarRechazaUnCodigoQueYaUsaOtroEquipo() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(equipo(1L, "TR-1", "Planta Lampa")));
        when(repositorio.findByCodigoAndInstalacionCliente("TR-2", "Planta Lampa"))
                .thenReturn(Optional.of(equipo(2L, "TR-2", "Planta Lampa")));

        ApiException ex = assertThrows(ApiException.class, () -> servicio.actualizar(1L, solicitud("TR-2", null)));

        assertEquals(HttpStatus.CONFLICT, ex.getEstado());
    }

    @Test
    void actualizarFallaSiNoExiste() {
        when(repositorio.findById(9L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> servicio.actualizar(9L, solicitud("TR-1", null)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getEstado());
    }

    @Test
    void eliminarBorraElEquipoExistente() {
        when(repositorio.existsById(1L)).thenReturn(true);

        servicio.eliminar(1L);

        verify(repositorio).deleteById(1L);
    }

    @Test
    void eliminarFallaSiNoExiste() {
        when(repositorio.existsById(9L)).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> servicio.eliminar(9L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getEstado());
        verify(repositorio, never()).deleteById(any());
    }
}
