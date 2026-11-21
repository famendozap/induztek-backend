package com.induztek.protocolos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "equipos-service")
public interface EquipoClient {

    @GetMapping("/api/equipos/{id}")
    EquipoDto obtener(@PathVariable("id") Long id);
}
