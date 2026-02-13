package it.ey.piao.bff.service.impl;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

//Service centralizzato per gestire token e recupero dei claims per funzionalità applicativa
@Service
public class JwtClaimsService {

    private Jwt getJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        return null;
    }

    public String getClaimAsString(String claimName) {
        Jwt jwt = getJwt();
        return jwt != null ? jwt.getClaimAsString(claimName) : null;
    }

    public Map<String, Object> getClaimAsMap(String claimName) {
        Jwt jwt = getJwt();
        return jwt != null ? jwt.getClaim(claimName) : Collections.emptyMap();
    }

    public List<String> getClaimAsList(String claimName) {
        Jwt jwt = getJwt();
        Object claim = jwt != null ? jwt.getClaim(claimName) : null;
        if (claim instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return Collections.emptyList();
    }


    public List<GrantedAuthority> getRealmRoles(Jwt jwt) {
        if (jwt == null) {
            return Collections.emptyList();
        }
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }

        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof Collection<?> roles)) {
            return Collections.emptyList();
        }

        return roles.stream()
            .filter(Objects::nonNull)
            .map(Object::toString)
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());
    }


    public List<GrantedAuthority> getRealmRoles() {
        return getRealmRoles(getJwt());
    }


    public String getFiscalCode() {
        return getClaimAsString("fiscal_code"); //TODO:In fase di configurazione del Keyklock definire il nome del claim per poterlo recuperare
    }


    //Da ampliare in base all'esigenza
}
