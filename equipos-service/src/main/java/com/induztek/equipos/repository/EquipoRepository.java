package com.induztek.equipos.repository;

import com.induztek.equipos.model.Equipo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    boolean existsByCodigoAndInstalacionCliente(String codigo, String instalacionCliente);

    Optional<Equipo> findByCodigoAndInstalacionCliente(String codigo, String instalacionCliente);

    List<Equipo> findByClienteId(Long clienteId);
}
