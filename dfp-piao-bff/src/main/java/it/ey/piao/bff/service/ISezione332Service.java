package it.ey.piao.bff.service;

import it.ey.dto.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ISezione332Service
{
    Mono<GenericResponseDTO<Sezione332DTO>> getOrCreate(PiaoDTO request);

    Mono<GenericResponseDTO<Sezione332DTO>> saveOrUpdate(Sezione332DTO request);

    Mono<GenericResponseDTO<Void>> richiediValidazione(Long id, String codicePa);

    Mono<GenericResponseDTO<Sezione332DTO>> findByPiao(Long idPiao);

    Mono<GenericResponseDTO<Void>> validaSezione(Long id, String codicePa);

    Mono<GenericResponseDTO<Void>> rifiutaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> revocaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> annullaValidazione(Long id, String codicePa);

    Mono<GenericResponseDTO<List<TipologiaAttivitaDTO>>> getTipologiaAttivita();

    Mono<GenericResponseDTO<List<AmbitoCompetenzaDTO>>> getAmbitoCompetenza();

    Mono<GenericResponseDTO<List<AreaTematicaDTO>>> getAreaTematica();

    Mono<GenericResponseDTO<List<TipologiaDestinatariDTO>>> getTipologiaDestinatari();
}
