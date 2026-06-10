package it.ey.piao.bff.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.enums.MinervaEndpoint;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IDynamicTableIteroperabilitaService;
import it.ey.utils.DynamicTableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class DynamicTableIteroperabilitaServiceImpl implements IDynamicTableIteroperabilitaService
{
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(DynamicTableIteroperabilitaServiceImpl.class);

    public DynamicTableIteroperabilitaServiceImpl(WebClientService webClientService)
    {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.MINERVA;
    }

    @Override
    public Mono<GenericResponseDTO<Map<String, Object>>> buildDynamicTableIteroperabilita(String tableName)
    {
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
    public Mono<GenericResponseDTO<Map<String, Object>>> buildDynamicTableIteroperabilitaMock()
    {
        // Simuliamo una risposta JSON come array di oggetti
        String mockJson =
            "[" +
                "{\"Unità organizzative\":\"U.O.\",\"n. risorse umane\":3}," +
                "{\"Unità organizzative\":\"Totale unità\",\"n. risorse umane\":3}," +
                "{\"Unità organizzative\":\"Ampiezza media U.O.\",\"n. risorse umane\":0}" +
                "]"
            ;

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
