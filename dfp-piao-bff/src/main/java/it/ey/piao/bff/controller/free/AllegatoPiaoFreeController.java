package it.ey.piao.bff.controller.free;

import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IAllegatoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/external/allegato")
@RequiredArgsConstructor
public class AllegatoPiaoFreeController {

    private static final Logger log = LoggerFactory.getLogger(AllegatoPiaoFreeController.class);

    private final IAllegatoService allegatoService;

    @GetMapping(value = "/piao-pdf-url", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponseDTO<String>> getPiaoPdfUrl(@RequestParam Long idPiao) {
        log.info("[GET /external/allegato/piao-pdf-url] idPiao={}", idPiao);

        List<String> codTipologia = List.of("PIAO");
        List<String> codTipologiaAllegato = List.of("PIAO_PDF_GENERATO");

        return allegatoService.getAllegati(codTipologia, codTipologiaAllegato, idPiao, true)
                .map(response -> {
                    GenericResponseDTO<String> result = new GenericResponseDTO<>();
                    result.setStatus(response.getStatus());
                    result.setError(response.getError());
                    result.setMetadato(null);
                    if (response.getData() != null && !response.getData().isEmpty()) {
                        result.setData(response.getData().get(0).getDownloadUrl());
                    }
                    return result;
                });
    }
}
