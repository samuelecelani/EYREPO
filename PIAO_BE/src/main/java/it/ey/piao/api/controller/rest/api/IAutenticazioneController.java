/*package it.example.piao.api.controller.rest.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.example.piao.api.model.autenticazione.Autenticazione;
import it.example.piao.api.model.autenticazione.LogoutAutenticazione;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@ComponentScan(lazyInit = true)
@RequestMapping(value = "/autenticazione")
@Tag(name = "Autenticazione", description = "API per la gestione dei token di autenticazione")
public interface IAutenticazioneController {

    @PostMapping(path = "/refresh-token")
    @Operation(summary = "Refresh token", description = "Riceve il token di refresh e CF dal frontend e chiama il servizio esterno per ottenere un nuovo token")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = Map.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token aggiornato correttamente"),
        @ApiResponse(responseCode = "400", description = "Parametri mancanti o non validi"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    ResponseEntity<Map<String, Object>> refreshAutenticazione(@RequestBody Autenticazione autenticazioneParams);


    @PostMapping(path = "/revoke-token")
    @Operation( summary = "Revoca token", description = "Revoca un access token o refresh token inviato dal frontend" )
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = Map.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token revocato correttamente"),
        @ApiResponse(responseCode = "400", description = "Parametri mancanti o non validi"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    ResponseEntity<Map<String, Object>> revokeAutenticazione(@RequestBody LogoutAutenticazione logoutParams);



}
*/
