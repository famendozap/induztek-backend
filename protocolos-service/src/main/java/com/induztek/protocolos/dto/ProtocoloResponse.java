package com.induztek.protocolos.dto;

import com.induztek.protocolos.model.EstadoProtocolo;
import java.time.LocalDateTime;
import java.util.List;

public record ProtocoloResponse(
        Long id,
        Long equipoId,
        LocalDateTime fechaHora,
        List<MedicionDto> mediciones,
        EstadoProtocolo estado,
        String observaciones,
        byte[] fotoEvidenciaBytes,
        String tecnicoNombre,
        String ingenieroNombre,
        Double temperaturaAmbiente,
        Double humedadRelativa,
        String instrumentosUsados,
        String resultadoGeneral,
        String ubicacionGPS) {
}
