package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Sezione1DTO;
import it.ey.dto.AnagraficaDTO;
import reactor.core.publisher.Mono;

public interface ISezione1Service {

    public Mono<GenericResponseDTO<Void>> saveOrUpdate(Sezione1DTO request);
    public Mono<GenericResponseDTO<Void>> richiediValidazione(Long id, String codicePa);
    public Mono<GenericResponseDTO<Sezione1DTO>> findByPiao(Long idPiao,String codiceFiscale);

    Mono<GenericResponseDTO<Void>> validaSezione(Long id, String codicePa);

    Mono<GenericResponseDTO<Void>> rifiutaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> revocaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> annullaValidazione(Long id, String codicePa);

    Mono<GenericResponseDTO<AnagraficaDTO>> getAnagraficaFromIpa(String codiceFiscale);

}
