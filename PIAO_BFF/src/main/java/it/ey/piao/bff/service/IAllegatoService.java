package it.ey.piao.bff.service;

import it.ey.dto.AllegatoDTO;
import it.ey.dto.GenericResponseDTO;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAllegatoService {
    Mono<GenericResponseDTO<AllegatoDTO>> insertAllegato(AllegatoDTO allegato, FilePart filePart);
    Mono<GenericResponseDTO<Void>> deleteAllegato(Long allegatoId, String fileKey, boolean isDoc);
    Mono<GenericResponseDTO<List<AllegatoDTO>>> getAllegati(String codTipologia, String codTipologiaAllegato, Long idPiao, boolean isDoc);
}
