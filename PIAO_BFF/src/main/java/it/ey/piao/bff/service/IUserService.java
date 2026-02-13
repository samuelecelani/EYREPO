package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.UserDTO;
import it.ey.dto.UtenteRuoloPaDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IUserService {
    public Mono<GenericResponseDTO<UserDTO>> getUserbyToken();
    public Mono<GenericResponseDTO<List<UtenteRuoloPaDTO>>> findUtentiByPa(String codicePa);
    public Mono<GenericResponseDTO<UtenteRuoloPaDTO>> saveUtenteByPa(UtenteRuoloPaDTO utenteRuoloPaDTO);
    public Mono<GenericResponseDTO<Void>> deleteUtentePa(Long id);

}
