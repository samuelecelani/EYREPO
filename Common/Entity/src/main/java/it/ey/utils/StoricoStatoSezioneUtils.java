package it.ey.utils;

import it.ey.entity.StoricoStatoSezione;

import java.util.Comparator;
import java.util.List;

public class StoricoStatoSezioneUtils {


    /**
     * Verifica se lo stato corrente (l'ultimo per createdTs/id) della lista
     * corrisponde allo stato atteso passato in input.
     *
     * @param stati       lista di storici della sezione
     * @param statoAtteso la descrizione dello stato atteso (es. "Validata")
     * @return true se lo stato corrente è uguale a statoAtteso, false altrimenti
     */
    public static boolean isStatoCorrente(List<StoricoStatoSezione> stati, String statoAtteso) {
        if (statoAtteso == null) return false;
        String statoCorrente = getStato(stati);
        return statoAtteso.equalsIgnoreCase(statoCorrente);
    }


    public static String getStato(List<StoricoStatoSezione> stati) {
        if (stati == null || stati.isEmpty()) return "";

        return stati.stream()
                .filter(s -> s != null
                        && s.getStatoSezione() != null
                        && s.getStatoSezione().getTesto() != null)
                .max(
                        Comparator
                                .comparing(StoricoStatoSezione::getCreatedTs,
                                        Comparator.nullsLast(Comparator.naturalOrder()))
                                .thenComparing(StoricoStatoSezione::getId,
                                        Comparator.nullsLast(Comparator.naturalOrder()))
                )
                .map(s -> s.getStatoSezione().getTesto())
                .orElse("");
    }


}
