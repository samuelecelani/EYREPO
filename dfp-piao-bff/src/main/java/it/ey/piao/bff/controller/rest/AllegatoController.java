package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AllegatoDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.enums.CodTipologiaAllegato;
import it.ey.enums.Sezione;
import it.ey.piao.bff.service.IAllegatoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/allegato")
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
        @RequestParam List<Sezione> codTipologia,
        @RequestParam List<CodTipologiaAllegato> codTipologiaAllegato,
        @RequestParam Long idPiao,
        @RequestParam(required = false, defaultValue = "true") boolean isDoc
    ) {
        log.info("[GET /allegato/by-tipologia] codTipologia={}, codTipologiaAllegato={}, isDoc={}",
            codTipologia, codTipologiaAllegato, isDoc);
        List<String> codTipologiaNames = codTipologia.stream().map(Enum::name).toList();
        List<String> codTipologiaAllegatoNames = codTipologiaAllegato.stream().map(Enum::name).toList();
        return allegatoService.getAllegati(codTipologiaNames, codTipologiaAllegatoNames, idPiao, isDoc);
    }

    @DeleteMapping("/delete")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> deleteAllegato(
        @RequestParam Long allegatoId,
        @RequestParam String fileKey,
        @RequestParam(required = false, defaultValue = "true") boolean isDoc,
        @RequestParam(required = false) String campiModificati,
        @RequestParam(required = false) Long idPiao,
        @RequestParam(required = false) String codTipologiaFK,
        @RequestParam(required = false) String testoSezione,
                                                            @RequestParam(required = false) String statoSezione) {
        return allegatoService.deleteAllegato(allegatoId, fileKey, isDoc, campiModificati, idPiao, codTipologiaFK, testoSezione)
            .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/findByIdPiao", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponseDTO<List<AllegatoDTO>>> getAllegatiByIdPiao(
        @RequestParam Long idPiao
    ) {
        return allegatoService.getAllegatiByIdPiao(idPiao);
    }


    @DeleteMapping("/delete-bozza")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> deleteBozza(@RequestParam String codDocumento) {

        log.info("[DELETE /allegato/delete-bozza] codDocumento={}", codDocumento);
        return allegatoService.deleteBozzaByCodDocumento(codDocumento)
            .map(ResponseEntity::ok);
    }

}
