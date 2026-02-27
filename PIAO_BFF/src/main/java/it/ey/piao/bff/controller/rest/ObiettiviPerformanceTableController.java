package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IObiettiviPerformanceTableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.util.Map;

@ApiV1Controller("/obiettivi-performance-table")
public class ObiettiviPerformanceTableController {

    private final IObiettiviPerformanceTableService obiettiviPerformanceTableService;

    public ObiettiviPerformanceTableController(IObiettiviPerformanceTableService obiettiviPerformanceTableService) {
        this.obiettiviPerformanceTableService = obiettiviPerformanceTableService;
    }

    /**
     * Endpoint per recuperare la tabella dinamica degli obiettivi di performance
     *
     * @return ResponseEntity con GenericResponseDTO contenente headers e rows della tabella
     *
     * Esempio di risposta:
     * {
     *   "status": { "isSuccess": true },
     *   "data": {
     *     "headers": ["Codice", "Obiettivo di valore pubblico", "Denominazione sintetica", ...],
     *     "rows": [
     *       ["VP1_ST1_PERF_ORG1", "Migliorare la salute organizzativa", "Obiettivo di organizzazione", ...],
     *       ["VP1_ST2_PERF_ORG2", "Ridurre i tempi di erogazione servizi", "Efficientamento processi", ...],
     *       ...
     *     ]
     *   }
     * }
     */
    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<Map<String, Object>>>> getObiettiviPerformanceTable() {
        return obiettiviPerformanceTableService.getObiettiviPerformanceTable()
            .map(ResponseEntity::ok);
    }
}
