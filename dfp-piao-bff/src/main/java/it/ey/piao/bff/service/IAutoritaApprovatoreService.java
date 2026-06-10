package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.AutoritaApprovatoreDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAutoritaApprovatoreService
{
    Mono<GenericResponseDTO<List<AutoritaApprovatoreDTO>>> getAll();
}
