package it.ey.piao.api.utilsPrivate;

import it.ey.dto.external.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class per la costruzione di PiaoExternalDTO e gestione dei dati esterni PIAO.
 */
@Component
public class PiaoExternalUtils {

    /**
     * Costruisce PiaoExternalDTO a partire dai risultati della query nativa.
     * Gestisce la denormalizzazione dei dati aggregando OVP, Strategie, Indicatori, Obiettivi e AmpiezzaOrganizzativa.
     */
    public PiaoExternalDTO buildPiaoExternalDTO(List<Object[]> results) {
        if (results == null || results.isEmpty()) {
            return null;
        }

        // Primo record per Anagrafica (tutti i record hanno gli stessi dati anagrafica)
        Object[] firstRow = results.getFirst();
        AnagraficaExternalDTO anagrafica = AnagraficaExternalDTO.builder()
            .denominazioneEnte(getString(firstRow, 0))
            .acronimoPA(getString(firstRow, 1))
            .codiceFiscale(getString(firstRow, 2))
            .codiceIPA(getString(firstRow, 3))
            .tipologiaPA(getString(firstRow, 4))
            .piva(getString(firstRow, 5))
            .indirizzoSedeLegale(getString(firstRow, 6))
            .indirizzoURP(getString(firstRow, 7))
            .www(getString(firstRow, 8))
            .mail(getString(firstRow, 9))
            .telefono(getString(firstRow, 10))
            .pec(getString(firstRow, 11))
            .nomeRPCT(getString(firstRow, 12))
            .cognomeRCTP(getString(firstRow, 13))
            .ruoloRPCT(getString(firstRow, 14))
            .nomeRTD(getString(firstRow, 16))
            .strutturaRifRTD(getString(firstRow, 17))
            .build();

        // Aggregazione OVP, Strategie, Indicatori, Obiettivi
        Map<Long, OvpExternalDTO> ovpMap = new LinkedHashMap<>();
        Map<Long, OvpStrategiaExternalDTO> strategiaMap = new LinkedHashMap<>();
        Map<Long, IndicatoreExternalDTO> indicatoreMap = new LinkedHashMap<>();
        Map<String, ObiettivoExternalDTO> obiettivoMap = new LinkedHashMap<>(); // Chiave: strategiaId_obiettivoId

        for (Object[] row : results) {
            // OVP
            Long ovpId = getLong(row, 17);
            if (ovpId != null && !ovpMap.containsKey(ovpId)) {
                ovpMap.put(ovpId, OvpExternalDTO.builder()
                    .id(ovpId)
                    .codice(getString(row, 18))
                    .denominazione(getString(row, 19))
                    .ovpStrategias(new ArrayList<>())
                    .build());
            }

            // OVPStrategia
            Long strategiaId = getLong(row, 20);
            if (strategiaId != null && !strategiaMap.containsKey(strategiaId)) {
                OvpStrategiaExternalDTO strategia = OvpStrategiaExternalDTO.builder()
                    .id(strategiaId)
                    .codStrategia(getString(row, 21))
                    .denominazioneStrategia(getString(row, 22))
                    .indicatori(new ArrayList<>())
                    .obbiettivoPerformance(new ArrayList<>())
                    .obbiettivoDiPienaAccessibilitaDigitale(new ArrayList<>())
                    .obbiettivoDiPienaAccessibilitaFisica(new ArrayList<>())
                    .obbiettivoDiSemplificazione(new ArrayList<>())
                    .obbiettivoDiPariOpportunita(new ArrayList<>())
                    .obbiettivoDiPerformanceOrganizzativa(new ArrayList<>())
                    .obbiettivoDiPerformanceIndividuale(new ArrayList<>())
                    .build();
                strategiaMap.put(strategiaId, strategia);

                // Aggiungi strategia all'OVP corretto
                if (ovpId != null && ovpMap.containsKey(ovpId)) {
                    ovpMap.get(ovpId).getOvpStrategias().add(strategia);
                }
            }

            // Indicatore
            Long indicatoreId = getLong(row, 23);
            if (indicatoreId != null && !indicatoreMap.containsKey(indicatoreId)) {
                IndicatoreExternalDTO indicatore = IndicatoreExternalDTO.builder()
                    .codTipologiaFK(getString(row, 24))
                    .denominazione(getString(row, 25))
                    .unitaMisura(getString(row, 26))
                    .peso(getLong(row, 27))
                    .polarita(getString(row, 28))
                    .baseLine(getLong(row, 29))
                    .fonteDati(getString(row, 30))
                    .build();
                indicatoreMap.put(indicatoreId, indicatore);

                // Aggiungi indicatore alla strategia corretta
                if (strategiaId != null && strategiaMap.containsKey(strategiaId)) {
                    strategiaMap.get(strategiaId).getIndicatori().add(indicatore);
                }
            }

            // Obiettivo Performance (diretto sulla strategia)
            Long obiettivoId = getLong(row, 31);
            if (obiettivoId != null && strategiaId != null) {
                String obiettivoKey = strategiaId + "_" + obiettivoId;
                if (!obiettivoMap.containsKey(obiettivoKey)) {
                    String tipologia = getString(row, 33);
                    ObiettivoExternalDTO obiettivo = ObiettivoExternalDTO.builder()
                        .id(obiettivoId)
                        .codice(getString(row, 32))
                        .tipologia(tipologia)
                        .denominazione(getString(row, 34))
                        .idObiettivoPeformance(getLong(row, 35))
                        .risorseEconomicaFinanziaria(getString(row, 36))
                        .risorseStrumentali(getString(row, 37))
                        .tipologiaRisorsa(getString(row, 38))
                        .build();
                    obiettivoMap.put(obiettivoKey, obiettivo);

                    // Aggiungi obiettivo alla strategia corretta in base alla tipologia
                    if (strategiaMap.containsKey(strategiaId) && tipologia != null) {
                        OvpStrategiaExternalDTO strat = strategiaMap.get(strategiaId);
                        addObiettivoByTipologia(strat, obiettivo, tipologia);
                    }
                }
            }
        }

        return PiaoExternalDTO.builder()
            .anagrafica(anagrafica)
            .ovp(new ArrayList<>(ovpMap.values()))
            .popolazioneSuddivisaEta(null)
            .build();
    }

