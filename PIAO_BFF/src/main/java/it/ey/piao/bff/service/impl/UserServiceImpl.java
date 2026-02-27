package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.CodTipologia;
import it.ey.enums.TypeAuthority;
import it.ey.enums.WebServiceType;
import it.ey.externaldto.RoleDto;
import it.ey.externaldto.UserProfileDto;
import it.ey.externaldto.mapper.MapperUtenti;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.mapper.AuthorityMapper;
import it.ey.piao.bff.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    private final JwtClaimsService  jwtClaimsService;
    private final WebClientService webClientService;
    private final List<WebServiceType> webServiceType;
    private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    public UserServiceImpl(JwtClaimsService jwtClaimsService, WebClientService webClientService) {
        this.jwtClaimsService=jwtClaimsService;
        this.webClientService=webClientService;
        this.webServiceType =List.of( WebServiceType.API, WebServiceType.BIP);
    }

    @Override
    public Mono<GenericResponseDTO<UserDTO>> getUserbyToken() {
        GenericResponseDTO<UserDTO> response =  new GenericResponseDTO<>();
        response.setStatus(new Status());
        String fiscalCode = "zzzzzz";    // Esempio di recupero dal token -> jwtClaimsService.getFiscalCode();
       //TODO:Impelmentare una chiamata ad un servizio che al momento non abbiamo. Usare webClient
        if (StringUtils.isBlank(fiscalCode)) {
            response.getStatus().setSuccess(false);
            response.setError(new Error());
            response.getError().setMessageError("Invalid or null fiscal code");
            return Mono.just(response);
        }
        // Simulazione della chiamata al servizio esterno per ottenere i ruoli
        //I ruoli vanno prelevati dal token, quando avremmo configurato correttamente il KeyKloack
        List<String> roles = List.of("Super User", "Redattore");

        List<String> formattedRoles = roles.stream()
            .map(role -> role.replace(" ", "_").toUpperCase())
            .toList();

        // esempio di ruoli
        String ruoloAttivo = formattedRoles.get(0);
        List<String> pa = List.of("Comune di Rimini", "Comune di Roma"); // esempio di PA
        String paAttiva = pa.get(0);
        String nome = "Esempio";
        // Mappatura dei ruoli in TypeAuthority
        TypeAuthority typeAuthority = AuthorityMapper.mapRolesToAuthority(roles);
        // Creazione del DTO

        UserDTO userDTO = UserDTO.builder().fiscalCode(fiscalCode).nome("Samuele").cognome("Celani")
            .email("emailprova@istituzione.it")
            .fiscalCode("XXXX")
            .paRiferimento(Collections.singletonList(
            PaRiferimentoDTO.builder()
                .codePA("1234").attiva(true).denominazionePA("Comune di Rimini")
                .ruoli( Collections.singletonList(
                    RuoloUserDTO.builder().ruoloAttivo(true)
                        .codice("REFERENTE")
                        .descrizione("referente")
                        .sezioneAssociata(Collections.singletonList(CodTipologia.SEZ2_2.toString())).build()))
                .build())
        ).typeAuthority(TypeAuthority.PA).build();

            // new UserDTO(nome,fiscalCode, formattedRoles, ruoloAttivo,pa,paAttiva,typeAuthority);
        response.setData(userDTO);
        response.getStatus().setSuccess(true);
        // Wrapping nella response
        return Mono.just(response);

    }
//
//    @Override
//    public Mono<GenericResponseDTO<List<UtenteRuoloPaDTO>>> findUtentiByPa(String codicePa) {
//        log.info("Richiesta lista di tutti gli utenti  data una Pa di riferimento");
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Accept", "application/json");
//
//
//        return webClientService.get("/utente/codice-pa/" + codicePa,webServiceType.getFirst() , headers, new ParameterizedTypeReference<List<UtenteRuoloPaDTO>>() {})
//            .doOnNext(response -> log.info("Utenti ricevuti: {}", response.size()))
//            .map(utenti -> {
//                GenericResponseDTO<List<UtenteRuoloPaDTO>> finalResponse = new GenericResponseDTO<>();
//                finalResponse.setData(utenti);
//                finalResponse.setStatus(new Status());
//                finalResponse.getStatus().setSuccess(Boolean.TRUE);
//                return finalResponse;
//            })
//            .onErrorResume(e -> {
//                log.error("Errore nel recupero dei dati {}", e.getMessage(), e);
//                GenericResponseDTO<List<UtenteRuoloPaDTO>> errorResponse = new GenericResponseDTO<>();
//                errorResponse.setStatus(new Status());
//                errorResponse.getStatus().setSuccess(Boolean.FALSE);
//                errorResponse.setError(new Error());
//                errorResponse.getError().setMessageError(e.getMessage());
//                errorResponse.setData(Collections.emptyList());
//                return Mono.just(errorResponse);
//            });
//    }

    @Override
    public Mono<GenericResponseDTO<List<UtenteRuoloPaDTO>>> findUtentiByPa(String codicePa) {
        log.info("Richiesta lista di tutti gli utenti  data una Pa di riferimento");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        //TODO:Appena ricevute le specifiche di BIP implemntare la chiamata di integrazione

        return webClientService.get("/utente/codice-pa/" + codicePa,webServiceType.getFirst() , headers, new ParameterizedTypeReference<List<UtenteRuoloPaDTO>>() {})

            .doOnNext(response -> log.info("Utenti ricevuti: {}", response.size()))
            .map(utenti -> {
                // Modifico in-place ogni elemento
                utenti.forEach(u -> {
                    u.setNome("Mario");
                    u.setCognome("Rossi");
                    u.setNumeroTelefono("123456789");
                    u.setEmail("test@example.com");
                    u.getRuoli().forEach(ruoli -> {
                        ruoli.setNomeRuolo("REFERENTE");
                    });
                });
                GenericResponseDTO<List<UtenteRuoloPaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(utenti);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })

            .onErrorResume(e -> {
                log.error("Errore nel recupero dei dati {}", e.getMessage(), e);
                GenericResponseDTO<List<UtenteRuoloPaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                errorResponse.setData(Collections.emptyList());
                return Mono.just(errorResponse);
            });
    }
