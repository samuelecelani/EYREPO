package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.NovitaDTO;
import it.ey.dto.NovitaPaginatedDTO;
import it.ey.dto.NovitaTipologiaDTO;
import it.ey.piao.bff.service.INovitaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/novita")
public class NovitaController {

    private final INovitaService novitaService;

    public NovitaController(INovitaService novitaService) {
        this.novitaService = novitaService;
    }

    /**
     * GET /api/v1/novita/tipologie — Recupera le tipologie di news da OpenCms.
     */
    @GetMapping("/tipologie")
    public Mono<ResponseEntity<GenericResponseDTO<List<NovitaTipologiaDTO>>>> getNewsTipologie(
        @RequestParam(required = false, defaultValue = "it") String locale) {
        return novitaService.getNewsTipologie(locale)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    /**
     * POST /api/v1/novita/search — Ricerca news con filtri e paginazione.
     */
    @PostMapping("/search")
    public Mono<ResponseEntity<GenericResponseDTO<NovitaPaginatedDTO>>> searchNews(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String tipologia,
        @RequestParam(required = false) Boolean inEvidenza,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false, defaultValue = "date_desc") String sort,
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer limit,
        @RequestParam(required = false, defaultValue = "it") String locale) {
        return novitaService.searchNews(keyword, tipologia, inEvidenza, startDate, sort, page, limit, locale)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    /**
     * GET /api/v1/novita/detail — Recupera il dettaglio di una news tramite UUID OpenCms.
     */
    @GetMapping("/detail")
    public Mono<ResponseEntity<GenericResponseDTO<NovitaDTO>>> getNewsDetail(
        @RequestParam String id) {
        return novitaService.getNewsDetail(id)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}
