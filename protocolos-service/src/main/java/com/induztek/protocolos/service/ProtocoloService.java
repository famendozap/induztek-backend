package com.induztek.protocolos.service;

import com.induztek.protocolos.client.EquipoClient;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProtocoloService {

    private final ProtocoloRepository repositorio;
    private final EquipoClient clienteEquipos;

    public ProtocoloService(ProtocoloRepository repositorio, EquipoClient clienteEquipos) {
        this.repositorio = repositorio;
        this.clienteEquipos = clienteEquipos;
    }

    public List<ProtocoloResponse> listar(EstadoProtocolo estado) {
        List<Protocolo> protocolos = estado == null ? repositorio.findAll() : repositorio.findByEstado(estado);
        return protocolos.stream().map(this::aRespuesta).toList();
    }

    public List<ProtocoloResponse> listarPorEquipo(Long equipoId) {
        return repositorio.findByEquipoIdOrderByFechaHoraDesc(equipoId).stream().map(this::aRespuesta).toList();
    }

    public ProtocoloResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public ProtocoloResponse crear(ProtocoloRequest r) {
        verificarEquipo(r.equipoId());
        Protocolo protocolo = new Protocolo();
        protocolo.setEquipoId(r.equipoId());
        protocolo.setFechaHora(r.fechaHora() != null ? r.fechaHora() : LocalDateTime.now());
        protocolo.setEstado(EstadoProtocolo.REGISTRADO_EN_TERRENO);
        protocolo.setObservaciones(r.observaciones());
        protocolo.setFotoEvidenciaBytes(r.fotoEvidenciaBytes());
        protocolo.setTecnicoNombre(r.tecnicoNombre().trim());
        protocolo.setTemperaturaAmbiente(r.temperaturaAmbiente());
        protocolo.setHumedadRelativa(r.humedadRelativa());
        protocolo.setInstrumentosUsados(r.instrumentosUsados());
        protocolo.setResultadoGeneral(r.resultadoGeneral() == null || r.resultadoGeneral().isBlank()
                ? "CONFORME" : r.resultadoGeneral().trim());
        protocolo.setUbicacionGPS(r.ubicacionGPS());
        protocolo.setMediciones(r.mediciones().stream()
                .map(m -> new Medicion(m.nombre().trim(), m.valor().trim().replace(',', '.'),
                        m.unidad() == null ? "" : m.unidad().trim()))
                .collect(Collectors.toCollection(ArrayList::new)));
        return aRespuesta(repositorio.save(protocolo));
    }

    @Transactional
    public ProtocoloResponse formalizar(Long id, FormalizarRequest r) {
        Protocolo protocolo = buscar(id);
        if (protocolo.getEstado() == EstadoProtocolo.FORMALIZADO) {
            throw new ApiException(HttpStatus.CONFLICT, "El protocolo ya está formalizado");
        }
        protocolo.setEstado(EstadoProtocolo.FORMALIZADO);
        protocolo.setIngenieroNombre(r.ingenieroNombre().trim());
        return aRespuesta(repositorio.save(protocolo));
    }

    private Protocolo buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Protocolo no encontrado"));
    }

    private void verificarEquipo(Long equipoId) {
        try {
            clienteEquipos.obtener(equipoId);
        } catch (FeignException.NotFound ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, "El equipo " + equipoId + " no existe");
        } catch (FeignException ex) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de equipos no está disponible");
        }
    }

    private ProtocoloResponse aRespuesta(Protocolo p) {
        List<MedicionDto> mediciones = p.getMediciones().stream()
                .map(m -> new MedicionDto(m.getNombre(), m.getValor(), m.getUnidad()))
                .toList();
        return new ProtocoloResponse(p.getId(), p.getEquipoId(), p.getFechaHora(), mediciones, p.getEstado(),
                p.getObservaciones(), p.getFotoEvidenciaBytes(), p.getTecnicoNombre(), p.getIngenieroNombre(),
                p.getTemperaturaAmbiente(), p.getHumedadRelativa(), p.getInstrumentosUsados(),
                p.getResultadoGeneral(), p.getUbicacionGPS());
    }
}
