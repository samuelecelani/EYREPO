package it.ey.externaldto.mapper;

import it.ey.dto.RuoloUtenteDTO;
import it.ey.dto.UtentePaDTO;
import it.ey.dto.UtenteRuoloPaDTO;
import it.ey.dto.UserDTO;
import it.ey.dto.PaRiferimentoDTO;
import it.ey.dto.RuoloUserDTO;
import it.ey.enums.RolePrivilege;
import it.ey.enums.TypeAuthority;
import it.ey.externaldto.RoleDto;
import it.ey.externaldto.UserProfileDto;
import it.ey.utils.RoleUtils;

import java.util.*;

import static it.ey.utils.RoleUtils.rolePriority;

public class MapperUtenti {

    private MapperUtenti() {
        // utility class
    }

    // ──────────────────────────────────────────────────────────────
    //  UserProfileDto (BIP) → UtenteRuoloPaDTO (interno)
    // ──────────────────────────────────────────────────────────────

    /**
     * Converte un UserProfileDto (BIP) in UtenteRuoloPaDTO (interno),
     * filtrando l'amministrazione in base all'ipaCode fornito.
     * Email, telefono e ruoli vengono presi dall'amministrazione corrispondente.
     *
     * @param src     profilo utente da BIP
     * @param ipaCode codice IPA dell'amministrazione attiva (può essere null → usa tutte)
     */
    public static UtenteRuoloPaDTO convert(UserProfileDto src, String ipaCode) {
        if (src == null) return null;

        List<UserProfileDto.AmministrazioneDto> tutteAmministrazioni =
                Optional.ofNullable(src.getAmministrazioni()).orElseGet(List::of);

        // Filtra per ipaCode se fornito, altrimenti usa tutte
        List<UserProfileDto.AmministrazioneDto> amministrazioniFiltrate = tutteAmministrazioni.stream()
                .filter(Objects::nonNull)
                .filter(amm -> ipaCode == null || ipaCode.equals(amm.getIpaCode()))
                .toList();

        // Se il filtro non trova nulla, fallback su tutte
        List<UserProfileDto.AmministrazioneDto> amministrazioni =
                amministrazioniFiltrate.isEmpty() ? tutteAmministrazioni : amministrazioniFiltrate;

        // Ruoli: flatten dalle amministrazioni filtrate
        List<RuoloUtenteDTO> ruoliAmministrazioni = amministrazioni.stream()
                .filter(Objects::nonNull)
                .flatMap(amm -> Optional.ofNullable(amm.getRuoli())
                        .orElseGet(List::of)
                        .stream())
                .filter(Objects::nonNull)
                .map(MapperUtenti::convertRole)
                .toList();

        // Aggiungi il dfpRole a livello root, se presente
        List<RuoloUtenteDTO> ruoli = new ArrayList<>(ruoliAmministrazioni);
        if (src.getDfpRole() != null) {
            ruoli.add(convertRole(src.getDfpRole()));
        }

        // PA (solo quelle filtrate)
        List<UtentePaDTO> pa = amministrazioni.stream()
                .filter(Objects::nonNull)
                .map(MapperUtenti::convertAmministrazione)
                .toList();

        // Email e telefono istituzionali: dall'amministrazione filtrata
        String emailIstituzionale = amministrazioni.stream()
                .filter(Objects::nonNull)
                .map(UserProfileDto.AmministrazioneDto::getEmail)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(src.getEmail()); // fallback su email personale

        String telefonoIstituzionale = amministrazioni.stream()
                .filter(Objects::nonNull)
                .map(UserProfileDto.AmministrazioneDto::getPhone)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(src.getPhone()); // fallback su telefono personale

        return UtenteRuoloPaDTO.builder()
                .id(src.getId())
                .codiceFiscale(src.getCodiceFiscale())
                .nome(src.getName())
                .cognome(src.getSurname())
                .email(emailIstituzionale)
                .numeroTelefono(telefonoIstituzionale)
                .dataNascita(src.getDataDiNascita())
                .luogoNascita(src.getLuogoDiNascita())
                .ruoli(ruoli)
                .codicePA(pa)
                .build();
    }

