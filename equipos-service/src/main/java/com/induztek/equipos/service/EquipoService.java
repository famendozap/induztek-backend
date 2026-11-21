package com.induztek.equipos.service;

import com.induztek.equipos.dto.EquipoRequest;
import com.induztek.equipos.dto.EquipoResponse;
import com.induztek.equipos.exception.ApiException;
import com.induztek.equipos.model.Equipo;
import com.induztek.equipos.repository.EquipoRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipoService {

    private final EquipoRepository repositorio;

    public EquipoService(EquipoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<EquipoResponse> listar(Long clienteId) {
        List<Equipo> equipos = clienteId == null ? repositorio.findAll() : repositorio.findByClienteId(clienteId);
        return equipos.stream().map(this::aRespuesta).toList();
    }

    public EquipoResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public EquipoResponse crear(EquipoRequest r) {
        String codigo = normalizarCodigo(r.codigo());
        String instalacion = r.instalacionCliente().trim();
        if (repositorio.existsByCodigoAndInstalacionCliente(codigo, instalacion)) {
            throw duplicado(codigo, instalacion);
        }
        Equipo equipo = new Equipo();
        copiar(equipo, r, codigo, instalacion);
        try {
            return aRespuesta(repositorio.save(equipo));
        } catch (DataIntegrityViolationException ex) {
            throw duplicado(codigo, instalacion);
        }
    }

    @Transactional
    public EquipoResponse actualizar(Long id, EquipoRequest r) {
        Equipo equipo = buscar(id);
        String codigo = normalizarCodigo(r.codigo());
        String instalacion = r.instalacionCliente().trim();
        boolean repetido = repositorio.findByCodigoAndInstalacionCliente(codigo, instalacion)
                .filter(otro -> !otro.getId().equals(id))
                .isPresent();
        if (repetido) {
            throw duplicado(codigo, instalacion);
        }
        copiar(equipo, r, codigo, instalacion);
        return aRespuesta(repositorio.save(equipo));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Equipo no encontrado");
        }
        repositorio.deleteById(id);
    }

    private Equipo buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Equipo no encontrado"));
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase();
    }

    private ApiException duplicado(String codigo, String instalacion) {
        return new ApiException(HttpStatus.CONFLICT,
                "Ya existe el equipo " + codigo + " en la instalación " + instalacion);
    }

    private String valorOPredeterminado(String valor, String predeterminado) {
        return valor == null || valor.isBlank() ? predeterminado : valor.trim();
    }

    private void copiar(Equipo equipo, EquipoRequest r, String codigo, String instalacion) {
        equipo.setCodigo(codigo);
        equipo.setTipo(r.tipo());
        equipo.setInstalacionCliente(instalacion);
        equipo.setMarca(valorOPredeterminado(r.marca(), "Genérica"));
        equipo.setModelo(valorOPredeterminado(r.modelo(), "Estándar"));
        equipo.setNumeroSerie(valorOPredeterminado(r.numeroSerie(), "N/A"));
        equipo.setPotenciaKVA(r.potenciaKVA());
        equipo.setTensionNominalKV(r.tensionNominalKV());
        equipo.setClienteId(r.clienteId());
        equipo.setClienteNombre(r.clienteNombre());
        equipo.setUbicacionGPS(r.ubicacionGPS());
    }

    private EquipoResponse aRespuesta(Equipo e) {
        return new EquipoResponse(e.getId(), e.getCodigo(), e.getTipo(), e.getInstalacionCliente(), e.getMarca(),
                e.getModelo(), e.getNumeroSerie(), e.getPotenciaKVA(), e.getTensionNominalKV(), e.getClienteId(),
                e.getClienteNombre(), e.getUbicacionGPS());
    }
}
