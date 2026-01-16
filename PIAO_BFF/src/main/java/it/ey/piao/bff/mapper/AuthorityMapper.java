package it.ey.piao.bff.mapper;

import it.ey.enums.TypeAuthority;

import java.util.List;
import java.util.Map;

public class AuthorityMapper {

//TODO: VAlutare se rendere dimanici i ruoli prendendoli a DB
    private static final Map<String, TypeAuthority> ROLE_TO_AUTHORITY_MAP = Map.ofEntries(
        Map.entry("Super User", TypeAuthority.DFP),
        Map.entry("Amministratore", TypeAuthority.DFP),
        Map.entry("Supervisore", TypeAuthority.PA_CAPOFILA),
        Map.entry("Referente", TypeAuthority.PA),
        Map.entry("Coordinatore Amministrativo", TypeAuthority.PA),
        Map.entry("Validatore", TypeAuthority.PA),
        Map.entry("Redattore", TypeAuthority.PA)
    );

    public static TypeAuthority mapRolesToAuthority(List<String> roles) {
        for (String role : roles) {
            TypeAuthority authority = ROLE_TO_AUTHORITY_MAP.get(role);
            if (authority != null) {
                return authority;
            }
        }
        return null; // oppure TypeAuthority.PA come default
    }
}
