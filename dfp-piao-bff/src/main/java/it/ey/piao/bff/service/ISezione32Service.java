package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione32DTO;
import reactor.core.publisher.Mono;

public interface ISezione32Service
{
    Mono<GenericResponseDTO<Void>> saveOrUpdate(Sezione32DTO request);

    Mono<GenericResponseDTO<Void>> richiediValidazione(Long id, String codicePa);

    Mono<GenericResponseDTO<Sezione32DTO>> getOrCreate(PiaoDTO request);

    Mono<GenericResponseDTO<Sezione32DTO>> findByPiao(Long idPiao);

    Mono<GenericResponseDTO<Void>> validaSezione(Long id, String codicePa);

    Mono<GenericResponseDTO<Void>> rifiutaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> revocaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> annullaValidazione(Long id, String codicePa);
}
