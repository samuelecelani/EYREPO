package it.ey.piao.bff.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.piao.bff.service.IObiettiviPerformanceTableService;
import it.ey.utils.DynamicTableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class ObiettiviPerformanceTableService implements IObiettiviPerformanceTableService {
    private static final Logger log = LoggerFactory.getLogger(ObiettiviPerformanceTableService.class);
    private final ObjectMapper objectMapper;

    public ObiettiviPerformanceTableService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<GenericResponseDTO<Map<String, Object>>> getObiettiviPerformanceTable() {
        log.info("Recupero tabella Obiettivi di Performance");

        // JSON mock con i dati degli obiettivi di performance
        String mockJson = """
            {
              "righe": [
                {
                  "Codice": "VP1_ST1_PERF_ORG1",
                  "Obiettivo di valore pubblico": "Migliorare la salute organizzativa",
                  "Denominazione sintetica": "Obiettivo di organizzazione",
                  "Responsabile amministrativo": "Ufficio Organizzazione e Personale",
                  "Stakeholder interni ed esterni": [
                    "Dipendenti",
                    "RSU",
                    "Cittadini",
                    "Associazioni di categoria"
                  ],
                  "Dimensione": "Salute organizzativa",
                  "Formula": "Indice salute = (punteggio survey / punteggio massimo) * 100",
                  "Polarità": "+",
                  "Baseline Anno N": 62,
                  "Target anno N+1": 68,
                  "Target anno N+2": 72,
                  "Target anno N+3": 75,
                  "Fonte": "Survey interna HR 2025"
                },
                {
                  "Codice": "VP1_ST2_PERF_ORG2",
                  "Obiettivo di valore pubblico": "Ridurre i tempi di erogazione servizi",
                  "Denominazione sintetica": "Efficientamento processi",
                  "Responsabile amministrativo": "Direzione Servizi al Cittadino",
                  "Stakeholder interni ed esterni": [
                    "Sportelli front-office",
                    "UT Servizi Digitali",
                    "Utenti/Contribuenti"
                  ],
                  "Dimensione": "Efficienza",
                  "Formula": "Tempo medio (giorni) = somma tempi / pratiche concluse",
                  "Polarità": "-",
                  "Baseline Anno N": 14.5,
                  "Target anno N+1": 12.0,
                  "Target anno N+2": 10.0,
                  "Target anno N+3": 9.0,
                  "Fonte": "Cruscotto tempi procedimentali"
                },
                {
                  "Codice": "VP2_ST1_PERF_ORG3",
                  "Obiettivo di valore pubblico": "Aumentare la soddisfazione dell'utenza",
                  "Denominazione sintetica": "Customer satisfaction",
                  "Responsabile amministrativo": "URP",
                  "Stakeholder interni ed esterni": [
                    "URP",
                    "Direzioni di linea",
                    "Utenti"
                  ],
                  "Dimensione": "Qualità percepita",
                  "Formula": "CSAT (%) = (risposte positive / risposte totali) * 100",
                  "Polarità": "+",
                  "Baseline Anno N": 78,
                  "Target anno N+1": 82,
                  "Target anno N+2": 85,
                  "Target anno N+3": 88,
                  "Fonte": "Questionari soddisfazione online"
                }
              ]
            }
            """;

        try {
            // Parse del JSON
            JsonNode jsonNode = objectMapper.readTree(mockJson);

            // Estrai l'array "righe"
            JsonNode righeNode = jsonNode.get("righe");
            if (righeNode == null || !righeNode.isArray()) {
                log.error("Formato JSON non valido: manca il campo 'righe' o non è un array");
                return Mono.error(new RuntimeException("Formato JSON non valido"));
            }

            // Usa DynamicTableUtils per costruire la tabella
            Map<String, Object> table = DynamicTableUtils.buildTableFromJson(righeNode);

            // Costruisci la response
            GenericResponseDTO<Map<String, Object>> response = new GenericResponseDTO<>();
            response.setStatus(Status.builder().isSuccess(true).build());
            response.setData(table);

            log.info("Tabella Obiettivi di Performance generata con successo: {} righe",
                righeNode.size());

            return Mono.just(response);

        } catch (Exception e) {
            log.error("Errore durante la generazione della tabella Obiettivi di Performance: {}",
                e.getMessage(), e);

            GenericResponseDTO<Map<String, Object>> errorResponse = new GenericResponseDTO<>();
            errorResponse.setStatus(Status.builder().isSuccess(false).build());
            errorResponse.setError(new it.ey.dto.Error());
            errorResponse.getError().setMessageError("Errore durante la generazione della tabella: " + e.getMessage());

            return Mono.just(errorResponse);
        }
    }
}
