package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.ObiettivoPrevenzioneDTO;
import it.ey.piao.api.service.IObiettivoPrevenzioneService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/obiettivo-prevenzione")
public class ObiettivoPrevenzioneController {
    private final IObiettivoPrevenzioneService obiettivoPrevenzioneService;

    public ObiettivoPrevenzioneController(IObiettivoPrevenzioneService obiettivoPrevenzioneService) {
        this.obiettivoPrevenzioneService = obiettivoPrevenzioneService;
    }

        @PostMapping("/save")
        public ResponseEntity<ObiettivoPrevenzioneDTO> saveOrUpdate(@RequestBody ObiettivoPrevenzioneDTO request) {
            return ResponseEntity.ok( obiettivoPrevenzioneService.saveOrUpdate(request));
        }


        /**
         * Recupera tutti gli obiettivi di performance per una Sezione23.
         */
        @GetMapping("/sezione23/{idSezione23}")
        public ResponseEntity<List<ObiettivoPrevenzioneDTO>> getAllBySezione23(@PathVariable Long idSezione23) {
            List<ObiettivoPrevenzioneDTO> response = obiettivoPrevenzioneService.getAllBySezione23(idSezione23);
            return ResponseEntity.ok(response);
        }

        /**
         * Elimina un obiettivo di performance per ID.
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteById(@PathVariable Long id,
                                               @RequestParam(required = false) String campiModificati,
                                               @RequestParam(required = false) Long idPiao,
                                               @RequestParam(required = false) String testoSezione,
                                               HttpServletRequest request)
        {
            String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
            String updatedByRole = request.getHeader("X-Updated-By-Role");
            String statoSezione = request.getHeader("X-Stato-Sezione");
            obiettivoPrevenzioneService.deleteById(id, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole, statoSezione);
            return ResponseEntity.noContent().build();
        }
    }

