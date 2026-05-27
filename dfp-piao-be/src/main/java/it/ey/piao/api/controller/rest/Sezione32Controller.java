package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.Sezione32DTO;
import it.ey.piao.api.service.ISezione32Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ApiV1Controller("/sezione32")
public class Sezione32Controller
{
    private final ISezione32Service sezione32Service;

    public Sezione32Controller(ISezione32Service sezione32Service)
    {
        this.sezione32Service = sezione32Service;
    }

    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody Sezione32DTO request)
    {
        sezione32Service.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/validazione/{id}")
    public ResponseEntity<Sezione32DTO> richiediValidazione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione32Service.richiediValidazione(id, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    @GetMapping("/{idPiao}")
    public ResponseEntity<Sezione32DTO> getByIdPiao(@PathVariable Long idPiao)
    {
        return ResponseEntity.ok(sezione32Service.findByIdPiao(idPiao));
    }


    @PatchMapping("/valida-sezione/{id}")
    public ResponseEntity<Sezione32DTO> validaSezione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione32Service.validaSezione(id, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    // Rifiuta la validazione: da IN_VALIDAZIONE passiamo in  COMPILATA + storico.rifiutato = true

    @PatchMapping("/rifiuta-validazione/{id}")
    public ResponseEntity<Sezione32DTO> rifiutaValidazione(@PathVariable Long id, @RequestBody String osservazioni,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione32Service.rifiutaValidazione(id, osservazioni, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    // Revoca la validazione: da VALIDATA passiamo in COMPILATA + storico.revocato = true

    @PatchMapping("/revoca-validazione/{id}")
    public ResponseEntity<Sezione32DTO> revocaValidazione(@PathVariable Long id, @RequestBody String osservazioni,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione32Service.revocaValidazione(id, osservazioni, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    // Annulla la validazione: da IN_VALIDAZIONE passiamo in  COMPILATA + storico.annullato = true

    @PatchMapping("/annulla-validazione/{id}")
    public ResponseEntity<Sezione32DTO> annullaValidazione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione32Service.annullaValidazione(id, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }
}
