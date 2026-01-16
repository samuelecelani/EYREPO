package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.TypeAuthority;
import it.ey.piao.bff.mapper.AuthorityMapper;
import it.ey.piao.bff.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

private final JwtClaimsService  jwtClaimsService;
private final  WebClient webClient;

@Autowired
public UserServiceImpl(JwtClaimsService jwtClaimsService,WebClient webClient) {
    this.jwtClaimsService=jwtClaimsService;
    this.webClient=webClient;
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
        String ruoloAttivo = formattedRoles.getFirst();
        List<String> pa = List.of("Comune di Rimini", "Comune di Roma"); // esempio di PA
        String paAttiva = pa.getFirst();
        String nome = "Esempio";
        // Mappatura dei ruoli in TypeAuthority
        TypeAuthority typeAuthority = AuthorityMapper.mapRolesToAuthority(roles);
        // Creazione del DTO

        UserDTO userDTO = UserDTO.builder().fiscalCode(fiscalCode).nome("Prova").cognome("Prova")
            .email("emailprova@istituzione.it")
            .paRiferimento(Collections.singletonList(
            PaRiferimentoDTO.builder()
                .codePA("1234").attiva(true).denominazionePA("Comune inventato")
                .ruoli( Collections.singletonList(
                    RuoloUserDTO.builder().ruoloAttivo(true)
                        .codice("AMMINISTRATORE")
                        .descrizione("Amministratore")
                        .sezioneAssociata(Collections.singletonList("SEZIONE1")).build()))
                .build())
        ).typeAuthority(TypeAuthority.DFP).build();

            // new UserDTO(nome,fiscalCode, formattedRoles, ruoloAttivo,pa,paAttiva,typeAuthority);
        response.setData(userDTO);
        response.getStatus().setSuccess(true);
        // Wrapping nella response
        return Mono.just(response);




    }
}
