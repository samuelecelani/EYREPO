package it.ey.piao.bff.configuration.security;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.UserDTO;
import it.ey.piao.bff.service.IUserService;
import it.ey.piao.bff.service.impl.JwtClaimsService;
import it.ey.piao.bff.util.SpringContextBridge;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;


public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final JwtClaimsService jwtClaimsService;
    private final IUserService userService;

    public KeycloakRealmRoleConverter() {
        this.userService = SpringContextBridge.getBean(IUserService.class);
        this.jwtClaimsService = SpringContextBridge.getBean(JwtClaimsService.class);
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        // Ruoli da Keycloak (già GrantedAuthority)
        List<GrantedAuthority> keycloakAuthorities = jwtClaimsService.getRealmRoles(jwt);

        // TODO: Insieme ai ruoli del keykclock bisogna recuperare i ruoli
        // legati all'utente loggato (Chiamando il servizio esterno che ci verrà fornito)
        // che potranno essere anche ruoli solo per determinate sezioni del PIAO

        GenericResponseDTO<UserDTO> response = userService.getUserbyToken().block();
        List<GrantedAuthority> customAuthorities = new ArrayList<>();

        if (response != null && response.getData() != null) {
            UserDTO user = response.getData();

            if (user.getPaRiferimento() != null) {
                user.getPaRiferimento().forEach(pa -> {
                    if (pa.getRuoli() != null) {
                        pa.getRuoli().forEach(ruolo -> {
                            if (ruolo.isRuoloAttivo()) {

                                //TODO: Capire se i ruoli globali vanno prelevati dal token staccato da keyclack
//                                // Ruolo globale
//                                customAuthorities.add(new SimpleGrantedAuthority("ROLE_" + ruolo.getCodice()));

                                // Ruoli per sezioni associate
                                if (ruolo.getSezioneAssociata() != null) {
                                    ruolo.getSezioneAssociata().forEach(sezione ->
                                        customAuthorities.add(new SimpleGrantedAuthority("ROLE_" + ruolo.getCodice() + "_" + sezione))
                                    );
                                }
                            }
                        });
                    }
                });
            }
        }

        // Combino le due liste
        List<GrantedAuthority> allAuthorities = new ArrayList<>(keycloakAuthorities);
        allAuthorities.addAll(customAuthorities);

        return allAuthorities;
    }
}
