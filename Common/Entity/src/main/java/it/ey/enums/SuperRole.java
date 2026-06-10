package it.ey.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum SuperRole {

    SUPERROLE_ROOT(
            "SUPERROLE_ROOT"),

    SUPERROLE_AMMINISTRATORE(
            "SUPERROLE_AMMINISTRATORE"),

    SUPERROLE_FUNZIONARIO(
            "SUPERROLE_FUNZIONARIO");

    private final String name;

    SuperRole( String name) {
        this.name = name;
    }

    public static SuperRole fromName(String name) {
        return Arrays.stream(values())
                .filter(role -> role.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("SuperRole not recognized: " + name));
    }


    public List<RolePrivilege> getRoles() {
        return Arrays.stream(RolePrivilege.values())
                .filter(role -> this.equals(role.getSuperRole()))
                .toList();
    }

    public Set<Privilege> getAllPrivileges() {
        return getRoles().stream()
                .flatMap(role -> role.getPrivileges().stream())
                .collect(Collectors.toSet());
    }

}
