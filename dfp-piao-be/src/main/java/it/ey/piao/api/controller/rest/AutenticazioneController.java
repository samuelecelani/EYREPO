/*package it.example.piao.api.controller.rest;


import it.example.piao.api.controller.rest.api.IAutenticazioneController;
import it.example.piao.api.model.autenticazione.Autenticazione;
import it.example.piao.api.model.autenticazione.LogoutAutenticazione;
import it.example.piao.api.service.AutenticazioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/autenticazione")
public class AutenticazioneController implements IAutenticazioneController {

    @Autowired
    private AutenticazioneService autenticazioneService;

    @Override
    public ResponseEntity<Map<String, Object>> refreshAutenticazione(@RequestBody Autenticazione autenticazioneParams) {
        Map<String, Object> response = autenticazioneService.refreshToken(
            autenticazioneParams.getRefreshToken(),
            autenticazioneParams.getCodiceFiscale()
        );
        if (response.containsKey("status")) {
            int status = (int) response.get("status");
            String message = (String) response.get("error_description");

            return ResponseEntity
                .status(status)
                .body(Map.of("error", message));
        }
        if (response.containsKey("refresh_token")) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Risposta inattesa dal servizio di autenticazione"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> revokeAutenticazione(@RequestBody LogoutAutenticazione logoutParams){

        Map<String, Object> response = autenticazioneService.revokeToken(logoutParams.getRefreshToken());

        if (response.containsKey("status") && (int) response.get("status") != 200) {
            int status = (int) response.get("status");
            String message = (String) response.get("error_description");
            return ResponseEntity.status(status).body(Map.of("error", message));
        }

        return ResponseEntity.ok(response);
    }
}
*/
