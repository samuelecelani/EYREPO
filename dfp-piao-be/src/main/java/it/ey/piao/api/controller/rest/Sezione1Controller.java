package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.Sezione1DTO;
import it.ey.piao.api.service.ISezione1Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ApiV1Controller("/sezione1")
public class Sezione1Controller {

    private final ISezione1Service sezione1Service;


    public Sezione1Controller(ISezione1Service sezione1Service) {
        this.sezione1Service = sezione1Service;
    }

    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody Sezione1DTO request) {
        sezione1Service.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/validazione/{id}")
    public ResponseEntity<Sezione1DTO> richiediValidazione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione1Service.richiediValidazione(id, userNameSurname, userRole, fiscalCode,testoSezione,campiModificati));
    }
    @GetMapping("/{idPiao}")
    public ResponseEntity<Sezione1DTO> getByIdPiao(@PathVariable Long idPiao) {
        return ResponseEntity.ok(sezione1Service.findByIdPiao(idPiao));
    }

    @PatchMapping("/valida-sezione/{id}")
    public ResponseEntity<Sezione1DTO> validaSezione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione1Service.validaSezione(id, userNameSurname, userRole,fiscalCode,testoSezione,campiModificati));
    }

     // Rifiuta la validazione: da IN_VALIDAZIONE passiamo in  COMPILATA + storico.rifiutato=true
    @PatchMapping("/rifiuta-validazione/{id}")
    public ResponseEntity<Sezione1DTO> rifiutaValidazione(@PathVariable Long id, @RequestBody String osservazioni,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione1Service.rifiutaValidazione(id, osservazioni, userNameSurname, userRole, fiscalCode,testoSezione,campiModificati));
    }

     // Revoca la validazione: da VALIDATA passiamo in COMPILATA + storico.revocato=true
    @PatchMapping("/revoca-validazione/{id}")
    public ResponseEntity<Sezione1DTO> revocaValidazione(@PathVariable Long id, @RequestBody String osservazioni,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione1Service.revocaValidazione(id, osservazioni, userNameSurname, userRole, fiscalCode,testoSezione,campiModificati));
    }

    // Rifiuta la validazione: da IN_VALIDAZIONE passiamo in  COMPILATA + storico.rifiutato=true
    @PatchMapping("/annulla-validazione/{id}")
    public ResponseEntity<Sezione1DTO> annullaValidazione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo",required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati",required = false) String campiModificati) {
        return ResponseEntity.ok(sezione1Service.annullaValidazione(id, userNameSurname, userRole,fiscalCode,testoSezione,campiModificati));
    }

}