    /**
     * Overload retrocompatibile senza filtro ipaCode (usa tutte le amministrazioni).
     */
    public static UtenteRuoloPaDTO convert(UserProfileDto src) {
        return convert(src, null);
    }

    // ──────────────────────────────────────────────────────────────
    //  UtenteRuoloPaDTO (interno) → UserProfileDto (BIP)
    // ──────────────────────────────────────────────────────────────

    public static UserProfileDto convert(UtenteRuoloPaDTO src) {
        if (src == null) return null;

        List<RoleDto> ruoli = Optional.ofNullable(src.getRuoli())
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .map(r -> RoleDto.builder()
                        //.id(r.getNomeRuolo())
                        .name(r.getCodiceRuolo() != null ? r.getCodiceRuolo() : "ROLE_" + r.getNomeRuolo().toUpperCase())
                        .typology(r.getTipologia())
                        .build())
                .toList();

        // Typology a livello root: presa dal primo ruolo che ha una tipologia
        String typology = Optional.ofNullable(src.getRuoli())
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .map(RuoloUtenteDTO::getTipologia)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        List<UserProfileDto.AmministrazioneDto> pa = Optional.ofNullable(src.getCodicePA())
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .map(p -> UserProfileDto.AmministrazioneDto.builder()
                        .id(p.getId() != null ? String.valueOf(p.getId()) : null)
                        .ipaCode(p.getCodicePa())
                        .name(p.getNome())
                        .phone(p.getPhone() != null ? p.getPhone() : src.getNumeroTelefono())
                        .email(p.getEmail() != null ? p.getEmail() : src.getEmail())
                        .qualifica(p.getQualifica())
                        .ruoli(ruoli)
                        .build())
                .toList();

        return UserProfileDto.builder()
                .codiceFiscale(src.getCodiceFiscale())
                .name(src.getNome())
                .surname(src.getCognome())
                .email(src.getEmail())
                .phone(src.getNumeroTelefono())
                .dataDiNascita(src.getDataNascita())
                .luogoDiNascita(src.getLuogoNascita())
                .typology(typology)
                .amministrazioni(pa)
                .build();
    }

    // ──────────────────────────────────────────────────────────────
    //  Lista helpers
    // ──────────────────────────────────────────────────────────────

    public static List<UtenteRuoloPaDTO> convertList(List<UserProfileDto> src, String ipaCode) {
        if (src == null) return List.of();
        return src.stream().map(s -> MapperUtenti.convert(s, ipaCode)).toList();
    }

    public static List<UtenteRuoloPaDTO> convertList(List<UserProfileDto> src) {
        return convertList(src, null);
    }

    public static List<UserProfileDto> convertToExternal(List<UtenteRuoloPaDTO> src) {
        if (src == null) return List.of();
        return src.stream().map(MapperUtenti::convert).toList();
    }

    // ──────────────────────────────────────────────────────────────
    //  Inner converters
    // ──────────────────────────────────────────────────────────────

    private static RuoloUtenteDTO convertRole(RoleDto r) {
        String roleName = r.getName();
        String nomeRuolo = roleName != null
                ? roleName.replaceFirst("^ROLE_", "").toLowerCase()
                : null;
        return RuoloUtenteDTO.builder()
                .codiceRuolo(roleName)
                .nomeRuolo(nomeRuolo)
                .tipologia(r.getTypology())
                .build();
    }

    private static UtentePaDTO convertAmministrazione(UserProfileDto.AmministrazioneDto amm) {
        return UtentePaDTO.builder()
                .codicePa(amm.getId())
                .nome(amm.getName())
                .phone(amm.getPhone())
                .email(amm.getEmail())
                .qualifica(amm.getQualifica())
                .build();
    }

