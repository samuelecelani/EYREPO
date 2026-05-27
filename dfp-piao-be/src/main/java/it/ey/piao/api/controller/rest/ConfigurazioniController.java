package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.ConfigurazioniDTO;
import it.ey.piao.api.service.IConfigurazioniService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
    public ResponseEntity<List<ConfigurazioniDTO>> getAllConfigurazioni()
    {
        return ResponseEntity.ok(configurazioniService.getAllConfigurazioni());
    }


    @GetMapping("/value")
    public ResponseEntity<String> getValoreFromCodice(@RequestParam String codice)
    {
        return ResponseEntity.ok(configurazioniService.getValoreFromCodice(codice));
    }

    @PutMapping()
    public ResponseEntity<Void> setValoreFromCodice(@RequestBody ConfigurazioniDTO dto)
    {
        configurazioniService.setValoreFromCodice(dto.getCodice(), dto.getValore());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/codice/{codice}")
    public ResponseEntity<ConfigurazioniDTO> getConfigurazioneByCodice(@PathVariable String codice)
    {
        ConfigurazioniDTO dto = configurazioniService.getConfigurazioneByCodice(codice);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }
}
