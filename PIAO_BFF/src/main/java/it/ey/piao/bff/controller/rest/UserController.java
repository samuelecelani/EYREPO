package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
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
        public Mono<ResponseEntity<GenericResponseDTO<List<UtenteRuoloPaDTO>>>> getUserByCodicePa(@PathVariable String codicePa) {
            return userService.findUtentiByPa(codicePa).map(ResponseEntity::ok);
        }

        @PostMapping("/utentepa")
        public Mono<ResponseEntity<GenericResponseDTO<UtenteRuoloPaDTO>>> save(@RequestBody UtenteRuoloPaDTO utenteRuoloPa) {
            return userService.saveUtenteByPa(utenteRuoloPa)
                .map(ResponseEntity::ok);
        }
        @DeleteMapping("/utentepa/{id}")
        public Mono<ResponseEntity<GenericResponseDTO<Void>>> delete(@PathVariable Long id) {
            return userService.deleteUtentePa(id)
                .map(ResponseEntity::ok);
        }

}
