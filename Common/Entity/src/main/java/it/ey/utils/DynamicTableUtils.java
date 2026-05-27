package it.ey.utils;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.enums.TipoTabellaSezione331;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


public class DynamicTableUtils {


    public static final List<String> paths = List.of(
            "reportDotazioneOrganicaERimodulata.dotazioneOrganica",
            "reportDotazioneOrganicaERimodulata.dotazioneOrganicaRimodulata",
            "reportPersonale.tempoIndeterminatoInServizio",
            "reportPersonale.dirigentiTempoDeterminato",
            "reportPersonale.personaleComandatoOut",
            "reportPersonale.personaleInAspettativa",
            "reportPersonale.personaleComandatoIn",
            "reportPersonale.personaleDiRuolo",
            "reportCessazioni.cessazioniAnnoPrecedente",
            "reportCessazioni.cessazioniAnnoCorrente",
            "reportCessazioni.cessazioniAnno2",
            "reportCessazioni.cessazioniAnno3",
            "reportFacoltaAssunzionali.facoltaAssunzionale",
            "reportVacanzePosto.vacanzePosto",
            "reportEccedenze.eccedenze",
            "reportAssunzioniAnnoCorrente.autorizzazione",
            "reportAssunzioniAnno2.autorizzazione",
            "reportAssunzioniAnno3.autorizzazione",
            "reportAssunzioniAnnoCorrente.assunzioniTempoIndeterminato",
            "reportAssunzioniAnnoCorrente.assunzioniTempoIndeterminatoProfiliRuolo",
            "reportAssunzioniAnno2.assunzioniTempoIndeterminato",
            "reportAssunzioniAnno2.assunzioniTempoIndeterminatoProfiliRuolo",
            "reportAssunzioniAnno3.assunzioniTempoIndeterminato",
            "reportAssunzioniAnno3.assunzioniTempoIndeterminatoProfiliRuolo"
    );

    public static final List<TipoTabellaSezione331> nameTablePAC = List.of(
            TipoTabellaSezione331.VALORE_FINANZIARIO_DOTAZIONE_ORGANICA,
            TipoTabellaSezione331.CONSISTENZA_PERSONALE_DIRIGENZIALE,
            TipoTabellaSezione331.CONSISTENZA_PERSONALE_NON_DIRIGENZIALE,
            TipoTabellaSezione331.RIEPILOGO_ASSUNZIONI_DIRIGENZIALE,
            TipoTabellaSezione331.RIEPILOGO_ASSUNZIONI_AREE_CONTRATTUALI,
            TipoTabellaSezione331.RIEPILOGO_CESSAZIONI,
            TipoTabellaSezione331.DOTAZIONE_ORGANICA_RIMODULAZIONE,
            TipoTabellaSezione331.COPERTURA_FABBISOGNO_ANNO_CORRENTE_DIRIGENZIALE,
            TipoTabellaSezione331.COPERTURA_FABBISOGNO_ANNO_CORRENTE_AREE_CONTRATTUALI,
            TipoTabellaSezione331.CESSAZIONI_ANNO_CORRENTE,
            TipoTabellaSezione331.COPERTURA_FABBISOGNO_ANNO1_DIRIGENZIALE,
            TipoTabellaSezione331.COPERTURA_FABBISOGNO_ANNO1_AREE_CONTRATTUALI,
            TipoTabellaSezione331.CESSAZIONI_SERVIZIO,
            TipoTabellaSezione331.COPERTURA_FABBISOGNO_ANNO2_DIRIGENZIALE,
            TipoTabellaSezione331.COPERTURA_FABBISOGNO_ANNO2_AREE_CONTRATTUALI
    );
    public static final List<TipoTabellaSezione331> nameTablePAL = List.of(

    );
    public static final List<TipoTabellaSezione331> nameTableUNI = List.of(

    );

    public static final String DIRIGENTI_1_FASCIA = "DIRIGENTI DI 1^ FASCIA";
    public static final String DIRIGENTI_2_FASCIA = "DIRIGENTI DI 2^ FASCIA";
    public static final String TOTALE_COMPLESSIVO = "Totale complessivo";
    public static final String TOTALE_PRIMA_FASCIA_DIR = "Totale di cui Dirigenti I fascia";
    public static final String TOTALE_SECONDA_FASCIA_DIR = "Totale di cui Dirigenti II fascia + Aree";
    public static final String TOTALE_SECONDA_FASCIA_DIR_NO_AREE = "Totale di cui Dirigenti II fascia";
    public static final String TOTALE = "Totale";