    // ──────────────────────────────────────────────────────────────
    //  UserProfileDto (gateway/BIP) → UserDTO (BFF interno)
    // ──────────────────────────────────────────────────────────────

    public static UserDTO convertToUserDTO(UserProfileDto src) {
        if (src == null) return null;

        List<UserProfileDto.AmministrazioneDto> amministrazioni =
                Optional.ofNullable(src.getAmministrazioni()).orElseGet(List::of);

        // Ricava typeAuthority: prima dal campo typology a livello root, poi fallback dai ruoli delle amministrazioni
        TypeAuthority typeAuthority = null;
        if (src.getTypology() != null) {
            try {
                typeAuthority = TypeAuthority.valueOf(src.getTypology().toUpperCase());
            } catch (IllegalArgumentException e) {
                typeAuthority = null;
            }
        }
        if (typeAuthority == null) {
            typeAuthority = amministrazioni.stream()
                    .filter(Objects::nonNull)
                    .flatMap(amm -> Optional.ofNullable(amm.getRuoli()).orElseGet(List::of).stream())
                    .filter(Objects::nonNull)
                    .map(RoleDto::getTypology)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(typology -> {
                        try {
                            return TypeAuthority.valueOf(typology.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .orElse(null);
        }

        List<PaRiferimentoDTO> paList = new ArrayList<>();
        for (int i = 0; i < amministrazioni.size(); i++) {
            UserProfileDto.AmministrazioneDto amm = amministrazioni.get(i);

            // Ruoli dall'amministrazione + dfpRole a livello root
            List<RoleDto> rawRoles = new ArrayList<>(
                    Optional.ofNullable(amm.getRuoli())
                            .orElseGet(List::of)
                            .stream()
                            .filter(Objects::nonNull)
                            .toList());
            if (src.getDfpRole() != null) {
                rawRoles.add(src.getDfpRole());
            }

            // Individua il nome del ruolo con priorità più alta per questa amministrazione
            String topRoleName = rawRoles.stream()
                    .map(RoleDto::getName)
                    .filter(Objects::nonNull)
                    .min(Comparator.comparingInt(RoleUtils::rolePriority))
                    .orElse(null);

            List<RuoloUserDTO> ruoli = rawRoles.stream()
                    .map(r -> {
                        String roleName = r.getName();
                        String descrizione = roleName != null
                                ? roleName.replaceFirst("^ROLE_", "").toLowerCase()
                                : null;
                        boolean attivo = roleName != null && roleName.equals(topRoleName);

                        List<String> privilegeNames = RolePrivilege.valueOf(r.getName()).getPrivileges().stream()
                                .map(Enum::name)
                                .toList();

                        return RuoloUserDTO.builder()
                                .codice(roleName)
                                .descrizione(descrizione)
                                .ruoloAttivo(attivo)
                                .sezioneAssociata(Collections.emptyList())
                                .tipologia(r.getTypology())
                                .privileges(privilegeNames)
                                .build();
                    })
                    .toList();

            paList.add(PaRiferimentoDTO.builder()
                    .attiva(false)
                    .externalId(amm.getId())
                    .codePA(amm.getIpaCode())
                    .denominazionePA(amm.getName())
                    .email(amm.getEmail())
                    .fiscalCode(amm.getFiscalCode())
                    .numeroTelefono(amm.getPhone())
                    .qualifica(amm.getQualifica())
                    .ruoli(ruoli)
                    .build());
        }

        return UserDTO.builder()
                .fiscalCode(src.getCodiceFiscale())
                .externalId(src.getId())
                .nome(src.getName())
                .cognome(src.getSurname())
                .luogoDiNascita(src.getLuogoDiNascita())
                .dataNascita(src.getDataDiNascita())
                .paRiferimento(paList)
                .typeAuthority(typeAuthority)
                .build();
    }
}
