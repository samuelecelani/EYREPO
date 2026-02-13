package it.ey.piao.api.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GetTestResponse;
import it.ey.dto.TestDTO;
import it.ey.piao.api.service.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//Esempio di RestController. Viene invocato dal BFF
@ApiV1Controller("/test")
public class TestController {

    @Autowired
    ITestService testService;

    @GetMapping
    public ResponseEntity<GetTestResponse> getAllTests() {
        return new ResponseEntity<>(testService.getAllTest(), HttpStatus.OK);
    }
    @PostMapping
    @Operation(summary = "Salva un nuovo test", description = "Riceve un oggetto TestDTO e lo salva nel database")
    public ResponseEntity<TestDTO> SalvaTest(@RequestBody TestDTO request){
        testService.Save(request);
        return ResponseEntity.ok(request);
    }
}