    public static Map<String, Object> buildTableFromJson(JsonNode jsonArray) {
        if (!jsonArray.isArray()) {
            throw new IllegalArgumentException("Risposta inattesa: non è un array");
        }

        Set<String> columns = new LinkedHashSet<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        for (JsonNode node : jsonArray) {
            Map<String, Object> row = new LinkedHashMap<>();
            node.fieldNames().forEachRemaining(field -> {
                columns.add(field);
                row.put(field, parseValue(node.get(field)));
            });
            rows.add(row);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("columns", columns);
        result.put("rows", rows);
        return result;
    }

    public static Map<String, Object> createTablePACMinerva(TipoTabellaSezione331 nameTable, List<Map<String, Object>> tableObject) {
        Map<String, Object> result = new LinkedHashMap<>();

        var dotazioneOrganica = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reportDotazioneOrganicaERimodulata.dotazioneOrganica"))
                .findFirst().get();
        var personaleRuolo = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reportPersonale.personaleDiRuolo"))
                .findFirst().get();
        var personaleComandatoOut = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reportPersonale.personaleComandatoOut"))
                .findFirst().get();
        var personaleComandatoIn = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reportPersonale.personaleComandatoIn"))
                .findFirst().get();
        var dirigentiTempoDeterminato = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reportPersonale.dirigentiTempoDeterminato"))
                .findFirst().get();
        var assunzioni1 = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reportAssunzioniAnnoCorrente.autorizzazione"))
                .findFirst().get();
        var cessazioniAnnoPrecedente = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reportCessazioni.cessazioniAnnoPrecedente"))
                .findFirst().get();
        var dotazioneOrganicaRimodulata = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reportDotazioneOrganicaERimodulata.dotazioneOrganicaRimodulata"))
                .findFirst().get();
        var facoltaAssunzionale = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reportFacoltaAssunzionali.facoltaAssunzionale"))
                .findFirst().get();
        var cessazioniAnnoCorrente = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reportCessazioni.cessazioniAnnoCorrente"))
                .findFirst().get();
        var assunzioni2 = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reportAssunzioniAnno2.autorizzazione"))
                .findFirst().get();
        var cessazioniAnno2 = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reportCessazioni.cessazioniAnno2"))
                .findFirst().get();
        var assunzioni3 = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reportAssunzioniAnno3.autorizzazione"))
                .findFirst().get();

        result.put("name", nameTable.name());

        switch (nameTable) {

            case VALORE_FINANZIARIO_DOTAZIONE_ORGANICA:
                createDotazioneOrganicaAnnoPrecedente(result, dotazioneOrganica);
                break;

            case CONSISTENZA_PERSONALE_DIRIGENZIALE:
                createPersonaleDirigenzialeAnnoPrecendente(result, dotazioneOrganica, personaleRuolo, personaleComandatoOut, dirigentiTempoDeterminato);
                break;

            case CONSISTENZA_PERSONALE_NON_DIRIGENZIALE:
                createPersonaleNonDirigenzialeAnnoPrecendente(result, dotazioneOrganica, personaleRuolo, personaleComandatoOut, personaleComandatoIn);
                break;

            case RIEPILOGO_ASSUNZIONI_DIRIGENZIALE:
                createAssunzioniDirigenzialeAnnoPrecedente(result, dotazioneOrganica, assunzioni1);
                break;

            case RIEPILOGO_ASSUNZIONI_AREE_CONTRATTUALI:
                // logica per cessazioniAnnoPrecedente
                break;

            case RIEPILOGO_CESSAZIONI:
                createRiepilogoCessazioniAnnoPrecendente(result, dotazioneOrganica, cessazioniAnnoPrecedente);
                break;

            case DOTAZIONE_ORGANICA_RIMODULAZIONE:
                createDotazioneOrganicaRimodulata(result, dotazioneOrganicaRimodulata);
                break;

            case COPERTURA_FABBISOGNO_ANNO_CORRENTE_DIRIGENZIALE:
                createCoperturaFabbisognoAnnoCorrenteDirigenziale(result, dotazioneOrganica, assunzioni1, facoltaAssunzionale);
                break;

            case COPERTURA_FABBISOGNO_ANNO_CORRENTE_AREE_CONTRATTUALI:
                // logica per cessazioniAnnoCorrente
                break;

            case CESSAZIONI_ANNO_CORRENTE:
                createCessazioniAnnoCorrente(result, dotazioneOrganica, cessazioniAnnoCorrente);
                break;

            case COPERTURA_FABBISOGNO_ANNO1_DIRIGENZIALE:
                createCoperturaFabbisognoAnno1Dirigenziale(result, dotazioneOrganica, assunzioni2, facoltaAssunzionale);
                break;

            case COPERTURA_FABBISOGNO_ANNO1_AREE_CONTRATTUALI:
                // logica per cessazioniPrimoAnnoSuccessivoAnnoCorrente
                break;

            case CESSAZIONI_SERVIZIO:
                createCessazioniServizio(result, dotazioneOrganica, cessazioniAnno2);
                break;

            case COPERTURA_FABBISOGNO_ANNO2_DIRIGENZIALE:
                createCoperturaFabbisognoAnno2Dirigenziale(result, dotazioneOrganica, assunzioni3, facoltaAssunzionale);
                break;

            case COPERTURA_FABBISOGNO_ANNO2_AREE_CONTRATTUALI:
                // logica per fabbisognoAreeContrattualiSecondoAnnoSuccessivoAnnoCorrente
                break;

            default:
                // gestione caso non previsto
                break;
        }

        return result;
    }

    // =================================================================================
    // Builders tabelle PAC
    // =================================================================================

    private static void createRiepilogoCessazioniAnnoPrecendente(Map<String, Object> result,
                                                                 Map<String, Object> dotazioneOrganica,
                                                                 Map<String, Object> cessazioni) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("areaGiuridica", "Area contrattuale CCNL e qualifiche");
        columns.put("ula", "Totale unità cessate");
        columns.put("valoreEconomico", "Totale risorse da cessazione");
        result.put("columns", columns);

        List<Map<String, Object>> rows = castRows(dotazioneOrganica);
        if (rows.isEmpty()) return;

        List<Map<String, Object>> cessazioniRows = castRows(cessazioni);
        List<Map<String, Object>> rowsCustom = new ArrayList<>(rows.size() + 3);

        BigDecimal totUlaDir1 = BigDecimal.ZERO;
        BigDecimal totValDir1 = BigDecimal.ZERO;
        BigDecimal totUlaDir2 = BigDecimal.ZERO;
        BigDecimal totValDir2 = BigDecimal.ZERO;
        BigDecimal totUlaNonDir = BigDecimal.ZERO;
        BigDecimal totValNonDir = BigDecimal.ZERO;

        // Per ogni area di dotazioneOrganica recupero i corrispettivi valori dal report cessazioni.
        // Tutte le righe (Dirigenti 1^/2^ fascia incluse) vengono mostrate; i dirigenti
        // confluiscono inoltre nei rispettivi totali "di cui ...".
        for (Map<String, Object> row : rows) {
            String area = String.valueOf(row.get("areaGiuridica"));
            BigDecimal ula = lookupSumByArea(cessazioniRows, area, "ula");
            BigDecimal val = lookupSumByArea(cessazioniRows, area, "valoreEconomicoCessazioni");

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("areaGiuridica", area);
            rowObj.put("ula", ula);
            rowObj.put("valoreEconomico", val);
            rowsCustom.add(rowObj);

            if (DIRIGENTI_1_FASCIA.equals(area)) {
                totUlaDir1 = totUlaDir1.add(ula);
                totValDir1 = totValDir1.add(val);
            } else if (DIRIGENTI_2_FASCIA.equals(area)) {
                totUlaDir2 = totUlaDir2.add(ula);
                totValDir2 = totValDir2.add(val);
            } else {
                totUlaNonDir = totUlaNonDir.add(ula);
                totValNonDir = totValNonDir.add(val);
            }
        }

        rowsCustom.add(buildCessazioneTotaleRow(TOTALE_PRIMA_FASCIA_DIR, totUlaDir1, totValDir1));
        rowsCustom.add(buildCessazioneTotaleRow(TOTALE_SECONDA_FASCIA_DIR, totUlaDir2, totValDir2));

        BigDecimal totUlaComplessivo = totUlaDir1.add(totUlaDir2).add(totUlaNonDir);
        BigDecimal totValComplessivo = totValDir1.add(totValDir2).add(totValNonDir);
        rowsCustom.add(buildCessazioneTotaleRow(TOTALE_COMPLESSIVO, totUlaComplessivo, totValComplessivo));

        result.put("rows", rowsCustom);
    }

