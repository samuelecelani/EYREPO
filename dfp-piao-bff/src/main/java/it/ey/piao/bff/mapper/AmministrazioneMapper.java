package it.ey.piao.bff.mapper;

import it.ey.dto.AmministrazioneInternalDTO;
import it.ey.dto.AnagraficaDTO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper per convertire AnagraficaDTO (dal BE) in AmministrazioneInternalDTO (interno BFF).
 */
public class AmministrazioneMapper {

    private AmministrazioneMapper() {
        // utility class
    }

    /**
     * Converte un singolo AnagraficaDTO in AmministrazioneInternalDTO.
     */
    public static AmministrazioneInternalDTO toInternal(AnagraficaDTO anagrafica) {
        if (anagrafica == null) {
            return null;
        }
        return AmministrazioneInternalDTO.builder()
                .id(anagrafica.getId())
                .denominazioneEnte(anagrafica.getDenominazioneEnte())
                .codiceIPA(anagrafica.getCodiceIPA())
                .tipologiaIstat(anagrafica.getTipologiaIstat())
                .codiceFiscale(anagrafica.getCodiceFiscale())
                .mail(anagrafica.getMail())
                .tipologiaPA(anagrafica.getTipologiaPA())
                .build();
    }

    /**
     * Converte una lista di AnagraficaDTO in una lista di AmministrazioneInternalDTO.
     */
    public static List<AmministrazioneInternalDTO> toInternalList(List<AnagraficaDTO> anagrafiche) {
        if (anagrafiche == null) {
            return Collections.emptyList();
        }
        return anagrafiche.stream()
                .map(AmministrazioneMapper::toInternal)
                .collect(Collectors.toList());
    }
}
