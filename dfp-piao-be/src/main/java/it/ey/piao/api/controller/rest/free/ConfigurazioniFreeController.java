package it.ey.piao.api.controller.rest.free;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.piao.api.service.IConfigurazioniService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * Endpoint "free" (pubblici, no auth) per leggere configurazioni utili al bootstrap del FE.
 * Espone solamente le configurazioni necessarie prima del login (es. date PIAO).
 * <p>
 * La logica vera è nel service {@link IConfigurazioniService}; questo controller fa solo
 * da entry point HTTP.
 */
@ApiV1Controller("/configurazioni/free")
public class ConfigurazioniFreeController
{
    private final IConfigurazioniService configurazioniService;

    public ConfigurazioniFreeController(IConfigurazioniService configurazioniService)
    {
        this.configurazioniService = configurazioniService;
    }

    /**
     * Recupera in un'unica chiamata le due configurazioni di date PIAO richieste dal FE
     * al bootstrap (DATA_COMPILAZIONE_PIAO, DATA_SCADENZA_PIAO).
     */
    @GetMapping("/piao-dates")
    public ResponseEntity<Map<String, String>> getPiaoDates()
    {
        return ResponseEntity.ok(configurazioniService.getPiaoDates());
    }
}
