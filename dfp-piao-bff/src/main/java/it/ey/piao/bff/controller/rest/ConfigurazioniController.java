package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.ConfigurazioniDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IConfigurazioniService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/configurazioni")
public class ConfigurazioniController
{
    private final IConfigurazioniService configurazioniService;

    public ConfigurazioniController(IConfigurazioniService configurazioniService)
    {
        this.configurazioniService = configurazioniService;
    }

    @GetMapping()
    public Mono<ResponseEntity<GenericResponseDTO<List<ConfigurazioniDTO>>>> getAllConfigurazioni()
    {
        return configurazioniService.getAllConfigurazioni()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/date")
    public Mono<ResponseEntity<GenericResponseDTO<List<String>>>> getPromemoriaByMilestone()
    {
        return configurazioniService.getAllDataDaAndDataA()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/value")
    public Mono<ResponseEntity<GenericResponseDTO<String>>> getValoreFromCodice(@RequestParam String codice)
    {
        return configurazioniService.getValoreFromCodice(codice)
            .map(ResponseEntity::ok);
    }

    @PutMapping()
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> setValoreFromCodice(@RequestBody ConfigurazioniDTO dto)
    {
        return configurazioniService.setValoreFromCodice(dto.getCodice(), dto.getValore()).map(response -> ResponseEntity.status(200).body(response));
    }
}
