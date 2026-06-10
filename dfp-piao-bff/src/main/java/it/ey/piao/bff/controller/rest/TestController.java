package it.ey.piao.bff.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.TestDTO;
import it.ey.piao.bff.service.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
//Rest controller per chiamate rest ai servizi esposti del modulo API che comunica direttamente con le fonte dati in gioco
@ApiV1Controller("/test")
@SecurityRequirement(name = "bearerAuth")
public class TestController {
  private final   ITestService testService;

    @Autowired
    public TestController(ITestService testService) {
        this.testService = testService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_offline_access')")//Permette a spring di rendere accessibili le api sulla base dei ruoli contenuti nel token
        public Mono<ResponseEntity<GenericResponseDTO<List<TestDTO>>>> getAllTests() {
            return testService.getAllTest()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
        }

        @PostMapping
        @Operation(summary = "Salva un nuovo test", description = "Riceve un oggetto TestDTO e lo salva nel database e genera PDF e salva su bucket S3")
        public Mono<ResponseEntity<String>> salvaTest(@RequestBody TestDTO request) {
            return testService.Save(request)
                .thenReturn(ResponseEntity.ok("OK"))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore")));
        }



    }
