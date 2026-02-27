package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.FunzionalitaDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IFunzionalitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/funzionalita")
public class FunzionalitaController {


    private final IFunzionalitaService funzionalitaService;

    @Autowired
    public FunzionalitaController(IFunzionalitaService funzionalitaService) {
        this.funzionalitaService = funzionalitaService;
    }


    @PostMapping("/by-ruolo")
    @PreAuthorize("hasRole('AMMINISTRATORE') or hasRole('AMMINISTRATORE_SEZIONE1') ")
    public Mono<ResponseEntity<GenericResponseDTO<List<FunzionalitaDTO>>>> getFunzionalita(@RequestBody List<String> ruoli) {
        return funzionalitaService.getFunzionalitàByRuolo(ruoli)
            .map(ResponseEntity::ok);
    }
}
