package it.ey.externaldto.mapper;

import it.ey.dto.RuoloDTO;
import it.ey.dto.RuoloUtenteDTO;
import it.ey.dto.UtentePaDTO;
import it.ey.dto.UtenteRuoloPaDTO;
import it.ey.externaldto.RoleDto;
import it.ey.externaldto.UserProfileDto;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MapperUtenti {


    public static UtenteRuoloPaDTO convert(UserProfileDto userProfileDto) {


        List<RuoloUtenteDTO> ruoli = Optional.ofNullable(userProfileDto.getAmministrazioni())
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .flatMap(amm -> Optional.ofNullable(amm.getRuoli())
                        .orElseGet(List::of)
                        .stream())
                .filter(Objects::nonNull)
                .map(r -> RuoloUtenteDTO.builder()
                        .codiceRuolo(r.getId())
                        .nomeRuolo(r.getName())
                        .build())
                .toList();

        List<UtentePaDTO> pa = userProfileDto.getAmministrazioni().stream().map(
        a -> UtentePaDTO.builder()
                .codicePa(a.getId())
                .nome(a.getNome())
                .build()).toList();

        return UtenteRuoloPaDTO.builder()
                .codiceFiscale(userProfileDto.getCodiceFiscale())
                .nome(userProfileDto.getName())
                .cognome(userProfileDto.getSurname())
                .email(userProfileDto.getEmail())
                .numeroTelefono(userProfileDto.getPhone())
                .ruoli(ruoli)
                .codicePA(pa)
                .build();
    }

    public static UserProfileDto  convert( UtenteRuoloPaDTO userProfileDto) {
        List<RoleDto> ruoli = userProfileDto.getRuoli().stream().map(
                r-> RoleDto.builder().id(r.getCodiceRuolo()).name(r.getNomeRuolo()).build()
        ).toList();
        List<UserProfileDto.AmministrazioneDto> pa = userProfileDto.getCodicePA().stream().map(
                p -> UserProfileDto.AmministrazioneDto.builder().id(p.getCodicePa()).nome(p.getNome()).ruoli(ruoli).build()
        ).toList();
        return UserProfileDto.builder().amministrazioni(pa).codiceFiscale(userProfileDto.getCodiceFiscale()).email(userProfileDto.getEmail()).name(userProfileDto.getNome()).surname(userProfileDto.getCognome()).build();

    }

}
