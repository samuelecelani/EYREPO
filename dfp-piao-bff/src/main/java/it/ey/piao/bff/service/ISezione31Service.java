package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.GraficoSezione31DTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione31DTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ISezione31Service
{
    Mono<GenericResponseDTO<Void>> saveOrUpdate(Sezione31DTO request);
    Mono<GenericResponseDTO<Void>> richiediValidazione(Long id, String codicePa);
    Mono<GenericResponseDTO<Sezione31DTO>> getOrCreate(PiaoDTO request);
    Mono<GenericResponseDTO<Sezione31DTO>> findByPiao(Long idPiao);


    Mono<GenericResponseDTO<Void>> validaSezione(Long id, String codicePa);

    Mono<GenericResponseDTO<Void>> rifiutaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> revocaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> annullaValidazione(Long id, String codicePa);

    /**
     * Mock che emula una chiamata al servizio Minerva per i dati del grafico Sezione 4.
     */
    Mono<GenericResponseDTO<List<GraficoSezione31DTO>>> getGraficoSezione31Mock();

}
