package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione22DTO;
import it.ey.dto.Sezione23DTO;
import reactor.core.publisher.Mono;

public interface ISezione23Service {
    Mono<GenericResponseDTO<Void>> saveOrUpdate(Sezione23DTO request);
    Mono<GenericResponseDTO<Void>> richiediValidazione(Long id, String codicePa);
    Mono<GenericResponseDTO<Sezione23DTO>> getOrCreate(PiaoDTO request);
    Mono<GenericResponseDTO<Sezione23DTO>> findByIdPiao(Long idPiao);


    Mono<GenericResponseDTO<Void>> validaSezione(Long id, String codicePa);

    Mono<GenericResponseDTO<Void>> rifiutaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> revocaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> annullaValidazione(Long id, String codicePa);

}
