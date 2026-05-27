package it.ey.piao.bff.service.impl;

import it.ey.dto.Error;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.dto.external.AllegatoTicketExternalDTO;
import it.ey.dto.external.CategoriaTicketExternalDTO;
import it.ey.dto.external.TicketExternalDTO;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.ITicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

/**
 * Service di puro proxy verso il modulo TICKET_BE.
 * Nessuna logica di business: ogni metodo fa redirect della chiamata.
 */
@Service
public class TicketServiceImpl implements ITicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final WebClientService webClientService;
    private final WebClient webClient;
    private final WebServiceType ticketBeType;

    @Autowired
    public TicketServiceImpl(WebClientService webClientService, WebClient webClient) {
        this.webClientService = webClientService;
        this.webClient = webClient;
        this.ticketBeType = WebServiceType.TICKET_BE;
    }

    // ======================== TICKET ========================

    @Override
    public Mono<GenericResponseDTO<TicketExternalDTO>> apriTicket(TicketExternalDTO ticketDTO) {
        log.info("[PROXY] apriTicket -> TICKET_BE");
        return webClientService
            .post("/api/v1/ticket", ticketBeType, ticketDTO, jsonHeaders(),
                new ParameterizedTypeReference<GenericResponseDTO<TicketExternalDTO>>() {})
            .onErrorResume(e -> handleError("apriTicket", e, this::emptyTicketResponse));
    }

    @Override
    public Mono<GenericResponseDTO<TicketExternalDTO>> aggiornaTicket(Long ticketId, TicketExternalDTO ticketDTO) {
        log.info("[PROXY] aggiornaTicket id={} -> TICKET_BE", ticketId);
        return webClient.put()
            .uri(URI.create(ticketBeType.getUrl() + "/api/v1/ticket/" + ticketId))
            .headers(h -> h.addAll(jsonHeaders()))
            .bodyValue(ticketDTO)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<GenericResponseDTO<TicketExternalDTO>>() {})
            .onErrorResume(e -> handleError("aggiornaTicket", e, this::emptyTicketResponse));
    }

    @Override
    public Mono<GenericResponseDTO<TicketExternalDTO>> chiudiTicketDaHelpdesk(TicketExternalDTO ticketDTO) {
        log.info("[PROXY] chiudiTicketDaHelpdesk -> TICKET_BE (idTicketEsterno={})", ticketDTO.getIdTicketEsterno());
        return webClient.put()
            .uri(URI.create(ticketBeType.getUrl() + "/api/v1/ticket/helpdesk/close"))
            .headers(h -> h.addAll(jsonHeaders()))
            .bodyValue(ticketDTO)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<GenericResponseDTO<TicketExternalDTO>>() {})
            .onErrorResume(e -> handleError("chiudiTicketDaHelpdesk", e, this::emptyTicketResponse));
    }

    // ======================== ALLEGATI ========================

    @Override
    public Mono<GenericResponseDTO<AllegatoTicketExternalDTO>> aggiungiAllegato(AllegatoTicketExternalDTO allegatoTicketDTO,
                                                                                FilePart filePart) {
        log.info("[PROXY] aggiungiAllegato ticketId={} -> TICKET_BE", allegatoTicketDTO.getIdTicketFk());

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("allegato", allegatoTicketDTO).contentType(MediaType.APPLICATION_JSON);
        builder.asyncPart("file", filePart.content(), org.springframework.core.io.buffer.DataBuffer.class)
            .headers(h -> {
                h.setContentDispositionFormData("file", filePart.filename());
                MediaType ct = filePart.headers().getContentType();
                h.setContentType(ct != null ? ct : MediaType.APPLICATION_OCTET_STREAM);
            });

        return webClient.post()
            .uri(URI.create(ticketBeType.getUrl() + "/api/v1/ticket/allegato"))
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(builder.build()))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<GenericResponseDTO<AllegatoTicketExternalDTO>>() {})
            .onErrorResume(e -> handleError("aggiungiAllegato", e, this::emptyAllegatoResponse));
    }

    @Override
    public Mono<GenericResponseDTO<List<AllegatoTicketExternalDTO>>> getAllegatiByTicket(Long idTicket) {
        log.info("[PROXY] getAllegatiByTicket id={} -> TICKET_BE", idTicket);
        return webClientService
            .get("/api/v1/ticket/" + idTicket + "/allegati", ticketBeType, jsonHeaders(),
                new ParameterizedTypeReference<GenericResponseDTO<List<AllegatoTicketExternalDTO>>>() {})
            .onErrorResume(e -> handleError("getAllegatiByTicket", e, this::emptyAllegatoListResponse));
    }

    @Override
    public Mono<GenericResponseDTO<AllegatoTicketExternalDTO>> aggiornaEsitoAntivirus(Long allegatoId,
                                                                                     AllegatoTicketExternalDTO allegatoTicketDTO) {
        log.info("[PROXY] aggiornaEsitoAntivirus allegatoId={} -> TICKET_BE", allegatoId);
        return webClient.put()
            .uri(URI.create(ticketBeType.getUrl() + "/api/v1/ticket/allegato/" + allegatoId + "/antivirus"))
            .headers(h -> h.addAll(jsonHeaders()))
            .bodyValue(allegatoTicketDTO)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<GenericResponseDTO<AllegatoTicketExternalDTO>>() {})
            .onErrorResume(e -> handleError("aggiornaEsitoAntivirus", e, this::emptyAllegatoResponse));
    }

    @Override
    public Mono<GenericResponseDTO<Void>> eliminaAllegato(Long allegatoId) {
        log.info("[PROXY] eliminaAllegato allegatoId={} -> TICKET_BE", allegatoId);
        return webClient.delete()
            .uri(URI.create(ticketBeType.getUrl() + "/api/v1/ticket/allegato/" + allegatoId))
            .headers(h -> h.addAll(jsonHeaders()))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<GenericResponseDTO<Void>>() {})
            .onErrorResume(e -> handleError("eliminaAllegato", e, this::emptyVoidResponse));
    }

    // ======================== CATEGORIE ========================

    @Override
    public Mono<GenericResponseDTO<List<CategoriaTicketExternalDTO>>> getAllCategorie() {
        log.info("[PROXY] getAllCategorie -> TICKET_BE");
        return webClientService
            .get("/api/v1/ticket/categorie", ticketBeType, jsonHeaders(),
                new ParameterizedTypeReference<GenericResponseDTO<List<CategoriaTicketExternalDTO>>>() {})
            .onErrorResume(e -> handleError("getAllCategorie", e, this::emptyCategorieListResponse));
    }

    @Override
    public Mono<GenericResponseDTO<List<CategoriaTicketExternalDTO>>> getCategorieByModulo(String idModulo) {
        log.info("[PROXY] getCategorieByModulo idModulo={} -> TICKET_BE", idModulo);
        return webClientService
            .get("/api/v1/ticket/categorie/" + idModulo, ticketBeType, jsonHeaders(),
                new ParameterizedTypeReference<GenericResponseDTO<List<CategoriaTicketExternalDTO>>>() {})
            .onErrorResume(e -> handleError("getCategorieByModulo", e, this::emptyCategorieListResponse));
    }

    // ======================== HELPERS ========================

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setAccept(List.of(MediaType.APPLICATION_JSON));
        return h;
    }

    private <T> Mono<GenericResponseDTO<T>> handleError(String op, Throwable e, Supplier<GenericResponseDTO<T>> empty) {
        log.error("Errore [PROXY] {}: {}", op, e.getMessage(), e);
        GenericResponseDTO<T> r = empty.get();
        r.setStatus(new Status());
        r.getStatus().setSuccess(Boolean.FALSE);
        r.setError(new Error());
        r.getError().setMessageError(e.getMessage());
        return Mono.just(r);
    }

    private GenericResponseDTO<TicketExternalDTO> emptyTicketResponse() { return new GenericResponseDTO<>(); }
    private GenericResponseDTO<AllegatoTicketExternalDTO> emptyAllegatoResponse() { return new GenericResponseDTO<>(); }
    private GenericResponseDTO<List<AllegatoTicketExternalDTO>> emptyAllegatoListResponse() { return new GenericResponseDTO<>(); }
    private GenericResponseDTO<List<CategoriaTicketExternalDTO>> emptyCategorieListResponse() { return new GenericResponseDTO<>(); }
    private GenericResponseDTO<Void> emptyVoidResponse() { return new GenericResponseDTO<>(); }
}