    /**
     * Riepilogo cessazioni anno corrente: stessa struttura/logica di
     * {@link #createRiepilogoCessazioniAnnoPrecendente(Map, Map, Map)} ma
     * sui dati di {@code reportCessazioni.cessazioniAnnoCorrente}.
     */
    private static void createCessazioniAnnoCorrente(Map<String, Object> result,
                                                     Map<String, Object> dotazioneOrganica,
                                                     Map<String, Object> cessazioniAnnoCorrente) {
        createRiepilogoCessazioniAnnoPrecendente(result, dotazioneOrganica, cessazioniAnnoCorrente);
    }

    /**
     * Cessazioni servizio (secondo anno successivo): stessa struttura/logica di
     * {@link #createRiepilogoCessazioniAnnoPrecendente(Map, Map, Map)} ma
     * sui dati di {@code reportCessazioni.cessazioniAnno2}.
     */
    private static void createCessazioniServizio(Map<String, Object> result,
                                                 Map<String, Object> dotazioneOrganica,
                                                 Map<String, Object> cessazioniAnno2) {
        createRiepilogoCessazioniAnnoPrecendente(result, dotazioneOrganica, cessazioniAnno2);
    }

    private static void createAssunzioniDirigenzialeAnnoPrecedente(Map<String, Object> result,
                                                                   Map<String, Object> dotazioneOrganica,
                                                                   Map<String, Object> assunzioni1) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("qualifiche", "Qualifiche");
        columns.put("tipologia", "Tipologia di reclutamento");
        columns.put("fonte", "Fonte di finaziamento");
        columns.put("totaleUnita", "Totale unità");
        result.put("columns", columns);

        List<Map<String, Object>> rowsDirigenti = filterDirigenti(castRows(dotazioneOrganica));
        if (rowsDirigenti.isEmpty()) return;

        List<Map<String, Object>> assunzioni = castRows(assunzioni1);
        List<Map<String, Object>> rowsCustom = new ArrayList<>(rowsDirigenti.size() + 2);

        BigDecimal totaleUnita1 = BigDecimal.ZERO;
        BigDecimal totaleUnita2 = BigDecimal.ZERO;

        for (Map<String, Object> row : rowsDirigenti) {
            String area = String.valueOf(row.get("areaGiuridica"));
            boolean isDir1 = DIRIGENTI_1_FASCIA.equals(area);
            String dirKey = isDir1 ? "dirigenti1" : "dirigenti2";

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("qualifiche", area);

            for (Map<String, Object> assunzione : assunzioni) {
                int dirInt = toIntSafe(assunzione.get(dirKey));
                rowObj.put("tipologia", assunzione.get("autorizzazione"));
                rowObj.put("fonte", "todo"); //TODO da capire se la fonte è questo campo
                rowObj.put("totaleUnita", dirInt);
                BigDecimal delta = BigDecimal.valueOf(dirInt);
                if (isDir1) totaleUnita1 = totaleUnita1.add(delta);
                else totaleUnita2 = totaleUnita2.add(delta);
            }
            rowsCustom.add(rowObj);
        }

