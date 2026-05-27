package it.ey.piao.api.controller.rest;


import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione1DTO;
import it.ey.dto.Sezione23DTO;
import it.ey.dto.Sezione331DTO;
import it.ey.piao.api.service.ISezione331Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ApiV1Controller("/sezione331")
public class Sezione331Controller {

    private final ISezione331Service sezione331Service;

    public Sezione331Controller(ISezione331Service sezione331Service) {
        this.sezione331Service = sezione331Service;
    }


    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody Sezione331DTO request) {
        sezione331Service.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/validazione/{id}")
    public ResponseEntity<Sezione331DTO> richiediValidazione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione331Service.richiediValidazione(id, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }
    @GetMapping("/{idPiao}")
    public ResponseEntity<Sezione331DTO> getByIdPiao(@PathVariable Long idPiao) {
        return ResponseEntity.ok(sezione331Service.findByIdPiao(idPiao));
    }


    @PatchMapping("/valida-sezione/{id}")
    public ResponseEntity<Sezione331DTO> validaSezione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione331Service.validaSezione(id, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    // Rifiuta la validazione: da IN_VALIDAZIONE passiamo in  COMPILATA + storico.rifiutato=true
    @PatchMapping("/rifiuta-validazione/{id}")
    public ResponseEntity<Sezione331DTO> rifiutaValidazione(@PathVariable Long id, @RequestBody String osservazioni,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione331Service.rifiutaValidazione(id, osservazioni, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    // Revoca la validazione: da VALIDATA passiamo in COMPILATA + storico.revocato=true
    @PatchMapping("/revoca-validazione/{id}")
    public ResponseEntity<Sezione331DTO> revocaValidazione(@PathVariable Long id, @RequestBody String osservazioni,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione331Service.revocaValidazione(id, osservazioni, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    // Annulla la validazione: da IN_VALIDAZIONE passiamo in  COMPILATA + storico.annullato=true
    @PatchMapping("/annulla-validazione/{id}")
    public ResponseEntity<Sezione331DTO> annullaValidazione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione331Service.annullaValidazione(id, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

}
