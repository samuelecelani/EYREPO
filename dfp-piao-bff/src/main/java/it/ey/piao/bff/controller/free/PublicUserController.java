package it.ey.piao.bff.controller.free;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.UtenteRuoloPaDTO;
import it.ey.piao.bff.service.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/public/user")
public class PublicUserController {

    private final IUserService userService;

    public PublicUserController(IUserService userService) {
        this.userService = userService;
    }

    /**
     * Crea (upsert) un referente: crea/aggiorna utente su BIP con ruolo Referente,
     * poi abilita tutte le sezioni effettive del PIAO sul nostro DB.
     * L'amministrazioneId è valorizzato dal chiamante nel campo codicePA del DTO.
     */
    @PostMapping("/referente")
    public Mono<ResponseEntity<GenericResponseDTO<UtenteRuoloPaDTO>>> createReferente(
            @RequestBody UtenteRuoloPaDTO utenteRuoloPaDTO) {
        return userService.createReferente(utenteRuoloPaDTO)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    /**
     * Crea (upsert) un validatore: crea/aggiorna utente su BIP con ruolo Validatore,
     * poi abilita tutte le sezioni effettive del PIAO sul nostro DB.
     * L'amministrazioneId è valorizzato dal chiamante nel campo codicePA del DTO.
     */
    @PostMapping("/validatore")
    public Mono<ResponseEntity<GenericResponseDTO<UtenteRuoloPaDTO>>> createValidatore(
            @RequestBody UtenteRuoloPaDTO utenteRuoloPaDTO) {
        return userService.createValidatore(utenteRuoloPaDTO)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    /**
     * Crea (upsert) un redattore: crea/aggiorna utente su BIP con ruolo Redattore,
     * poi abilita tutte le sezioni effettive del PIAO sul nostro DB.
     * L'amministrazioneId è valorizzato dal chiamante nel campo codicePA del DTO.
     */
    @PostMapping("/redattore")
    public Mono<ResponseEntity<GenericResponseDTO<UtenteRuoloPaDTO>>> createRedattore(
            @RequestBody UtenteRuoloPaDTO utenteRuoloPaDTO) {
        return userService.createRedattore(utenteRuoloPaDTO)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}

