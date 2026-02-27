package it.ey.utils;

import it.ey.entity.StoricoStatoSezione;

import java.util.Comparator;
import java.util.List;

public class StoricoStatoSezioneUtils {






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
