package it.ey.enums;


import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum RolePrivilege {

    ROLE_ROOT(
            "ROLE_ROOT",
            SuperRole.SUPERROLE_ROOT,
            null),

    ROLE_SUPERUSER(
            "ROLE_SUPERUSER",
            SuperRole.SUPERROLE_AMMINISTRATORE,
            TypeAuthority.DFP),

    ROLE_AMMINISTRATORE(
            "ROLE_AMMINISTRATORE",
            SuperRole.SUPERROLE_AMMINISTRATORE,
            TypeAuthority.DFP),

    ROLE_SUPERVISORE(
            "ROLE_SUPERVISORE",
            SuperRole.SUPERROLE_AMMINISTRATORE,
            TypeAuthority.PA),

    ROLE_REFERENTE(
            "ROLE_REFERENTE",
            SuperRole.SUPERROLE_AMMINISTRATORE,
            TypeAuthority.PA),

    ROLE_COORDINATORE_AMMINISTRATIVO(
            "ROLE_COORDINATORE_AMMINISTRATIVO",
            SuperRole.SUPERROLE_AMMINISTRATORE,
            TypeAuthority.PA),

    ROLE_VALIDATORE(
            "ROLE_VALIDATORE",
            SuperRole.SUPERROLE_AMMINISTRATORE,
            TypeAuthority.PA),

    ROLE_REDATTORE(
            "ROLE_REDATTORE",
            SuperRole.SUPERROLE_AMMINISTRATORE,
            TypeAuthority.PA);




    private final String name;
    private final SuperRole superRole;
    private final TypeAuthority typeAuthority;

    RolePrivilege(String name, SuperRole superRole, TypeAuthority typeAuthority) {
        this.name = name;
        this.superRole = superRole;
        this.typeAuthority = typeAuthority;
    }

    public static RolePrivilege fromName(String name) {
        return Arrays.stream(values())
                .filter(role -> role.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Role not recognized: " + name));
    }



    public static List<RolePrivilege> getByTypeAuthority(TypeAuthority typeAuthority) {
        return Arrays.stream(values())
                .filter(role -> role.typeAuthority == typeAuthority)
                .toList();
    }

    public static List<RolePrivilege> getAllAvailable() {
        return Arrays.stream(values())
                .filter(role -> role != ROLE_ROOT)
                .toList();
    }

    public boolean belongsToTypeAuthority(TypeAuthority typeAuthority) {
        return this.typeAuthority == typeAuthority;
    }

    public List<Privilege> getPrivileges() {
        return switch (this) {
            case ROLE_ROOT -> privilegiRoot();
            case ROLE_SUPERUSER -> privilegiSuperuser();
            case ROLE_AMMINISTRATORE -> privilegiAmministratore();
            case ROLE_SUPERVISORE -> privilegiSupervisore();
            case ROLE_REFERENTE -> privilegiReferente();
            case ROLE_COORDINATORE_AMMINISTRATIVO -> privilegiCoordinatoreAmministrativo();
            case ROLE_VALIDATORE -> privilegiValidatore();
            case ROLE_REDATTORE -> privilegiRedattore();
        };
    }

    public static List<Privilege> getPrivileges(String roleName) {
        RolePrivilege role = fromName(roleName);
        return role.getPrivileges();
    }

    private static List<Privilege> privilegiRoot() {
        return List.of(Privilege.ALL_ALL);
    }

    private static List<Privilege> privilegiSuperuser() {
        return List.of();
    }

    private static List<Privilege> privilegiAmministratore() {
        return List.of();
    }

    private static List<Privilege> privilegiSupervisore() {
        return List.of();
    }

    private static List<Privilege> privilegiReferente() {
        return List.of(
                Privilege.GEST_CTA_NEW_PROFIL,
                Privilege.GEST_LINK_VISUAL_DETAILS,
                Privilege.SCR_COMPILAZIONE_MANCATA_INPUT_FORM,
                Privilege.PIAO_AGGIORNA_INDICE_CTA_CARICA_DOCUMENT,
                Privilege.PIAO_AGGIORNA_CTA_VALID,
                Privilege.PIAO_AGGIORNA_APPR_INPUT_DATA,
                Privilege.PIAO_AGGIORNA_INPUT_DATA,
                Privilege.PIAO_CTA_DETAILS_EDITING,
                Privilege.PIAO_REDIGI_BANNER_SCADENZA
        );
    }

    private static List<Privilege> privilegiCoordinatoreAmministrativo() {
        return List.of(
                Privilege.GEST_CTA_NEW_PROFIL,
                Privilege.GEST_LINK_VISUAL_DETAILS
        );
    }

    private static List<Privilege> privilegiValidatore() {
        return List.of(
                Privilege.PIAO_AGGIORNA_INDICE_CTA_CARICA_DOCUMENT,
                Privilege.PIAO_AGGIORNA_CTA_VALID,
                Privilege.PIAO_CTA_DETAILS_EDITING,
                Privilege.PIAO_REDIGI_BANNER_SCADENZA
        );
    }

    private static List<Privilege> privilegiRedattore() {
        return List.of(
                Privilege.PIAO_AGGIORNA_INDICE_CTA_CARICA_DOCUMENT,
                Privilege.PIAO_CTA_DETAILS_EDITING,
                Privilege.PIAO_REDIGI_BANNER_SCADENZA,
                Privilege.PIAO_AGGIORNA_CTA_VALID
        );
    }

}
