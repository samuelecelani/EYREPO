package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Sezione4DTO;
import reactor.core.publisher.Mono;

public interface ISezione4Service {

    Mono<GenericResponseDTO<Sezione4DTO>> saveOrUpdate(Sezione4DTO request);

    Mono<GenericResponseDTO<Void>> richiediValidazione(Long id, String codicePa);

    Mono<GenericResponseDTO<Void>> validaSezione(Long id, String codicePa);

    Mono<GenericResponseDTO<Void>> rifiutaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> revocaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> annullaValidazione(Long id, String codicePa);

    Mono<GenericResponseDTO<Sezione4DTO>> findByIdPiao(Long idPiao);
}
