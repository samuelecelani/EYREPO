package it.ey.piao.bff.controller.rest;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.StrutturaPiaoDTO;
import it.ey.piao.bff.service.IStrutturaPiaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/struttura")
public class StrutturaPiaoController {


    private final IStrutturaPiaoService strutturaPiaoService;

    public StrutturaPiaoController(IStrutturaPiaoService strutturaPiaoService) {
        this.strutturaPiaoService = strutturaPiaoService;
    }

    @GetMapping("/piao")
    public Mono<ResponseEntity<GenericResponseDTO<List<StrutturaPiaoDTO>>> > getAllStutturaPiao(@RequestParam Long idPiao) {
        return strutturaPiaoService.getStrutturaPiao(idPiao).map(ResponseEntity::ok);
    }
}
