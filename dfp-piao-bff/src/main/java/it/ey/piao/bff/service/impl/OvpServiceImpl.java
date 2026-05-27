package it.ey.piao.bff.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.TypeErrorEnum;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IOVPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OvpServiceImpl implements IOVPService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(OvpServiceImpl.class);

    public OvpServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }




    @Override
    public Mono<GenericResponseDTO<OVPDTO>> saveOrUpdate(OVPDTO request) {
        log.info("Salvataggio di un OVP");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/ovp/save", webServiceType, request, headers, OVPDTO.class)
            .doOnNext(response -> log.info("Sezione1 Salvata/Modficata: {}", response))
            .map(ovp -> {
                GenericResponseDTO<OVPDTO> finalResponse = new GenericResponseDTO<>();
                if (ovp == null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(ovp);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore Salvataggio/modifica OVP {}", e);
                GenericResponseDTO<OVPDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
                return Mono.just(finalResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<OVPDTO>>> getOvpByIdSezione21(Long idSezione21) {
        log.info("Recupero di tutti gli ovp");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/ovp/sezione/" + idSezione21, webServiceType, headers, new ParameterizedTypeReference<List<OVPDTO>>() {})
            .doOnNext(response -> log.info(" Numero ovp ricevuti: {}", response.size()))
            .map(ovp -> {
                GenericResponseDTO<List<OVPDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(ovp);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero dei dati {}", e);
                GenericResponseDTO<List<OVPDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
                return Mono.just(finalResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<OVPDTO>>> getOvpByPiaoId(Long piaoId) {
        log.info("Recupero di tutti gli OVP per PIAO id={}", piaoId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/ovp/piao/" + piaoId, webServiceType, headers, new ParameterizedTypeReference<List<OVPDTO>>() {})
            .doOnNext(response -> log.info("Numero OVP ricevuti per PIAO {}: {}", piaoId, response.size()))
            .map(ovp -> {
                GenericResponseDTO<List<OVPDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(ovp);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero degli OVP per PIAO {}: {}", piaoId, e.getMessage(), e);
                GenericResponseDTO<List<OVPDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
                return Mono.just(finalResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, boolean forceDelete) {

        log.info("Richiesta eliminazione OVP con id={}, forceDelete={}", id, forceDelete);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        StringBuilder url = new StringBuilder("/ovp/" + id + "?");

        if (campiModificati != null && !campiModificati.isBlank()) {
            url.append("campiModificati=")
                .append(URLEncoder.encode(campiModificati, StandardCharsets.UTF_8))
                .append("&");
        }

        if (idPiao != null) {
            url.append("idPiao=").append(idPiao).append("&");
        }

        if (testoSezione != null && !testoSezione.isBlank()) {
            url.append("testoSezione=")
                .append(URLEncoder.encode(testoSezione, StandardCharsets.UTF_8))
                .append("&");
        }

        url.append("forceDelete=").append(forceDelete);

        return webClientService
            .deleteWithExchange(
                url.toString(),
                webServiceType,
                headers,
                new ParameterizedTypeReference<GenericResponseDTO<Void>>() {}
            )
            .defaultIfEmpty(new GenericResponseDTO<>())

            .map(apiResponse -> {

                // WARNING o ERROR APPLICATIVO (200 con body.error != null)
                if (apiResponse != null && apiResponse.getError() != null) {

                    MetadatoDTO<Object> metadato = MetadatoDTO.<Object>builder()
                        .value(OVPDTO.class.getSimpleName())
                        .idFK(id)
                        .idPiao(idPiao)
                        .build();

                    apiResponse.setMetadato(List.of(metadato));

                    return apiResponse;
                }

                // ⭐ CASO SUCCESS 204 → ritorna success=true
                GenericResponseDTO<Void> successResponse = new GenericResponseDTO<>();
                Status status = new Status(true);
                successResponse.setStatus(status);

                return successResponse;
            })

            .onErrorResume(e -> {

                log.error("Errore eliminazione OVP id={} -> {}", id, e.getMessage(), e);

                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                Status status = new Status(false);
                finalResponse.setStatus(status);

                Error err = Error.builder()
                    .messageError(e.getMessage())
                    .errorCode("ERRORE_INTERNO_CONFLITTI_CANCELLAZIONE")
                    .build();

                if (e instanceof WebClientResponseException webEx) {
                    try {
                        GenericResponseDTO<Void> errorResponse =
                            webEx.getResponseBodyAs(new ParameterizedTypeReference<GenericResponseDTO<Void>>() {});
                        if (errorResponse != null && errorResponse.getError() != null) {
                            err = errorResponse.getError();
                        }
                    } catch (Exception exParse) {
                        log.warn("Errore parsing body risposta OVP", exParse);
                        err.setMessageError(webEx.getResponseBodyAsString());
                    }
                }

                finalResponse.setError(err);

                MetadatoDTO<Object> metadato = MetadatoDTO.<Object>builder()
                    .value(OVPDTO.class.getSimpleName())
                    .idFK(id)
                    .idPiao(idPiao)
                    .build();

                finalResponse.setMetadato(List.of(metadato));

                return Mono.just(finalResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<ItemMatriceDTO>>> getOvpMatriceByIdSezione21(Long idSezione21, Long idSezione1, Long idPiao) {
        log.info("Recupero matrice OVP completa per sezione21={}, sezione1={}, piaoId={}", idSezione21, idSezione1, idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        // Costruisco l'URL con query parameters
        StringBuilder url = new StringBuilder("/ovp/matrice-data?idSezione1=" + idSezione1);
        if (idSezione21 != null) {
            url.append("&idSezione=").append(idSezione21);
        }
        if (idPiao != null) {
            url.append("&idPiao=").append(idPiao);
        }

        return webClientService.get(url.toString(), webServiceType, headers,
                new ParameterizedTypeReference<OVPMatriceDataDTO>() {})
            .doOnNext(response -> log.info("Ricevuti {} OVP, {} Priorità Politiche, {} Aree Organizzative",
                response.getOvpList() != null ? response.getOvpList().size() : 0,
                response.getAllPrioritaPolitiche() != null ? response.getAllPrioritaPolitiche().size() : 0,
                response.getAllAreeOrganizzative() != null ? response.getAllAreeOrganizzative().size() : 0))
            .map(matriceData -> {
                GenericResponseDTO<List<ItemMatriceDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(buildMatrice(matriceData));
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero della matrice OVP: {}", e.getMessage(), e);
                GenericResponseDTO<List<ItemMatriceDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
                return Mono.just(finalResponse);
            });
    }

    private List<ItemMatriceDTO> buildMatrice(OVPMatriceDataDTO matriceData) {
        long startTime = System.currentTimeMillis();

        if (matriceData == null) {
            return Collections.emptyList();
        }

        List<OVPDTO> ovpList = matriceData.getOvpList();
        List<PrioritaPoliticaDTO> allPrioritaPolitiche = matriceData.getAllPrioritaPolitiche();
        List<AreaOrganizzativaDTO> allAreeOrganizzative = matriceData.getAllAreeOrganizzative();

        if (ovpList == null) ovpList = Collections.emptyList();
        if (allPrioritaPolitiche == null) allPrioritaPolitiche = Collections.emptyList();
        if (allAreeOrganizzative == null) allAreeOrganizzative = Collections.emptyList();

        // Pre-calcola le liste ordinate (una sola volta)
        List<String> orderedPriorities = allPrioritaPolitiche.stream()
            .filter(Objects::nonNull)
            .map(PrioritaPoliticaDTO::getNomePrioritaPolitica)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (orderedPriorities.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> orderedAreas = allAreeOrganizzative.stream()
            .filter(Objects::nonNull)
            .map(AreaOrganizzativaDTO::getNomeArea)
            .filter(Objects::nonNull)
            .toList();

        // Crea mappa di lookup area -> descrizione (evita ricerche ripetute)
        Map<String, String> areaDescriptions = allAreeOrganizzative.stream()
            .filter(Objects::nonNull)
            .filter(ao -> ao.getNomeArea() != null)
            .collect(Collectors.toMap(
                AreaOrganizzativaDTO::getNomeArea,
                ao -> ao.getDescrizioneArea() != null ? ao.getDescrizioneArea() : "",
                (v1, v2) -> v1,
                LinkedHashMap::new
            ));

        // Pre-elabora gli OVP: crea struttura priorità -> area -> lista OVP
        // Usa HashMap invece di LinkedHashMap per velocità (l'ordine è già garantito da orderedPriorities)
        Map<String, Map<String, List<OVPDTO>>> matriceMap = new HashMap<>(orderedPriorities.size());

        for (OVPDTO ovp : ovpList) {
            if (ovp == null || ovp.getPrioritaPolitiche() == null || ovp.getAreeOrganizzative() == null) {
                continue;
            }

            // Estrai nomi (evita stream per piccole liste)
            List<String> ovpPriorities = new ArrayList<>();
            for (OVPPrioritaPoliticaDTO pp : ovp.getPrioritaPolitiche()) {
                if (pp != null && pp.getPrioritaPolitica() != null && pp.getPrioritaPolitica().getNomePrioritaPolitica() != null) {
                    ovpPriorities.add(pp.getPrioritaPolitica().getNomePrioritaPolitica());
                }
            }

            List<String> ovpAreas = new ArrayList<>();
            for (OVPAreaOrganizzativaDTO ao : ovp.getAreeOrganizzative()) {
                if (ao != null && ao.getAreaOrganizzativa() != null && ao.getAreaOrganizzativa().getNomeArea() != null) {
                    ovpAreas.add(ao.getAreaOrganizzativa().getNomeArea());
                }
            }

            if (ovpPriorities.isEmpty() || ovpAreas.isEmpty()) {
                continue;
            }

            // Popola la mappa
            for (String priority : ovpPriorities) {
                Map<String, List<OVPDTO>> areasMap = matriceMap.computeIfAbsent(priority, k -> new HashMap<>());
                for (String area : ovpAreas) {
                    areasMap.computeIfAbsent(area, k -> new ArrayList<>()).add(ovp);
                }
            }
        }

        // Costruisci la lista finale usando l'ordine pre-calcolato
        List<ItemMatriceDTO> result = new ArrayList<>(orderedPriorities.size());

        for (String priority : orderedPriorities) {
            ItemMatriceDTO item = new ItemMatriceDTO();
            item.setPoliticalPriority(priority);

            Map<String, List<OVPDTO>> areasForPriority = matriceMap.get(priority);
            Map<String, List<OvpItemDTO>> organisationalAreas = new LinkedHashMap<>(orderedAreas.size());

            for (String area : orderedAreas) {
                List<OVPDTO> ovpsInArea = areasForPriority != null ? areasForPriority.get(area) : null;

                if (ovpsInArea == null || ovpsInArea.isEmpty()) {
                    organisationalAreas.put(area, Collections.emptyList());
                } else {
                    List<OvpItemDTO> ovpItems = new ArrayList<>(ovpsInArea.size());
                    String areaDesc = areaDescriptions.get(area);

                    for (OVPDTO ovp : ovpsInArea) {
                        ovpItems.add(new OvpItemDTO(ovp.getCodice(), areaDesc, ovp.getDenominazione()));
                    }

                    organisationalAreas.put(area, ovpItems);
                }
            }

            item.setOrganisationalAreas(organisationalAreas);
            result.add(item);
        }

        long endTime = System.currentTimeMillis();
        log.info("Matrice costruita in {}ms - Righe: {}, Colonne: {}, OVP: {}",
                 (endTime - startTime), orderedPriorities.size(), orderedAreas.size(), ovpList.size());

        return result;
    }
}
