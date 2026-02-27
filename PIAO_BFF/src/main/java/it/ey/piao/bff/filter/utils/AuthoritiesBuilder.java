package it.ey.piao.bff.filter.utils;


import it.ey.dto.UserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AuthoritiesBuilder {

    public static List<GrantedAuthority> fromUser(UserDTO user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (user.getPaRiferimento() != null) {
            user.getPaRiferimento().forEach(pa -> {
                if (pa.getRuoli() != null) {
                    pa.getRuoli().forEach(ruolo -> {
                        if (ruolo.isRuoloAttivo()) {

                            // SOLO ruoli per sezioni associate (come da tua richiesta, senza Keycloak)
                            if (ruolo.getSezioneAssociata() != null) {
                                ruolo.getSezioneAssociata().stream()
                                        .filter(Objects::nonNull)
                                        .forEach(sezione ->
                                                authorities.add(new SimpleGrantedAuthority("ROLE_" + ruolo.getCodice() + "_" + sezione))
                                        );
                            }

                            // Se in futuro vorrai anche il ruolo “globale”, scommenta:
                            // authorities.add(new SimpleGrantedAuthority("ROLE_" + ruolo.getCodice()));
                        }
                    });
                }
            });
        }

        // Se vuoi aggiungere typeAuthority come macro-ruolo:
        // if (user.getTypeAuthority() != null) {
        //     authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getTypeAuthority().name()));
        // }

        return authorities;
    }
}
