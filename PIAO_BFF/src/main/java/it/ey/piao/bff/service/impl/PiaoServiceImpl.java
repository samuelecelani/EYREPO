package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import java.util.List;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.global.exception.CustomBusinessException;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IPiaoService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.core.ParameterizedTypeReference;

@Service
public class PiaoServiceImpl implements IPiaoService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(PiaoServiceImpl.class);

    public PiaoServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }
    @Override
    public Mono<GenericResponseDTO<PiaoDTO>> initializePiao(PiaoDTO piaoDTO) {
        log.info("Richiesta lista di tutte le funzuonalità sulla base del ruolo passato");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/piao/getOrCreate", webServiceType,piaoDTO,headers,PiaoDTO.class)
            .doOnNext(response -> log.info("Piao inizializzato: {}", response))
            .map(piao -> {
                GenericResponseDTO<PiaoDTO> finalResponse = new GenericResponseDTO<>();
                if (piao == null){
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Funzionalità Redigi PIAO disabilitata. Creare una nuova versione del PIAO");
                }
                finalResponse.setData(piao);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore inizializzazione PIAO {}", e);
                GenericResponseDTO<PiaoDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }
    @Override
    public Mono<GenericResponseDTO<Boolean>> redigiPiaoIsAllowed(String codPAFK){

            // Validazione input
            if (!StringUtils.isNotBlank(codPAFK)) {
                log.error("CodPAFK mancante nel PiaoDTO");
                throw new CustomBusinessException("Il codice della pubblica amministrazione è obbligatorio per creare o recuperare il PIAO");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            return webClientService.get("/piao/redigiPiaoIsAllowed?codPAFK=" + codPAFK  , webServiceType,headers,Boolean.class)
                .doOnNext(response -> log.info("isRedigiPiaoAllowed: {}", response))
                .map(res ->{
                    GenericResponseDTO<Boolean> respose = new GenericResponseDTO<>();
                    respose.setData(res);
                    respose.setStatus(Status.builder().isSuccess(true).build());
                    return respose;
                });

    }

    @Override
    public Mono<GenericResponseDTO<List<PiaoDTO>>> findPiaoByCodPAFK(String codPAFK) {
        // Validazione input
        if (!StringUtils.isNotBlank(codPAFK)) {
            log.error("CodPAFK mancante nel PiaoDTO");
            throw new CustomBusinessException("Il codice della pubblica amministrazione è obbligatorio per creare o recuperare il PIAO");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/piao/findBycodPAFK?codPAFK=" + codPAFK  , webServiceType,headers,new ParameterizedTypeReference<List<PiaoDTO>>(){})
            .doOnNext(response -> log.info("findPiaosByCodPAFK: {}", response))
            .map(res ->{
                GenericResponseDTO<List<PiaoDTO>> respose = new GenericResponseDTO<>();
                respose.setData(res);
                respose.setStatus(Status.builder().isSuccess(true).build());
                return respose;
            });
    }


}
