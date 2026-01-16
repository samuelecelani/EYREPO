package it.ey.piao.api.controller.rest;

import it.ey.dto.FunzionalitaDTO;
import it.ey.piao.api.service.IFunzionalitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/funzionalita")
public class FunzionalitaController {

    private final IFunzionalitaService funzionalitaService;

    @Autowired
    public FunzionalitaController(IFunzionalitaService funzionalitaService) {
        this.funzionalitaService = funzionalitaService;
    }
//N.B al momento si è optato per la POST considerando che ci potrebbero essere svariati ruoli  valutare se metterli in querystring e passare ad una GET che è più appropriata
    @PostMapping("/by-ruolo")
    public ResponseEntity<List<FunzionalitaDTO>> getFunzionalitaByRuolo(@RequestBody List<String> ruoli) {
        List<FunzionalitaDTO> funzionalita = funzionalitaService.getFunzionalitaByRuolo(ruoli);
        return ResponseEntity.ok(funzionalita);
    }
}
