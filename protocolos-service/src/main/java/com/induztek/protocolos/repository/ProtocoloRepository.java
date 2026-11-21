package com.induztek.protocolos.repository;

import com.induztek.protocolos.model.EstadoProtocolo;
import com.induztek.protocolos.model.Protocolo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProtocoloRepository extends JpaRepository<Protocolo, Long> {

    List<Protocolo> findByEquipoIdOrderByFechaHoraDesc(Long equipoId);

    List<Protocolo> findByEstado(EstadoProtocolo estado);
}
