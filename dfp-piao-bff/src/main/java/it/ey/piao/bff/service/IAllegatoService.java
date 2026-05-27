package it.ey.piao.bff.service;

import it.ey.dto.AllegatoDTO;
import it.ey.dto.GenericResponseDTO;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAllegatoService {
    Mono<GenericResponseDTO<AllegatoDTO>> insertAllegato(AllegatoDTO allegato, FilePart filePart);
    Mono<GenericResponseDTO<Void>> deleteAllegato(Long allegatoId, String fileKey, boolean isDoc, String campiModificati, Long idPiao, String codTipologiaFK, String testoSezione);
    Mono<GenericResponseDTO<List<AllegatoDTO>>> getAllegati(List<String> codTipologia, List<String> codTipologiaAllegato, Long idPiao, boolean isDoc);
    Mono<GenericResponseDTO<List<AllegatoDTO>>> getAllegatiByIdPiao(Long idPiao);

    /**
     * Salva un allegato direttamente su DB tramite il BE, senza upload S3.
     */
    Mono<GenericResponseDTO<AllegatoDTO>> saveAllegatoSenzaUpload(AllegatoDTO allegato);

    /**
     * Recupera un allegato per ID dal BE.
     */
    Mono<AllegatoDTO> findAllegatoById(Long id);

    /**
     * Aggiorna un allegato esistente tramite il BE (solo campi non-null).
     */
    Mono<GenericResponseDTO<AllegatoDTO>> updateAllegato(AllegatoDTO allegato);

   //Soft-delete della bozza PDF identificata dal codDocumento
    Mono<GenericResponseDTO<Void>> deleteBozzaByCodDocumento(String codDocumento);
}
