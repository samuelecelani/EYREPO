package it.ey.piao.bff.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.enums.MinervaEndpoint;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IDynamicTableService;
import it.ey.utils.DynamicTableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.*;

@Service
public class DynamicTableService implements IDynamicTableService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(DynamicTableService.class);

    public DynamicTableService(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.MINERVA;
    }

    @Override
    public Mono<GenericResponseDTO<Map<String, Object>>> buildDynamicTable(String tableName) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return webClientService.get( MinervaEndpoint.valueOf(tableName).getEndpoint(), webServiceType, headers, JsonNode.class)
            .map(jsonNode -> {
                Map<String, Object> table = DynamicTableUtils.buildTableFromJson(jsonNode);
                GenericResponseDTO<Map<String, Object>> response = new GenericResponseDTO<>();
                response.setStatus(Status.builder().isSuccess(true).build());
                response.setData(table);
                return response;

            });
    }

    @Override
    public Mono<GenericResponseDTO<Map<String, Object>>> buildDynamicTableMock() {
        // Simuliamo una risposta JSON come array di oggetti
        String mockJson =
            "[" +
                "{\"Qualifiche\":\"Dirigenti I fascia\",\"Posti in Dotazione Organica\":2,\"Pers. di ruolo al 31/12/2024 anno t-1\":5,\"Totale unità art. 19 comma 5-bis\":2}," +
                "{\"Qualifiche\":\"Totale di cui Dirigenti I fascia\",\"Posti in Dotazione Organica\":2,\"Pers. di ruolo al 31/12/2024 anno t-1\":2,\"Totale unità art. 19 comma 5-bis\":2}," +
                "{\"Qualifiche\":\"Totale di cui Dirigenti II fascia + Aree\",\"Posti in Dotazione Organica\":2,\"Pers. di ruolo al 31/12/2024 anno t-1\":2,\"Totale unità art. 19 comma 5-bis\":2}," +
                "{\"Qualifiche\":\"Totale complessivo\",\"Posti in Dotazione Organica\":2,\"Pers. di ruolo al 31/12/2024 anno t-1\":2,\"Totale unità art. 19 comma 5-bis\":2}" +
                "]";


        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(mockJson);
        } catch (Exception e) {
            return Mono.error(e);
        }

        Map<String, Object> table = DynamicTableUtils.buildTableFromJson(jsonNode);

        GenericResponseDTO<Map<String, Object>> response = new GenericResponseDTO<>();
        response.setStatus(Status.builder().isSuccess(true).build());
        response.setData(table);

        return Mono.just(response);
    }


}
