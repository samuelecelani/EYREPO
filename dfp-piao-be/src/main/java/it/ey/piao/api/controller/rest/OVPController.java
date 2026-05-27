package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.OVPDTO;
import it.ey.dto.OVPMatriceDataDTO;
import it.ey.piao.api.service.IOVPService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/ovp")
public class OVPController {

    private final IOVPService ovpService;

    public OVPController(IOVPService ovpService) {
        this.ovpService = ovpService;
    }

    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody OVPDTO request) {
        ovpService.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sezione/{idSezione}")
    public ResponseEntity<List<OVPDTO>> getAllBySezione(@PathVariable Long idSezione) {
        return ResponseEntity.ok(ovpService.findAllOVPBySezione(idSezione));
    }

    @GetMapping("/piao/{piaoId}")
    public ResponseEntity<List<OVPDTO>> getAllByPiao(@PathVariable Long piaoId) {
        return ResponseEntity.ok(ovpService.findAllOVPByPiao(piaoId));
    }

    @GetMapping("/matrice-data")
    public ResponseEntity<OVPMatriceDataDTO> getMatriceData(
            @RequestParam(required = false) Long idSezione,
            @RequestParam Long idSezione1,
            @RequestParam(required = false) Long idPiao) {
        return ResponseEntity.ok(ovpService.findOVPMatriceData(idSezione, idSezione1, idPiao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestParam(required = false) String campiModificati,
                                       @RequestParam(required = false) Long idPiao,
                                       @RequestParam(required = false) String testoSezione,
                                       @RequestParam(defaultValue = "false") boolean forceDelete,

                                       HttpServletRequest request)
    {
        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");
        ovpService.deleteById(id, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole,forceDelete, statoSezione);
        return ResponseEntity.noContent().build();
    }
}