//TODO: Questa è la vera chiamata
//    @Override
//    public Mono<GenericResponseDTO<UtenteRuoloPaDTO>> saveUtenteByPa(UtenteRuoloPaDTO utenteRuoloPaDTO) {
//        log.info("Salvataggio utente/ruolo PA");
//
//        log.info("Richiesta lista di tutti gli utenti  data una Pa di riferimento");
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Accept", "application/json");
//
//        return webClientService
//            // 1) Chiamata al servizio esterno
//            .post("/save/create",
//                webServiceType.getFirst(),
//                MapperUtenti.convert(utenteRuoloPaDTO),   // body destinato al servizio esterno
//                headers,
//                UserProfileDto.class)
//
//            // 2) Se ok, converto il risultato e richiamo il secondo servizio
//            .flatMap(userProfileDto -> {
//                UtenteRuoloPaDTO bodySecondaChiamata = MapperUtenti.convert(userProfileDto);
//                return webClientService.post("/utente",
//                    webServiceType.getFirst(),
//                    bodySecondaChiamata,
//                    headers,
//                    UtenteRuoloPaDTO.class);
//            })
//
//            // 3) Impacchetto nel GenericResponseDTO
//            .map(saved -> {
//                GenericResponseDTO<UtenteRuoloPaDTO> resp = new GenericResponseDTO<>();
//                resp.setData(saved);
//                Status status = new Status();
//                status.setSuccess(Boolean.TRUE);
//                resp.setStatus(status);
//                return resp;
//            })
//
//            .doOnSuccess(r -> log.info("Utente salvato con successo"))
//
//            // 4) Gestione errori (se fallisce una delle due chiamate, non chiamo la successiva e ritorno errore)
//            .onErrorResume(e -> {
//                log.error("Errore nel salvataggio utente {}", e.getMessage(), e);
//                GenericResponseDTO<UtenteRuoloPaDTO> errorResponse = new GenericResponseDTO<>();
//                Status status = new Status();
//                status.setSuccess(Boolean.FALSE);
//                errorResponse.setStatus(status);
//                Error err = new Error();
//                err.setMessageError(e.getMessage());
//                errorResponse.setError(err);
//                return Mono.just(errorResponse);
//            });
//    }


    @Override
    public Mono<GenericResponseDTO<UtenteRuoloPaDTO>> saveUtenteByPa(UtenteRuoloPaDTO utenteRuoloPaDTO) {
        log.info("Salvataggio utente/ruolo PA (prima chiamata MOCK)");

            HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        UserProfileDto mockUserProfile = MapperUtenti.convert(utenteRuoloPaDTO);

        Mono<UserProfileDto> userProfileMono = Mono.just(mockUserProfile);

        return userProfileMono
            .flatMap(userProfileDto -> {
              return webClientService.post("/utente",
                    webServiceType.getFirst(),
                    utenteRuoloPaDTO,
                    headers,
                    UtenteRuoloPaDTO.class);
            })
            .map(saved -> {
                GenericResponseDTO<UtenteRuoloPaDTO> resp = new GenericResponseDTO<>();
                resp.setData(saved);
                Status status = new Status();
                status.setSuccess(Boolean.TRUE);
                resp.setStatus(status);
                return resp;
            })
            .doOnSuccess(r -> log.info("Utente salvato con successo (prima chiamata MOCK)"))
            .onErrorResume(e -> {
                log.error("Errore nel salvataggio utente {}", e.getMessage(), e);
                GenericResponseDTO<UtenteRuoloPaDTO> errorResponse = new GenericResponseDTO<>();
                Status status = new Status();
                status.setSuccess(Boolean.FALSE);
                errorResponse.setStatus(status);
                Error err = new Error();
                err.setMessageError(e.getMessage());
                errorResponse.setError(err);
                return Mono.just(errorResponse);
            });
    }


    @Override
    public Mono<GenericResponseDTO<Void>> deleteUtentePa(Long id) {
        log.info("Eliminazione utente/ruolo PA con id {}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.delete("/utente/" + id, webServiceType.getFirst(), headers)
            .map(v -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnNext(r -> log.info("Utente eliminato con successo: {}", id))
            .onErrorResume(e -> {
                log.error("Errore nell'eliminazione utente {} - {}", id, e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

}