        rowsCustom.add(buildAssunzioneTotaleRow(TOTALE_PRIMA_FASCIA_DIR, totaleUnita1));
        rowsCustom.add(buildAssunzioneTotaleRow(TOTALE_SECONDA_FASCIA_DIR_NO_AREE, totaleUnita2));
        result.put("rows", rowsCustom);
    }

    private static void createCoperturaFabbisognoAnnoCorrenteDirigenziale(Map<String, Object> result,
                                                                          Map<String, Object> dotazioneOrganica,
                                                                          Map<String, Object> assunzioni1,
                                                                          Map<String, Object> facoltaAssunzionale) {
        buildCoperturaFabbisognoDirigenziale(result, dotazioneOrganica, assunzioni1, facoltaAssunzionale, "anno1");
    }

    /**
     * Copertura fabbisogno primo anno successivo (dirigenziale): stessa struttura di
     * {@link #createCoperturaFabbisognoAnnoCorrenteDirigenziale} ma sui dati di
     * {@code reportAssunzioniAnno2.autorizzazione} e oneri da {@code facoltaAssunzionale.anno2}.
     */
    private static void createCoperturaFabbisognoAnno1Dirigenziale(Map<String, Object> result,
                                                                   Map<String, Object> dotazioneOrganica,
                                                                   Map<String, Object> assunzioni2,
                                                                   Map<String, Object> facoltaAssunzionale) {
        buildCoperturaFabbisognoDirigenziale(result, dotazioneOrganica, assunzioni2, facoltaAssunzionale, "anno2");
    }

    /**
     * Copertura fabbisogno secondo anno successivo (dirigenziale): stessa struttura di
     * {@link #createCoperturaFabbisognoAnnoCorrenteDirigenziale} ma sui dati di
     * {@code reportAssunzioniAnno3.autorizzazione} e oneri da {@code facoltaAssunzionale.anno3}.
     */
    private static void createCoperturaFabbisognoAnno2Dirigenziale(Map<String, Object> result,
                                                                   Map<String, Object> dotazioneOrganica,
                                                                   Map<String, Object> assunzioni3,
                                                                   Map<String, Object> facoltaAssunzionale) {
        buildCoperturaFabbisognoDirigenziale(result, dotazioneOrganica, assunzioni3, facoltaAssunzionale, "anno3");
    }

    /**
     * Logica comune per le tabelle di copertura fabbisogno dirigenziali:
     * mostra le righe Dirigenti 1^/2^ fascia con tipologia/fonte/totaleUnita prelevate da
     * {@code autorizzazione}, oneri prelevati da {@code facoltaAssunzionale.<annoField>}.
     */
    private static void buildCoperturaFabbisognoDirigenziale(Map<String, Object> result,
                                                             Map<String, Object> dotazioneOrganica,
                                                             Map<String, Object> autorizzazione,
                                                             Map<String, Object> facoltaAssunzionale,
                                                             String annoField) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("qualifiche", "Qualifiche");
        columns.put("tipologia", "Tipologia di reclutamento");
        columns.put("fonte", "Fonte di finanziamento");
        columns.put("totaleUnita", "Totale Unità");
        columns.put("totaleOneri", "Totale oneri assunzionali");
        result.put("columns", columns);

        List<Map<String, Object>> rowsDirigenti = filterDirigenti(castRows(dotazioneOrganica));
        if (rowsDirigenti.isEmpty()) return;

        List<Map<String, Object>> assunzioni = castRows(autorizzazione);
        List<Map<String, Object>> facoltaRows = castRows(facoltaAssunzionale);

        BigDecimal oneriDir1 = lookupFacoltaAnno(facoltaRows, false, annoField);
        BigDecimal oneriDir2 = lookupFacoltaAnno(facoltaRows, true, annoField);

        List<Map<String, Object>> rowsCustom = new ArrayList<>(rowsDirigenti.size() + 2);

        BigDecimal totaleUnita1 = BigDecimal.ZERO;
        BigDecimal totaleUnita2 = BigDecimal.ZERO;

        for (Map<String, Object> row : rowsDirigenti) {
            String area = String.valueOf(row.get("areaGiuridica"));
            boolean isDir1 = DIRIGENTI_1_FASCIA.equals(area);
            String dirKey = isDir1 ? "dirigenti1" : "dirigenti2";

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("qualifiche", area);

            for (Map<String, Object> assunzione : assunzioni) {
                int dirInt = toIntSafe(assunzione.get(dirKey));
                rowObj.put("tipologia", assunzione.get("autorizzazione"));
                rowObj.put("fonte", "todo");
                rowObj.put("totaleUnita", dirInt);
                rowObj.put("totaleOneri", scale2(isDir1 ? oneriDir1 : oneriDir2));
                BigDecimal delta = BigDecimal.valueOf(dirInt);
                if (isDir1) totaleUnita1 = totaleUnita1.add(delta);
                else totaleUnita2 = totaleUnita2.add(delta);
            }
            rowsCustom.add(rowObj);
        }

        rowsCustom.add(buildCoperturaDirigenzialeRow(TOTALE_PRIMA_FASCIA_DIR, totaleUnita1, oneriDir1));
        rowsCustom.add(buildCoperturaDirigenzialeRow(TOTALE_SECONDA_FASCIA_DIR, totaleUnita2, oneriDir2));
        result.put("rows", rowsCustom);
    }

    private static void createPersonaleNonDirigenzialeAnnoPrecendente(Map<String, Object> result,
                                                                      Map<String, Object> dotazioneOrganica,
                                                                      Map<String, Object> personaleRuolo,
                                                                      Map<String, Object> personaleComandatoOut,
                                                                      Map<String, Object> personaleComandatoIn) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("qualifiche", "Area contrattuale CCNL");
        columns.put("postiDotazione", "Posti in Dotazione Organica");
        columns.put("personaleRuolo", "Personale di ruolo al 31/12/ anno t-1");
        columns.put("comandatiOut", "Comandati OUT al 31/12/ anno t-1");
        columns.put("comandatiIn", "Comandati IN al 31/12/anno t-1");
        columns.put("totaleUnita", "Totale unità");
        result.put("columns", columns);

        List<Map<String, Object>> rows = castRows(dotazioneOrganica);
        if (rows.isEmpty()) return;

        List<Map<String, Object>> prRows = castRows(personaleRuolo);
        List<Map<String, Object>> coRows = castRows(personaleComandatoOut);
        List<Map<String, Object>> ciRows = castRows(personaleComandatoIn);

        List<Map<String, Object>> rowsCustom = new ArrayList<>(rows.size() + 1);
        BigDecimal totPosti = BigDecimal.ZERO;
        BigDecimal totPr = BigDecimal.ZERO;
        BigDecimal totCo = BigDecimal.ZERO;
        BigDecimal totCi = BigDecimal.ZERO;
        BigDecimal totUnita = BigDecimal.ZERO;

        for (Map<String, Object> row : rows) {
            String area = String.valueOf(row.get("areaGiuridica"));
            if (isDirigenteArea(area)) continue;

            BigDecimal posti = toBigDecimalSafe(row.get("ula"));
            BigDecimal pr = lookupUlaByArea(prRows, area, true);
            BigDecimal co = lookupUlaByArea(coRows, area, true);
            BigDecimal ci = lookupUlaByArea(ciRows, area, true);
            BigDecimal totale = posti.add(pr).add(co).add(ci);

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("qualifiche", area);
            rowObj.put("postiDotazione", posti);
            rowObj.put("personaleRuolo", pr);
            rowObj.put("comandatiOut", co);
            rowObj.put("comandatiIn", ci);
            rowObj.put("totaleUnita", totale);
            rowsCustom.add(rowObj);

            totPosti = totPosti.add(posti);
            totPr = totPr.add(pr);
            totCo = totCo.add(co);
            totCi = totCi.add(ci);
            totUnita = totUnita.add(totale);
        }

        Map<String, Object> totaleRow = new LinkedHashMap<>();
        totaleRow.put("qualifiche", TOTALE_COMPLESSIVO);
        totaleRow.put("postiDotazione", scale2(totPosti));
        totaleRow.put("personaleRuolo", scale2(totPr));
        totaleRow.put("comandatiOut", scale2(totCo));
        totaleRow.put("comandatiIn", scale2(totCi));
        totaleRow.put("totaleUnita", scale2(totUnita));
        rowsCustom.add(totaleRow);

        result.put("rows", rowsCustom);
    }

    private static void createPersonaleDirigenzialeAnnoPrecendente(Map<String, Object> result,
                                                                   Map<String, Object> dotazioneOrganica,
                                                                   Map<String, Object> personaleRuolo,
                                                                   Map<String, Object> personaleComandatoOut,
                                                                   Map<String, Object> dirigentiTempoDeterminato) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("qualifiche", "Qualifiche");
        columns.put("postiDotazione", "Posti in Dotazione Organica");
        columns.put("personaleRuolo", "Personale di ruolo al 31/12/ anno t-1");
        columns.put("comandatiOut", "Comandati OUT al 31/12/ anno t-1");
        columns.put("totaleUnita5", "Totale unità art. 19 comma 5-bis");
        columns.put("totaleUnita6", "Totale unità art. 19 comma 6");
        columns.put("totaleUnita", "Totale unità");
        result.put("columns", columns);

        List<Map<String, Object>> rowsDirigenti = filterDirigenti(castRows(dotazioneOrganica));
        if (rowsDirigenti.isEmpty()) return;

        List<Map<String, Object>> prRows = castRows(personaleRuolo);
        List<Map<String, Object>> coRows = castRows(personaleComandatoOut);
        List<Map<String, Object>> tdRows = castRows(dirigentiTempoDeterminato);

        List<Map<String, Object>> rowsCustom = new ArrayList<>(rowsDirigenti.size() + 3);
        Map<String, Object> totaleDir1 = null;
        Map<String, Object> totaleDir2 = null;

        for (Map<String, Object> row : rowsDirigenti) {
            String area = String.valueOf(row.get("areaGiuridica"));
            BigDecimal posti = toBigDecimalSafe(row.get("ula"));
            BigDecimal pr = lookupUlaByArea(prRows, area, true);
            BigDecimal co = lookupUlaByArea(coRows, area, true);
            BigDecimal u5 = lookupUlaByArea(tdRows, area, true);
            BigDecimal u6 = lookupUla2ByArea(tdRows, area, true);

            Map<String, Object> dirRow = buildDirRow(area, posti, pr, co, u5, u6);
            rowsCustom.add(dirRow);

            if (DIRIGENTI_1_FASCIA.equals(area)) {
                totaleDir1 = buildDirRow(TOTALE_PRIMA_FASCIA_DIR, posti, pr, co, u5, u6);
            } else if (DIRIGENTI_2_FASCIA.equals(area)) {
                totaleDir2 = buildDirRow(TOTALE_SECONDA_FASCIA_DIR, posti, pr, co, u5, u6);
            }
        }

        if (totaleDir1 == null) totaleDir1 = buildDirRow(TOTALE_PRIMA_FASCIA_DIR,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        if (totaleDir2 == null) totaleDir2 = buildDirRow(TOTALE_SECONDA_FASCIA_DIR,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        rowsCustom.add(totaleDir1);
        rowsCustom.add(totaleDir2);
        rowsCustom.add(sumDirRows(TOTALE_COMPLESSIVO, totaleDir1, totaleDir2));

        result.put("rows", rowsCustom);
    }

    private static void createDotazioneOrganicaAnnoPrecedente(Map<String, Object> result,
                                                              Map<String, Object> dotazioneOrganica) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("areaGiuridica", "Area contrattuale CCNL e qualifiche");
        columns.put("ula", "Totale unità in D.O.");
        columns.put("valoreFinanziario", "Valore finanziario della D.O.");
        result.put("columns", columns);

        List<Map<String, Object>> rows = castRows(dotazioneOrganica);
        if (rows.isEmpty()) return;

        // NB: si conserva la logica originale: il totale viene calcolato solo
        // sulle righe Dirigenti 1^/2^ fascia, mentre la lista mostra tutte le righe.
        List<Map<String, Object>> rowsDirigenti = filterDirigenti(rows);

        BigDecimal totaleUla = BigDecimal.ZERO;
        BigDecimal totaleValoreFinanziario = BigDecimal.ZERO;
        for (Map<String, Object> row : rowsDirigenti) {
            totaleUla = totaleUla.add(toBigDecimalSafe(row.get("ula")));
            totaleValoreFinanziario = totaleValoreFinanziario.add(toBigDecimalSafe(row.get("valoreFinanziario")));
        }

        List<Map<String, Object>> rowsCustom = new ArrayList<>(rowsDirigenti);
        Map<String, Object> totaleRow = new LinkedHashMap<>();
        totaleRow.put("areaGiuridica", TOTALE_COMPLESSIVO);
        totaleRow.put("ula", scale2(totaleUla));
        totaleRow.put("valoreFinanziario", scale2(totaleValoreFinanziario));
        rowsCustom.add(totaleRow);

        result.put("rows", rowsCustom);
    }

    private static void createDotazioneOrganicaRimodulata(Map<String, Object> result,
                                                          Map<String, Object> dotazioneOrganicaRimodulata) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("areaGiuridica", "Area contrattuale CCNL e qualifiche");
        columns.put("ula", "Totale unità in D.O.");
        columns.put("valoreFinanziario", "Valore finanziario della D.O.");
        result.put("columns", columns);

        List<Map<String, Object>> rows = castRows(dotazioneOrganicaRimodulata);
        if (rows.isEmpty()) return;

        // Mostra solo le righe Dirigenti 1^/2^ fascia, poi i totali "di cui ..." e il totale complessivo.
        List<Map<String, Object>> rowsDirigenti = filterDirigenti(rows);
        List<Map<String, Object>> rowsCustom = new ArrayList<>(rowsDirigenti.size() + 3);

        BigDecimal totUlaDir1 = BigDecimal.ZERO;
        BigDecimal totValDir1 = BigDecimal.ZERO;
        BigDecimal totUlaDir2 = BigDecimal.ZERO;
        BigDecimal totValDir2 = BigDecimal.ZERO;

        for (Map<String, Object> row : rowsDirigenti) {
            String area = String.valueOf(row.get("areaGiuridica"));
            BigDecimal ula = toBigDecimalSafe(row.get("ula"));
            BigDecimal val = toBigDecimalSafe(row.get("valoreFinanziario"));

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("areaGiuridica", area);
            rowObj.put("ula", ula);
            rowObj.put("valoreFinanziario", val);
            rowsCustom.add(rowObj);

            if (DIRIGENTI_1_FASCIA.equals(area)) {
                totUlaDir1 = totUlaDir1.add(ula);
                totValDir1 = totValDir1.add(val);
            } else if (DIRIGENTI_2_FASCIA.equals(area)) {
                totUlaDir2 = totUlaDir2.add(ula);
                totValDir2 = totValDir2.add(val);
            }
        }

        rowsCustom.add(buildDotazioneTotaleRow(TOTALE_PRIMA_FASCIA_DIR, totUlaDir1, totValDir1));
        rowsCustom.add(buildDotazioneTotaleRow(TOTALE_SECONDA_FASCIA_DIR, totUlaDir2, totValDir2));
        rowsCustom.add(buildDotazioneTotaleRow(TOTALE_COMPLESSIVO,
                totUlaDir1.add(totUlaDir2),
                totValDir1.add(totValDir2)));

        result.put("rows", rowsCustom);
    }

    // =================================================================================
    // Helpers row-builder
    // =================================================================================

    private static Map<String, Object> buildDirRow(String qualifica, BigDecimal posti, BigDecimal pr,
                                                   BigDecimal co, BigDecimal u5, BigDecimal u6) {
        BigDecimal nzPosti = nz(posti);
        BigDecimal nzPr = nz(pr);
        BigDecimal nzCo = nz(co);
        BigDecimal nzU5 = nz(u5);
        BigDecimal nzU6 = nz(u6);
        BigDecimal totale = nzPosti.add(nzPr).add(nzCo).add(nzU5).add(nzU6);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("qualifiche", qualifica);
        row.put("postiDotazione", nzPosti);
        row.put("personaleRuolo", nzPr);
        row.put("comandatiOut", nzCo);
        row.put("totaleUnita5", nzU5);
        row.put("totaleUnita6", nzU6);
        row.put("totaleUnita", totale);
        return row;
    }

    private static Map<String, Object> sumDirRows(String label, Map<String, Object> a, Map<String, Object> b) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("qualifiche", label);
        for (String key : List.of("postiDotazione", "personaleRuolo", "comandatiOut", "totaleUnita5", "totaleUnita6", "totaleUnita")) {
            BigDecimal sum = toBigDecimalSafe(a.get(key)).add(toBigDecimalSafe(b.get(key)));
            out.put(key, scale2(sum));
        }
        return out;
    }

    private static Map<String, Object> buildAssunzioneTotaleRow(String label, BigDecimal totale) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("qualifiche", label);
        row.put("tipologia", "");
        row.put("fonte", "");
        row.put("totaleUnita", scale2(totale));
        return row;
    }

    private static Map<String, Object> buildCoperturaDirigenzialeRow(String label, BigDecimal totaleUnita, BigDecimal oneri) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("qualifiche", label);
        row.put("tipologia", "");
        row.put("fonte", "");
        row.put("totaleUnita", scale2(totaleUnita));
        row.put("totaleOneri", scale2(oneri));
        return row;
    }

    /**
     * Recupera il valore del campo {@code field} (es. {@code anno1}, {@code anno2})
     * dal report facoltaAssunzionale per la riga relativa alla I o II fascia.
     * La riga "II Fascia" viene riconosciuta tramite {@code contains("II Fascia")}
     * nel campo {@code areaGiuridica}.
     */
    private static BigDecimal lookupFacoltaAnno(List<Map<String, Object>> rows, boolean isDir2, String field) {
        if (rows == null || rows.isEmpty()) return BigDecimal.ZERO;
        for (Map<String, Object> row : rows) {
            String area = String.valueOf(row.get("areaGiuridica"));
            boolean rowIsDir2 = area.contains("II Fascia");
            if (rowIsDir2 == isDir2) {
                return toBigDecimalSafe(row.get(field));
            }
        }
        return BigDecimal.ZERO;
    }

    private static Map<String, Object> buildCessazioneTotaleRow(String label, BigDecimal ula, BigDecimal valore) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("areaGiuridica", label);
        row.put("ula", scale2(ula));
        row.put("valoreEconomico", scale2(valore));
        return row;
    }

    private static Map<String, Object> buildDotazioneTotaleRow(String label, BigDecimal ula, BigDecimal valoreFinanziario) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("areaGiuridica", label);
        row.put("ula", scale2(ula));
        row.put("valoreFinanziario", scale2(valoreFinanziario));
        return row;
    }

    /**
     * Somma i valori del campo {@code field} sulle righe con la data {@code areaGiuridica}.
     * Ritorna {@link BigDecimal#ZERO} se nessuna riga corrisponde o {@code rows} è vuoto.
     */
    private static BigDecimal lookupSumByArea(List<Map<String, Object>> rows, String areaGiuridica, String field) {
        if (rows == null || rows.isEmpty()) return BigDecimal.ZERO;
        BigDecimal result = BigDecimal.ZERO;
        for (Map<String, Object> row : rows) {
            if (areaGiuridica.equals(row.get("areaGiuridica"))) {
                result = result.add(toBigDecimalSafe(row.get(field)));
            }
        }
        return result;
    }

    private static List<Map<String, Object>> filterDirigenti(List<Map<String, Object>> rows) {
        return rows.stream()
                .filter(r -> isDirigenteArea(String.valueOf(r.get("areaGiuridica"))))
                .toList();
    }

    private static boolean isDirigenteArea(String area) {
        return DIRIGENTI_1_FASCIA.equals(area) || DIRIGENTI_2_FASCIA.equals(area);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castRows(Map<String, Object> holder) {
        if (holder == null) return Collections.emptyList();
        Object rows = holder.get("rows");
        return (rows instanceof List) ? (List<Map<String, Object>>) rows : Collections.emptyList();
    }

    private static BigDecimal toBigDecimalSafe(Object n) {
        if (n instanceof BigDecimal) return (BigDecimal) n;
        if (n instanceof Number) return new BigDecimal(n.toString());
        return BigDecimal.ZERO;
    }

    private static int toIntSafe(Object n) {
        return (n instanceof Number) ? ((Number) n).intValue() : 0;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal scale2(BigDecimal v) {
        return nz(v).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Pre-calcola la somma degli 'ula' per le righe con la data 'areaGiuridica'.
     * Ritorna BigDecimal.ZERO se nessuna riga corrisponde.
     */
    private static BigDecimal lookupUlaByArea(List<Map<String, Object>> rows, String areaGiuridica, boolean isEqual) {
        if (rows == null || rows.isEmpty()) return BigDecimal.ZERO;
        BigDecimal result = null;
        for (Map<String, Object> row : rows) {
            if(isEqual){
                if (areaGiuridica.equals(row.get("areaGiuridica"))) {
                    Object ula = row.get("ula");
                    if (ula instanceof Number) {
                        BigDecimal val = toBigDecimal(ula);
                        result = (result == null) ? val : result.add(val);
                    }
                }
            }else{
                if (!areaGiuridica.equals(row.get("areaGiuridica"))) {
                    Object ula = row.get("ula");
                    if (ula instanceof Number) {
                        BigDecimal val = toBigDecimal(ula);
                        result = (result == null) ? val : result.add(val);
                    }
                }
            }

        }
        return result;
    }

    /**
     * Pre-calcola la somma degli 'ula' per le righe con la data 'areaGiuridica'.
     * Ritorna BigDecimal.ZERO se nessuna riga corrisponde.
     */
    private static BigDecimal lookupUlaByAreas(List<Map<String, Object>> rows, List<String> areaGiuridica, boolean isEqual) {
        if (rows == null || rows.isEmpty()) return BigDecimal.ZERO;
        BigDecimal result = null;
        for (Map<String, Object> row : rows) {
            if(isEqual){
                if (areaGiuridica.contains(row.get("areaGiuridica"))) {
                    Object ula = row.get("ula");
                    if (ula instanceof Number) {
                        BigDecimal val = toBigDecimal(ula);
                        result = (result == null) ? val : result.add(val);
                    }
                }
            }else{
                if (!areaGiuridica.contains(row.get("areaGiuridica"))) {
                    Object ula = row.get("ula");
                    if (ula instanceof Number) {
                        BigDecimal val = toBigDecimal(ula);
                        result = (result == null) ? val : result.add(val);
                    }
                }
            }

        }
        return result;
    }

    /**
     * Pre-calcola la somma degli 'ula2' per le righe con la data 'areaGiuridica'.
     * Ritorna BigDecimal.ZERO se nessuna riga corrisponde.
     */
    private static BigDecimal lookupUla2ByArea(List<Map<String, Object>> rows, String areaGiuridica, boolean isEqual) {
        if (rows == null || rows.isEmpty()) return BigDecimal.ZERO;
        BigDecimal result = null;
        for (Map<String, Object> row : rows) {
            if(isEqual){
                if (areaGiuridica.equals(row.get("areaGiuridica"))) {
                    Object ula = row.get("ula2");
                    if (ula instanceof Number) {
                        BigDecimal val = toBigDecimal(ula);
                        result = (result == null) ? val : result.add(val);
                    }
                }
            }else{
                if (!areaGiuridica.equals(row.get("areaGiuridica"))) {
                    Object ula = row.get("ula2");
                    if (ula instanceof Number) {
                        BigDecimal val = toBigDecimal(ula);
                        result = (result == null) ? val : result.add(val);
                    }
                }
            }

        }
        return result;
    }

    private static BigDecimal toBigDecimal(Object numObj) {
        if (numObj instanceof BigDecimal) return (BigDecimal) numObj;
        return new BigDecimal(numObj.toString());
    }

    public static Map<String, Object> extractTableFromMinerva(JsonNode root, String path, ObjectMapper objectMapper) {
        String[] parts = path.split("\\.");
        JsonNode current = root;

        // Navigazione dinamica del path
        for (String p : parts) {
            current = current.path(p);
            if (current.isMissingNode()) {
                // Non trovato → ritorna tabella vuota
                Map<String, Object> empty = new LinkedHashMap<>();
                empty.put("name", path);
                empty.put("columns", List.of());
                empty.put("rows", List.of());
                return empty;
            }
        }

        JsonNode nomeColonne = current.path("nomeColonne");
        JsonNode dati = current.path("dati");

        // columns (anche se manca la struttura)
        Map<String, Object> columns = objectMapper.convertValue(
                nomeColonne.isObject() ? nomeColonne : objectMapper.createObjectNode(),
                new TypeReference<Map<String, Object>>() {
                }
        );

        // rows (anche se dati è vuoto)
        List<Map<String, Object>> rows = new ArrayList<>();
        if (dati.isArray()) {
            Map<String, Object> rowsMap = buildTableFromJson(dati);
            rows = (List<Map<String, Object>>) rowsMap.get("rows");
        }

        // Costruzione risultato
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", path);
        result.put("columns", columns);
        result.put("rows", rows);

        return result;
    }

    private static Object parseValue(JsonNode valueNode) {

        if (valueNode.isNumber()) {
            Number num = valueNode.numberValue();

            // int esatto
            if (num instanceof Integer) {
                return num.intValue();
            }

            // long esatto
            if (num instanceof Long) {
                return num.longValue();
            }

            // double o bigdecimal → formatto a 2 decimali
            double doubleValue = num.doubleValue();

            //Se non ha decimali (es. 100.0) lo restituisco come long
            if (doubleValue == Math.rint(doubleValue)) {
                long asLong = (long) doubleValue;
                return asLong;
            }

            //Ha decimali → arrotondo a 2 decimali
            // double o bigdecimal → formatto a 2 decimali
            BigDecimal bd = new BigDecimal(num.toString());
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            return bd;
        }

        if (valueNode.isBoolean()) {
            return valueNode.booleanValue();
        }

        if (valueNode.isTextual()) {
            String text = valueNode.asText();

            if (text.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(text);
            }

            if (text.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(Z)?")) {
                return LocalDateTime.parse(text.replace("Z", ""));
            }

            return text;
        }

        return valueNode.toString();
    }
}
