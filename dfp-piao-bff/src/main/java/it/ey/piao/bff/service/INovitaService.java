package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.NovitaDTO;
import it.ey.dto.NovitaPaginatedDTO;
import it.ey.dto.NovitaTipologiaDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface INovitaService {

    /**
     * GET /opencms/handle/news-tipologie — Recupera le tipologie di news.
     */
    Mono<GenericResponseDTO<List<NovitaTipologiaDTO>>> getNewsTipologie(String locale);

    /**
     * POST /opencms/handle/news-search — Ricerca news con filtri e paginazione.
     */
    Mono<GenericResponseDTO<NovitaPaginatedDTO>> searchNews(String keyword, String tipologia,
                                                            Boolean inEvidenza, String startDate,
                                                            String sort, Integer page, Integer limit,
                                                            String locale);

    /**
     * GET /opencms/handle/news-detail — Recupera il dettaglio di una news tramite UUID.
     */
    Mono<GenericResponseDTO<NovitaDTO>> getNewsDetail(String id);
}
