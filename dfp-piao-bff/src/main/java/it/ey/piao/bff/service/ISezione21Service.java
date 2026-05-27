package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione21DTO;
import reactor.core.publisher.Mono;

public interface ISezione21Service {

    public Mono<GenericResponseDTO<Void>> saveOrUpdate(Sezione21DTO request);
    public Mono<GenericResponseDTO<Void>> richiediValidazione(Long id, String codicePa);
    public Mono<GenericResponseDTO<Sezione21DTO>> findByPiao(Long idPiao);

    Mono<GenericResponseDTO<Void>> validaSezione(Long id, String codicePa);

    Mono<GenericResponseDTO<Void>> rifiutaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> revocaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> annullaValidazione(Long id, String codicePa);

}