    /**
     * Aggiunge un obiettivo alla lista corretta della strategia in base alla tipologia.
     */
    public void addObiettivoByTipologia(OvpStrategiaExternalDTO strategia,
                                         ObiettivoExternalDTO obiettivo,
                                         String tipologia) {
        switch (tipologia) {
            case "PERFORMANCE" -> strategia.getObbiettivoPerformance().add(obiettivo);
            case "ACCESSI_DIGITALE" -> strategia.getObbiettivoDiPienaAccessibilitaDigitale().add(obiettivo);
            case "ACCESSI_FISICI" -> strategia.getObbiettivoDiPienaAccessibilitaFisica().add(obiettivo);
            case "SEMPLIFICAZIONE" -> strategia.getObbiettivoDiSemplificazione().add(obiettivo);
            case "PARI_OPPORTUNITA" -> strategia.getObbiettivoDiPariOpportunita().add(obiettivo);
            case "PERFORMANCE_ORGANIZZATIVA" -> strategia.getObbiettivoDiPerformanceOrganizzativa().add(obiettivo);
            case "PERFORMANCE_INDIVIDUALE" -> strategia.getObbiettivoDiPerformanceIndividuale().add(obiettivo);
            default -> strategia.getObbiettivoPerformance().add(obiettivo);
        }
    }

    /**
     * Estrae una stringa da un array di Object in posizione specifica.
     */
    public String getString(Object[] row, int index) {
        if (row == null || index >= row.length || row[index] == null) {
            return null;
        }
        return row[index].toString();
    }

    /**
     * Estrae un Long da un array di Object in posizione specifica.
     */
    public Long getLong(Object[] row, int index) {
        if (row == null || index >= row.length || row[index] == null) {
            return null;
        }
        Object val = row[index];
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Estrae un LocalDate da un array di Object in posizione specifica.
     * Gestisce java.sql.Date, java.util.Date, LocalDate e stringhe ISO (yyyy-MM-dd).
     */
    public LocalDate getLocalDate(Object[] row, int index) {
        if (row == null || index >= row.length || row[index] == null) {
            return null;
        }
        Object val = row[index];
        if (val instanceof LocalDate ld) {
            return ld;
        }
        if (val instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (val instanceof java.util.Date utilDate) {
            return new java.sql.Date(utilDate.getTime()).toLocalDate();
        }
        try {
            return LocalDate.parse(val.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
