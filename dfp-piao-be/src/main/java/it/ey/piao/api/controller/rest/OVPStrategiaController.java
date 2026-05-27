package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.OVPStrategiaDTO;
import it.ey.piao.api.service.IOVPStrategiaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/ovp-strategia")
public class OVPStrategiaController {

    private final IOVPStrategiaService iovpStrategiaService;

    public OVPStrategiaController(IOVPStrategiaService iovpStrategiaService) {
        this.iovpStrategiaService = iovpStrategiaService;
    }

    @PostMapping("/save/{idOVP}")
    public ResponseEntity<OVPStrategiaDTO> save(@RequestBody OVPStrategiaDTO request, @PathVariable Long idOVP) {
        return ResponseEntity.ok(iovpStrategiaService.save(request, idOVP));
    }

    @GetMapping("/ovp/{idOvp}")
    public ResponseEntity<List<OVPStrategiaDTO>> findByOvpId(@PathVariable Long idOvp) {
        return ResponseEntity.ok(iovpStrategiaService.findByOvpId(idOvp));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestParam(required = false) String campiModificati,
                                       @RequestParam(required = false) Long idPiao,
                                       @RequestParam(required = false) String testoSezione,
                                       @RequestParam(defaultValue = "false") boolean forceDelete,

                                       HttpServletRequest request) {
        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");
        iovpStrategiaService.delete(id, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole, forceDelete, statoSezione);
        return ResponseEntity.noContent().build();
    }

}
