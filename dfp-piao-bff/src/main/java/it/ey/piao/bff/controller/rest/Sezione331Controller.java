package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione21DTO;
import it.ey.dto.Sezione331DTO;
import it.ey.enums.TipoTabellaSezione331;
import it.ey.piao.bff.service.ISezione331Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@ApiV1Controller("/sezione331")
public class Sezione331Controller {

    private  final ISezione331Service sezione331Service;

    public Sezione331Controller(ISezione331Service sezione331Service) {
        this.sezione331Service = sezione331Service;
    }


    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione331DTO>>> save(@RequestBody Sezione331DTO request) {
        return sezione331Service.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @PatchMapping("/validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> richiestaDiValidazione(@PathVariable Long id,
                                                                                 @RequestParam String codicePa,
                                                                                 @RequestParam(required = false) String testoSezione,
                                                                                 @RequestParam(required = false) String campiModificati) {
        return sezione331Service.richiediValidazione(id, codicePa).map(ResponseEntity::ok);
    }

    @PostMapping("/piao")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione331DTO>>> getOrCreatePIAO(@RequestBody PiaoDTO request) {
        return sezione331Service.getOrCreate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione331DTO>>> getByIdPiao(@PathVariable Long idPiao) {
        return sezione331Service.findByPiao(idPiao).map(ResponseEntity::ok);
    }


    @PatchMapping("/valida-sezione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> validaSezione(@PathVariable Long id,
                                                                        @RequestParam String codicePa,
                                                                        @RequestParam(required = false) String testoSezione,
                                                                        @RequestParam(required = false) String campiModificati) {
        return sezione331Service.validaSezione(id, codicePa).map(ResponseEntity::ok);
    }
    @PatchMapping("/rifiuta-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> rifiutaValidazione(@PathVariable Long id,@RequestBody String osservazioni,
                                                                             @RequestParam String codicePa,
                                                                             @RequestParam(required = false) String testoSezione,
                                                                             @RequestParam(required = false) String campiModificati) {
        return sezione331Service.rifiutaValidazione(id, osservazioni, codicePa).map(ResponseEntity::ok);
    }
    @PatchMapping("/revoca-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> revocaValidazione(@PathVariable Long id,@RequestBody String osservazioni,
                                                                            @RequestParam String codicePa,
                                                                            @RequestParam(required = false) String testoSezione,
                                                                            @RequestParam(required = false) String campiModificati) {
        return sezione331Service.revocaValidazione(id, osservazioni, codicePa).map(ResponseEntity::ok);
    }

    @PatchMapping("/annulla-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> annullaValidazione(@PathVariable Long id,
                                                                             @RequestParam String codicePa,
                                                                             @RequestParam(required = false) String testoSezione,
                                                                             @RequestParam(required = false) String campiModificati) {
        return sezione331Service.annullaValidazione(id, codicePa).map(ResponseEntity::ok);
    }

    @GetMapping("/tabella/{tipoTabella}")
    public Mono<ResponseEntity<GenericResponseDTO<Map<String, Object>>>> getTabellaMock(
            @PathVariable TipoTabellaSezione331 tipoTabella) {
        return sezione331Service.getTabellaMock(tipoTabella).map(ResponseEntity::ok);
    }

    @GetMapping("/tabelle")
    public Mono<ResponseEntity<GenericResponseDTO<List<Map<String, Object>>>>> getAllTabelleMock(@RequestParam String codiceAmministrazione,
                                                                                                 @RequestParam String annoRiferimento,
                                                                                                 @RequestParam(required = false) Long idEntitaFk,
                                                                                                 @RequestParam(required = false) Boolean storageMinerva) {
        return sezione331Service.getAllTabelleMock(codiceAmministrazione, annoRiferimento, idEntitaFk, storageMinerva)
            .map(ResponseEntity::ok);
    }

    /*
    // API per il recupero del token di MINERVA
    @GetMapping("/minerva")
    public Mono<ResponseEntity<GenericResponseDTO<String>>> getMinervaMock() {
        return sezione331Service.getTokenMinerva().map(ResponseEntity::ok);
    }
    */
}
