package com.induztek.equipos.dto;

import com.induztek.equipos.model.TipoEquipo;

public record EquipoResponse(
        Long id,
        String codigo,
        TipoEquipo tipo,
        String instalacionCliente,
        String marca,
        String modelo,
        String numeroSerie,
        Double potenciaKVA,
        Double tensionNominalKV,
        Long clienteId,
        String clienteNombre,
        String ubicacionGPS) {
}
