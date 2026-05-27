package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione1DTO;
import it.ey.dto.Sezione21DTO;
import it.ey.dto.Sezione22DTO;
import it.ey.piao.api.service.ISezione22Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@ApiV1Controller("/sezione22")
public class Sezione22Controller {

    private final ISezione22Service sezione22Service;

    public Sezione22Controller(ISezione22Service sezione22Service) {
        this.sezione22Service = sezione22Service;
    }

    @PostMapping("/piao")
    public ResponseEntity<Sezione22DTO> getOrCreate(@RequestBody PiaoDTO piaoDTO) {
        return ResponseEntity.ok(sezione22Service.getOrCreateSezione(piaoDTO));
    }

    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody Sezione22DTO request) {
        sezione22Service.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/validazione/{id}")
    public ResponseEntity<Sezione22DTO> richiediValidazione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione22Service.richiediValidazione(id, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }
    @GetMapping("/{idPiao}")
    public ResponseEntity<Sezione22DTO> getByIdPiao(@PathVariable Long idPiao) {
        return ResponseEntity.ok(sezione22Service.findByIdPiao(idPiao));
    }

    @PatchMapping("/valida-sezione/{id}")
    public ResponseEntity<Sezione22DTO> validaSezione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione22Service.validaSezione(id, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    // Rifiuta la validazione: da IN_VALIDAZIONE passiamo in  COMPILATA + storico.rifiutato=true
    @PatchMapping("/rifiuta-validazione/{id}")
    public ResponseEntity<Sezione22DTO> rifiutaValidazione(@PathVariable Long id, @RequestBody String osservazioni,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione22Service.rifiutaValidazione(id, osservazioni, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    // Revoca la validazione: da VALIDATA passiamo in COMPILATA + storico.revocato=true
    @PatchMapping("/revoca-validazione/{id}")
    public ResponseEntity<Sezione22DTO> revocaValidazione(@PathVariable Long id, @RequestBody String osservazioni,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione22Service.revocaValidazione(id, osservazioni, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    // annulla la validazione: da IN_VALIDAZIONE passiamo in  COMPILATA + storico.annullato=true
    @PatchMapping("/annulla-validazione/{id}")
    public ResponseEntity<Sezione22DTO> annullaValidazione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione22Service.annullaValidazione(id, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

}

