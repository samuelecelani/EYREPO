package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.*;
import it.ey.piao.api.service.ISezione332Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/sezione332")
public class Sezione332Controller
{
    private final ISezione332Service sezione332Service;

    public Sezione332Controller(ISezione332Service sezione332Service) {
        this.sezione332Service = sezione332Service;
    }


    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody Sezione332DTO request)
    {
        sezione332Service.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/validazione/{id}")
    public ResponseEntity<Sezione332DTO> richiediValidazione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione332Service.richiediValidazione(id, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }
    @GetMapping("/{idPiao}")
    public ResponseEntity<Sezione332DTO> getByIdPiao(@PathVariable Long idPiao)
    {
        return ResponseEntity.ok(sezione332Service.findByIdPiao(idPiao));
    }


    @PatchMapping("/valida-sezione/{id}")
    public ResponseEntity<Sezione332DTO> validaSezione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione332Service.validaSezione(id, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    // Rifiuta la validazione: da IN_VALIDAZIONE passiamo in  COMPILATA + storico.rifiutato=true
    @PatchMapping("/rifiuta-validazione/{id}")
    public ResponseEntity<Sezione332DTO> rifiutaValidazione(@PathVariable Long id, @RequestBody String osservazioni,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione332Service.rifiutaValidazione(id, osservazioni, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    // Revoca la validazione: da VALIDATA passiamo in COMPILATA + storico.revocato=true
    @PatchMapping("/revoca-validazione/{id}")
    public ResponseEntity<Sezione332DTO> revocaValidazione(@PathVariable Long id, @RequestBody String osservazioni,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione332Service.revocaValidazione(id, osservazioni, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    // Annulla la validazione: da IN_VALIDAZIONE passiamo in  COMPILATA + storico.annullato=true
    @PatchMapping("/annulla-validazione/{id}")
    public ResponseEntity<Sezione332DTO> annullaValidazione(@PathVariable Long id,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode,
            @RequestHeader(value = "X-Testo", required = false) String testoSezione,
            @RequestHeader(value = "X-Campi-Modificati", required = false) String campiModificati) {
        return ResponseEntity.ok(sezione332Service.annullaValidazione(id, userNameSurname, userRole, fiscalCode, testoSezione, campiModificati));
    }

    @GetMapping("/tipologia-attivita")
    public ResponseEntity<List<TipologiaAttivitaDTO>> getTipologiaAttivita()
    {
        List<TipologiaAttivitaDTO> response = sezione332Service.getTipologiaAttivita();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ambito-competenza")
    public ResponseEntity<List<AmbitoCompetenzaDTO>> getAmbitoCompetenza()
    {
        List<AmbitoCompetenzaDTO> response = sezione332Service.getAmbitoCompetenza();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/area-tematica")
    public ResponseEntity<List<AreaTematicaDTO>> getAreaTematica()
    {
        List<AreaTematicaDTO> response = sezione332Service.getAreaTematica();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tipologia-destinatari")
    public ResponseEntity<List<TipologiaDestinatariDTO>> getTipologiaDestinatari()
    {
        List<TipologiaDestinatariDTO> response = sezione332Service.getTipologiaDestinatari();
        return ResponseEntity.ok(response);
    }
}
