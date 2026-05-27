package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.Sezione1DTO;
import it.ey.dto.Sezione31DTO;
import it.ey.piao.api.service.ISezione31Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ApiV1Controller("/sezione31")
public class Sezione31Controller
{
    private final ISezione31Service sezione31Service;

    public Sezione31Controller(ISezione31Service sezione31Service) {
        this.sezione31Service = sezione31Service;
    }

    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody Sezione31DTO request)
    {
        sezione31Service.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/validazione/{id}")
    public ResponseEntity<Sezione31DTO> richiediValidazione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione31Service.richiediValidazione(id, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    @GetMapping("/{idPiao}")
    public ResponseEntity<Sezione31DTO> getByIdPiao(@PathVariable Long idPiao)
    {
        return ResponseEntity.ok(sezione31Service.findByIdPiao(idPiao));
    }


    @PatchMapping("/valida-sezione/{id}")
    public ResponseEntity<Sezione31DTO> validaSezione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione31Service.validaSezione(id, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    // Rifiuta la validazione: da IN_VALIDAZIONE passiamo in  COMPILATA + storico.rifiutato=true
    @PatchMapping("/rifiuta-validazione/{id}")
    public ResponseEntity<Sezione31DTO> rifiutaValidazione(@PathVariable Long id, @RequestBody String osservazioni,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione31Service.rifiutaValidazione(id, osservazioni, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    // Revoca la validazione: da VALIDATA passiamo in COMPILATA + storico.revocato=true
    @PatchMapping("/revoca-validazione/{id}")
    public ResponseEntity<Sezione31DTO> revocaValidazione(@PathVariable Long id, @RequestBody String osservazioni,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione31Service.revocaValidazione(id, osservazioni, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    // Annulla la validazione: da IN_VALIDAZIONE passiamo in  COMPILATA + storico.annullato=true
    @PatchMapping("/annulla-validazione/{id}")
    public ResponseEntity<Sezione31DTO> annullaValidazione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione31Service.annullaValidazione(id, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }
}
