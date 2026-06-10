package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.LabelValueDTO;
import it.ey.dto.UserDTO;
import it.ey.dto.UtenteRuoloPaDTO;
import it.ey.piao.bff.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/tokenized")
public class UserController {
    private final IUserService userService;

    @Autowired
    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public Mono<ResponseEntity<GenericResponseDTO<UserDTO>>> getAllTests() {
        return userService.getUserbyToken()
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

        @GetMapping("/codice-pa/{codicePa}")
        public Mono<ResponseEntity<GenericResponseDTO<List<UtenteRuoloPaDTO>>>> getUserByCodicePa(
                @PathVariable String codicePa,
                @RequestParam(name = "roleNames", required = false) List<String> roleNames) {
            return userService.findUtentiByPa(codicePa, roleNames).map(ResponseEntity::ok);
        }

        @PostMapping("/utentepa")
        public Mono<ResponseEntity<GenericResponseDTO<UtenteRuoloPaDTO>>> save(@RequestBody UtenteRuoloPaDTO utenteRuoloPa) {
            return userService.saveUtenteByPa(utenteRuoloPa)
                .map(ResponseEntity::ok);
        }
        @DeleteMapping("/utentepa/{id}")
        public Mono<ResponseEntity<GenericResponseDTO<Void>>> delete(
                @PathVariable String id,
                @RequestParam String codicePa) {
            return userService.deleteUtentePa(id, codicePa)
                .map(ResponseEntity::ok);
        }

        @PutMapping("/utentepa/{id}")
        public Mono<ResponseEntity<GenericResponseDTO<UtenteRuoloPaDTO>>> update(
                @PathVariable String id,
                @RequestParam String codicePa,
                @RequestBody UtenteRuoloPaDTO utenteRuoloPa) {
            return userService.updateUtenteByPa(id, codicePa, utenteRuoloPa)
                .map(ResponseEntity::ok);
        }

        @GetMapping("/profile/{externalId}")
        public Mono<ResponseEntity<GenericResponseDTO<UtenteRuoloPaDTO>>> retrieveProfileById(
                @PathVariable String externalId,
                @RequestParam(name = "codePA") String codePa) {
            return userService.retrieveProfileById(externalId, codePa)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
        }

        @GetMapping("/ruoliByCodePA/{codicePa}")
        public Mono<ResponseEntity<GenericResponseDTO<List<LabelValueDTO>>>> getRuoliByCodePA(
            @PathVariable String codicePa) {
            return userService.getRuoliByCodePA(codicePa)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
        }


}
