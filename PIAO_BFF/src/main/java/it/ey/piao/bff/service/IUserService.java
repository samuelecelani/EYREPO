package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.UserDTO;
import reactor.core.publisher.Mono;

public interface IUserService {
    public Mono<GenericResponseDTO<UserDTO>> getUserbyToken();
}
