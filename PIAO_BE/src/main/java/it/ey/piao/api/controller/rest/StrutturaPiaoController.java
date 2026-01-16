package it.ey.piao.api.controller.rest;


import it.ey.dto.StrutturaPiaoDTO;
import it.ey.piao.api.service.IStrutturaPiaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/struttura")
public class StrutturaPiaoController {

    private final IStrutturaPiaoService strutturaService;

    public StrutturaPiaoController(IStrutturaPiaoService strutturaService) {
        this.strutturaService = strutturaService;
    }

    @GetMapping("/piao")
    public ResponseEntity<List<StrutturaPiaoDTO>> getStruttura(@RequestParam Long idPiao) {
        return  ResponseEntity.ok(strutturaService.getAllStruttura(idPiao));
    }
}
