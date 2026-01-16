package it.ey.piao.bff.controller.rest;
import it.ey.dto.AllegatoDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.enums.CodTipologia;
import it.ey.enums.CodTipologiaAllegato;
import it.ey.piao.bff.global.exception.CustomBusinessException;
import it.ey.piao.bff.service.IAllegatoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/allegato")
@RequiredArgsConstructor
public class AllegatoController {

    private static final Logger log = LoggerFactory.getLogger(AllegatoController.class);

    private final IAllegatoService allegatoService;

    @PostMapping(
        value = "/save",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<GenericResponseDTO<AllegatoDTO>> insertAllegato(
        @RequestPart("allegato") Mono<AllegatoDTO> allegatoMono,
        @RequestPart("file") Mono<FilePart> filePartMono
    ) {
        return Mono.zip(allegatoMono, filePartMono)
            .flatMap(tuple -> {
                AllegatoDTO allegato = tuple.getT1();
                FilePart filePart = tuple.getT2();

                log.info("[POST /allegato/save] Inserimento allegato: {} - file: {}",
                    allegato, filePart.filename());

                return allegatoService.insertAllegato(allegato, filePart);
            });
    }


    @GetMapping(value = "/by-tipologia", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponseDTO<List<AllegatoDTO>>> getAllegatiByTipologia(
        @RequestParam CodTipologia codTipologia,
        @RequestParam CodTipologiaAllegato codTipologiaAllegato,
        @RequestParam Long idPiao
    ) {
        log.info("[GET /allegato/by-tipologia] codTipologia={}, codTipologiaAllegato={}", codTipologia, codTipologiaAllegato);
        // Il service accetta String: passiamo i name() degli enum
        return allegatoService.getAllegati(codTipologia.name(), codTipologiaAllegato.name(),idPiao);
    }

    @DeleteMapping("/delete")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> deleteAllegato(
        @RequestParam Long allegatoId,
        @RequestParam String fileKey) {
        return allegatoService.deleteAllegato(allegatoId, fileKey)
            .map(ResponseEntity::ok);
    }

}
