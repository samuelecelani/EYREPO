package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.external.AmministrazioneExternalPPDTO;
import it.ey.dto.external.DocumentoPiaoExternalPPDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IExternalService {

    Mono<GenericResponseDTO<List<DocumentoPiaoExternalPPDTO>>> getPiaoAndAllegati(Long idPiao, String denominazione, String codePa);

    Mono<GenericResponseDTO<List<AmministrazioneExternalPPDTO>>> getAmministrazioniServiziComuni(Boolean isBIP);
}

