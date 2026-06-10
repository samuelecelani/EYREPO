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
            "reportAssunzioniAnno3.assunzioniTempoIndeterminatoProfiliRuolo",
            "reportAssunzioniAnnoCorrente.oneriFinanziari",
            "reportAssunzioniAnno2.oneriFinanziari",
            "reportAssunzioniAnno3.oneriFinanziari",
            "reclutamentoAutorizzatoAnnoPrecedente",
            "reclutamentoAutorizzatoAnnoCorrente",
            "reclutamentoAutorizzatoAnno2",
            "reclutamentoAutorizzatoAnno3",
            // PAL (ReportEELL): paths sorgente per le tabelle di reclutamento/copertura fabbisogno.
            // NB: gli alias *Autorizzato* sopra sono PAC-only e non esistono nel JSON PAL.
            "reclutamentoAnnoCorrente",
            "reclutamentoAnno2",
            "reclutamentoAnno3",
            "reclutamentoObbligatorioAnnoCorrente"
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
            TipoTabellaSezione331.VALORE_FINANZIARIO_DOTAZIONE_ORGANICA,
            TipoTabellaSezione331.CONSISTENZA_PERSONALE_DIRIGENZIALE,
            TipoTabellaSezione331.CONSISTENZA_PERSONALE_NON_DIRIGENZIALE,
            TipoTabellaSezione331.CONSISTENZA_PERSONALE_DIRIGENZIALE_TEMPO_DETERMINATO,
            TipoTabellaSezione331.CONSISTENZA_PERSONALE_NON_DIRIGENZIALE_TEMPO_DETERMINATO,
            TipoTabellaSezione331.RIEPILOGO_ASSUNZIONI_DIRIGENZIALE,
            TipoTabellaSezione331.RIEPILOGO_ASSUNZIONI_AREE_CONTRATTUALI,
            TipoTabellaSezione331.RIEPILOGO_CESSAZIONI,
            TipoTabellaSezione331.RENDICONTI_ENTRATE_ULTIMI_3_ANNI,
            TipoTabellaSezione331.SPESE_PERSONALE_ANNO_T2,
            TipoTabellaSezione331.VERIFICA_VALORE_SOGLIA,
            TipoTabellaSezione331.LIMITE_SPESA_PERSONALE_2011_2013,
            TipoTabellaSezione331.LIMITE_SPESA_PERSONALE,
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
    public static final List<TipoTabellaSezione331> nameTableUNI = List.of(

    );

    public static final String DIRIGENTI_1_FASCIA = "DIRIGENTI DI 1^ FASCIA";
    public static final String DIRIGENTI_2_FASCIA = "DIRIGENTI DI 2^ FASCIA";
    public static final String TOTALE_COMPLESSIVO = "Totale complessivo";
    public static final String TOTALE_PRIMA_FASCIA_DIR = "Totale di cui Dirigenti I fascia";
    public static final String TOTALE_SECONDA_FASCIA_DIR = "Totale di cui Dirigenti II fascia + Aree";
    public static final String TOTALE_SECONDA_FASCIA_DIR_NO_AREE = "Totale di cui Dirigenti II fascia";
    public static final String TOTALE = "Totale";

    // PAL: l'area "DIRIGENTI" (unica) sostituisce le costanti I^/II^ fascia usate dalla PAC.
    public static final String DIRIGENTI_PAL = "DIRIGENTI";


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
        var reclutamentoAutorizzatoAnnoPrecedente = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reclutamentoAutorizzatoAnnoPrecedente"))
                .findFirst().orElse(null);
        var reclutamentoAutorizzatoAnnoCorrente = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reclutamentoAutorizzatoAnnoCorrente"))
                .findFirst().orElse(null);
        var reclutamentoAutorizzatoAnno2Var = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reclutamentoAutorizzatoAnno2"))
                .findFirst().orElse(null);
        var reclutamentoAutorizzatoAnno3Var = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reclutamentoAutorizzatoAnno3"))
                .findFirst().orElse(null);
        var oneriFinanziariAnno2 = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reportAssunzioniAnno2.oneriFinanziari"))
                .findFirst().orElse(null);
        var oneriFinanziariAnno3 = tableObject.stream()
                .filter(m ->
                        m.get("name").equals("reportAssunzioniAnno3.oneriFinanziari"))
                .findFirst().orElse(null);

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
                createAssunzioniDirigenzialeAnnoPrecedente(result, reclutamentoAutorizzatoAnnoPrecedente);
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
                buildCoperturaFabbisognoDirigenziale(result, dotazioneOrganica,
                        assunzioni1, reclutamentoAutorizzatoAnnoCorrente, oneriFinanziariAnno2);
                break;

            case COPERTURA_FABBISOGNO_ANNO_CORRENTE_AREE_CONTRATTUALI:
                createCoperturaFabbisognoAnnoCorrenteAreeContrattuali(result,
                        reclutamentoAutorizzatoAnnoCorrente, oneriFinanziariAnno2);
                break;

            case CESSAZIONI_ANNO_CORRENTE:
                createCessazioniAnnoCorrente(result, dotazioneOrganica, cessazioniAnnoCorrente);
                break;

            case COPERTURA_FABBISOGNO_ANNO1_DIRIGENZIALE:
                // Stesso mapping di AnnoCorrente: tipologia <- reclutamentoAutorizzatoAnno2.tipologiaReclutamento,
                // fonte <- reportAssunzioniAnno2.autorizzazione.autorizzazione,
                // unita <- reportAssunzioniAnno2.autorizzazione.dirigenti1/dirigenti2,
                // oneri <- reportAssunzioniAnno2.oneriFinanziari.oneriAssunzioniTotali.
                buildCoperturaFabbisognoDirigenziale(result, dotazioneOrganica,
                        assunzioni2, reclutamentoAutorizzatoAnno2Var, oneriFinanziariAnno2);
                break;

            case COPERTURA_FABBISOGNO_ANNO1_AREE_CONTRATTUALI:
                // Stesso mapping/struttura di AnnoCorrente ma con reclutamentoAutorizzatoAnno2
                // come sorgente del reclutamento; oneri sempre da reportAssunzioniAnno2.oneriFinanziari.
                createCoperturaFabbisognoAnnoCorrenteAreeContrattuali(result,
                        reclutamentoAutorizzatoAnno2Var, oneriFinanziariAnno2);
                break;

            case CESSAZIONI_SERVIZIO:
                createCessazioniServizio(result, dotazioneOrganica, cessazioniAnno2);
                break;

            case COPERTURA_FABBISOGNO_ANNO2_DIRIGENZIALE:
                // Stesso mapping di AnnoCorrente ma su sorgenti Anno3.
                buildCoperturaFabbisognoDirigenziale(result, dotazioneOrganica,
                        assunzioni3, reclutamentoAutorizzatoAnno3Var, oneriFinanziariAnno3);
                break;

            case COPERTURA_FABBISOGNO_ANNO2_AREE_CONTRATTUALI:
                // Stesso mapping/struttura di AnnoCorrente/Anno1 ma con reclutamentoAutorizzatoAnno3
                // come sorgente del reclutamento; oneri da reportAssunzioniAnno3.oneriFinanziari.
                // "Obiettivo Operativo" NON mappato perché eliminato.
                createCoperturaFabbisognoAnnoCorrenteAreeContrattuali(result,
                        reclutamentoAutorizzatoAnno3Var, oneriFinanziariAnno3);
                break;

            default:
                // gestione caso non previsto
                break;
        }

        // Garanzia: se la tabella non ha generato rows, ritorna rows come lista vuota
        if (!result.containsKey("rows")) {
            result.put("rows", new ArrayList<>());
        }

        return result;
    }

    // =================================================================================
    // PAL - Enti Locali (ReportEELL)
    // =================================================================================

    /**
     * Entry-point per la costruzione delle tabelle PAL (Enti Locali / ReportEELL).
     * <p>
     * NB: la struttura JSON delle sorgenti Minerva per PAL ha gli stessi nomi di campo
     * della PAC ({@code areaGiuridica}, {@code ula}, {@code valoreFinanziario}, ecc.),
     * MA i valori di {@code areaGiuridica} sono diversi (es. "DIRIGENTI", "FUNZIONARI ED E.Q.",
     * "ISTRUTTORI", "OPERATORI ESPERTI", "OPERATORI") e NON corrispondono alle costanti
     * {@link #DIRIGENTI_1_FASCIA} / {@link #DIRIGENTI_2_FASCIA} usate dai builder PAC.
     * Per questo motivo i builder PAL sono separati.
     */
    public static Map<String, Object> createTablePALMinerva(TipoTabellaSezione331 nameTable, List<Map<String, Object>> tableObject) {
        Map<String, Object> result = new LinkedHashMap<>();

        var dotazioneOrganica = tableObject.stream()
                .filter(m -> m.get("name").equals("reportDotazioneOrganicaERimodulata.dotazioneOrganica"))
                .findFirst().orElse(null);
        var personaleRuolo = tableObject.stream()
                .filter(m -> m.get("name").equals("reportPersonale.personaleDiRuolo"))
                .findFirst().orElse(null);
        var personaleComandatoOut = tableObject.stream()
                .filter(m -> m.get("name").equals("reportPersonale.personaleComandatoOut"))
                .findFirst().orElse(null);
        var personaleComandatoIn = tableObject.stream()
                .filter(m -> m.get("name").equals("reportPersonale.personaleComandatoIn"))
                .findFirst().orElse(null);
        var dirigentiTempoDeterminato = tableObject.stream()
                .filter(m -> m.get("name").equals("reportPersonale.dirigentiTempoDeterminato"))
                .findFirst().orElse(null);
        var cessazioniAnnoPrecedente = tableObject.stream()
                .filter(m -> m.get("name").equals("reportCessazioni.cessazioniAnnoPrecedente"))
                .findFirst().orElse(null);
        var facoltaAssunzionale = tableObject.stream()
                .filter(m -> m.get("name").equals("reportFacoltaAssunzionali.facoltaAssunzionale"))
                .findFirst().orElse(null);
        var reclutamentoAnnoCorrente = tableObject.stream()
                .filter(m -> m.get("name").equals("reclutamentoAnnoCorrente"))
                .findFirst().orElse(null);
        var reclutamentoAnno2 = tableObject.stream()
                .filter(m -> m.get("name").equals("reclutamentoAnno2"))
                .findFirst().orElse(null);
        var reclutamentoAnno3 = tableObject.stream()
                .filter(m -> m.get("name").equals("reclutamentoAnno3"))
                .findFirst().orElse(null);
        var cessazioniAnnoCorrente = tableObject.stream()
                .filter(m -> m.get("name").equals("reportCessazioni.cessazioniAnnoCorrente"))
                .findFirst().orElse(null);
        var cessazioniAnno2 = tableObject.stream()
                .filter(m -> m.get("name").equals("reportCessazioni.cessazioniAnno2"))
                .findFirst().orElse(null);
        var dotazioneOrganicaRimodulata = tableObject.stream()
                .filter(m -> m.get("name").equals("reportDotazioneOrganicaERimodulata.dotazioneOrganicaRimodulata"))
                .findFirst().orElse(null);

        result.put("name", nameTable.name());

        switch (nameTable) {
            case VALORE_FINANZIARIO_DOTAZIONE_ORGANICA:
                createDotazioneOrganicaPAL(result, dotazioneOrganica);
                break;
            case CONSISTENZA_PERSONALE_DIRIGENZIALE:
                createPersonaleDirigenzialePAL(result, dotazioneOrganica, personaleRuolo, personaleComandatoOut);
                break;
            case CONSISTENZA_PERSONALE_NON_DIRIGENZIALE:
                createPersonaleNonDirigenzialePAL(result, dotazioneOrganica, personaleRuolo,
                        personaleComandatoOut, personaleComandatoIn);
                break;
            case CONSISTENZA_PERSONALE_DIRIGENZIALE_TEMPO_DETERMINATO:
                createPersonaleDirigenzialeTempoDeterminatoPAL(result, dirigentiTempoDeterminato);
                break;
            case CONSISTENZA_PERSONALE_NON_DIRIGENZIALE_TEMPO_DETERMINATO:
                createPersonaleNonDirigenzialeTempoDeterminatoPAL(result, dotazioneOrganica, dirigentiTempoDeterminato);
                break;
            case RIEPILOGO_ASSUNZIONI_DIRIGENZIALE:
                break;
            case RIEPILOGO_ASSUNZIONI_AREE_CONTRATTUALI:
                break;
            case RIEPILOGO_CESSAZIONI:
                createRiepilogoCessazioniPAL(result, dotazioneOrganica, cessazioniAnnoPrecedente);
                break;
            case RENDICONTI_ENTRATE_ULTIMI_3_ANNI:
                createRendicontiEntrateUltimi3AnniPAL(result);
                break;
            case SPESE_PERSONALE_ANNO_T2:
                createSpesePersonaleAnnoT2PAL(result, facoltaAssunzionale);
                break;
            case VERIFICA_VALORE_SOGLIA:
                createVerificaValoreSogliaPAL(result, facoltaAssunzionale);
                break;
            case LIMITE_SPESA_PERSONALE_2011_2013:
                createLimiteSpesaPersonale20112013PAL(result);
                break;
            case LIMITE_SPESA_PERSONALE:
                createLimiteSpesaPersonalePAL(result);
                break;
            case DOTAZIONE_ORGANICA_RIMODULAZIONE:
                createDotazioneOrganicaRimodulataPAL(result, dotazioneOrganicaRimodulata);
                break;
            case COPERTURA_FABBISOGNO_ANNO_CORRENTE_DIRIGENZIALE:
                createCoperturaFabbisognoDirigenzialePAL(result, dotazioneOrganica, reclutamentoAnnoCorrente);
                break;
            case COPERTURA_FABBISOGNO_ANNO_CORRENTE_AREE_CONTRATTUALI:
                createCoperturaFabbisognoAreeContrattualiPAL(result, reclutamentoAnnoCorrente);
                break;
            case CESSAZIONI_ANNO_CORRENTE:
                createCessazioniAnnoCorrentePAL(result, dotazioneOrganica, cessazioniAnnoCorrente);
                break;
            case COPERTURA_FABBISOGNO_ANNO1_DIRIGENZIALE:
                // Stesso mapping di AnnoCorrente, sorgente reclutamentoAnno2.
                createCoperturaFabbisognoDirigenzialePAL(result, dotazioneOrganica, reclutamentoAnno2);
                break;
            case COPERTURA_FABBISOGNO_ANNO1_AREE_CONTRATTUALI:
                // Stesso mapping di AnnoCorrente, sorgente reclutamentoAnno2.
                createCoperturaFabbisognoAreeContrattualiPAL(result, reclutamentoAnno2);
                break;
            case CESSAZIONI_SERVIZIO:
                // PAL - Prospetto previsionale cessazioni per il primo anno successivo
                // all'anno corrente (Sezione 3.3.1 - Tabella 21): sorgente reportCessazioni.cessazioniAnno2.
                createCessazioniAnno2PAL(result, dotazioneOrganica, cessazioniAnno2);
                break;
            case COPERTURA_FABBISOGNO_ANNO2_DIRIGENZIALE:
                // Stesso mapping di AnnoCorrente, sorgente reclutamentoAnno3 (Tab 22).
                createCoperturaFabbisognoDirigenzialePAL(result, dotazioneOrganica, reclutamentoAnno3);
                break;
            case COPERTURA_FABBISOGNO_ANNO2_AREE_CONTRATTUALI:
                // Stesso mapping di AnnoCorrente, sorgente reclutamentoAnno3 (Tab 23).
                createCoperturaFabbisognoAreeContrattualiPAL(result, reclutamentoAnno3);
                break;
            default:
                // Le altre tabelle PAL saranno mappate progressivamente.
                break;
        }

        // Garanzia: se la tabella non ha generato rows, ritorna rows come lista vuota
        if (!result.containsKey("rows")) {
            result.put("rows", new ArrayList<>());
        }

        return result;
    }

    /**
     * PAL - Valore Finanziario della Dotazione Organica.
     * <ul>
     *   <li>columns: areaGiuridica (Area Giuridica), ula (Totale unità in D.O.), valoreFinanziario (Valore finanziario della D.O.)</li>
     *   <li>rows: TUTTE le righe di {@code reportDotazioneOrganicaERimodulata.dotazioneOrganica}
     *       (DIRIGENTI, FUNZIONARI ED E.Q., ISTRUTTORI, OPERATORI ESPERTI, OPERATORI)</li>
     *   <li>riga finale "Totale complessivo": somma di ula e valoreFinanziario su tutte le righe</li>
     * </ul>
     */
    private static void createDotazioneOrganicaPAL(Map<String, Object> result,
                                                   Map<String, Object> dotazioneOrganica) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("areaGiuridica", "Area Giuridica");
        columns.put("ula", "Totale unità in D.O.");
        columns.put("valoreFinanziario", "Valore finanziario della D.O.");
        result.put("columns", columns);

        List<Map<String, Object>> rows = castRows(dotazioneOrganica);
        if (rows.isEmpty()) {
            result.put("rows", new ArrayList<>());
            return;
        }

        List<Map<String, Object>> rowsCustom = new ArrayList<>(rows.size() + 1);
        BigDecimal totaleUla = BigDecimal.ZERO;
        BigDecimal totaleValoreFinanziario = BigDecimal.ZERO;

        for (Map<String, Object> row : rows) {
            BigDecimal ula = toBigDecimalSafe(row.get("ula"));
            BigDecimal val = toBigDecimalSafe(row.get("valoreFinanziario"));

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("areaGiuridica", row.get("areaGiuridica"));
            rowObj.put("ula", ula);
            rowObj.put("valoreFinanziario", val);
            rowsCustom.add(rowObj);

            totaleUla = totaleUla.add(ula);
            totaleValoreFinanziario = totaleValoreFinanziario.add(val);
        }

        // PAL: unica riga di totale "Totale complessivo" (non si distinguono
        // sotto-totali per fascia, esistendo una sola voce dirigenziale).
        rowsCustom.add(buildDotazioneTotaleRow(TOTALE_COMPLESSIVO, totaleUla, totaleValoreFinanziario));

        result.put("rows", rowsCustom);
    }

    /**
     * PAL - Dotazione organica in seguito alla rimodulazione (Sezione 3.3.1).
     * <p>
     * Sorgente: {@code reportDotazioneOrganicaERimodulata.dotazioneOrganicaRimodulata}.
     * Struttura analoga a {@link #createDotazioneOrganicaPAL(Map, Map)} (mostra tutte
     * le voci della rimodulazione: DIRIGENTI, FUNZIONARI ED E.Q., ISTRUTTORI,
     * OPERATORI ESPERTI, OPERATORI) con un'<strong>unica</strong> riga finale
     * "Totale complessivo" (somma di {@code ula} e {@code valoreFinanziario}
     * su tutte le righe). In PAL non esistono I^/II^ fascia: niente sotto-totali "di cui ...".
     */
    private static void createDotazioneOrganicaRimodulataPAL(Map<String, Object> result,
                                                             Map<String, Object> dotazioneOrganicaRimodulata) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("areaGiuridica", "Area Giuridica");
        columns.put("ula", "Totale unità in D.O.");
        columns.put("valoreFinanziario", "Valore finanziario della D.O.");
        result.put("columns", columns);

        List<Map<String, Object>> rows = castRows(dotazioneOrganicaRimodulata);
        if (rows.isEmpty()) {
            result.put("rows", new ArrayList<>());
            return;
        }

        List<Map<String, Object>> rowsCustom = new ArrayList<>(rows.size() + 1);
        BigDecimal totaleUla = BigDecimal.ZERO;
        BigDecimal totaleValoreFinanziario = BigDecimal.ZERO;

        for (Map<String, Object> row : rows) {
            BigDecimal ula = toBigDecimalSafe(row.get("ula"));
            BigDecimal val = toBigDecimalSafe(row.get("valoreFinanziario"));

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("areaGiuridica", row.get("areaGiuridica"));
            rowObj.put("ula", ula);
            rowObj.put("valoreFinanziario", val);
            rowsCustom.add(rowObj);

            totaleUla = totaleUla.add(ula);
            totaleValoreFinanziario = totaleValoreFinanziario.add(val);
        }

        // PAL: unica riga di totale "Totale complessivo".
        rowsCustom.add(buildDotazioneTotaleRow(TOTALE_COMPLESSIVO, totaleUla, totaleValoreFinanziario));

        result.put("rows", rowsCustom);
    }

    /**
     * PAL - Consistenza Personale Dirigenziale.
     * Unica categoria "DIRIGENTI" (in PAL non esistono I^/II^ fascia, e il tempo determinato
     * è gestito in una tabella separata {@code CONSISTENZA_PERSONALE_DIRIGENZIALE_TEMPO_DETERMINATO}).
     * <ul>
     *   <li>qualifiche       ← "DIRIGENTI"</li>
     *   <li>postiDotazione   ← reportDotazioneOrganicaERimodulata.dotazioneOrganica.ula (DIRIGENTI)</li>
     *   <li>personaleRuolo   ← reportPersonale.personaleDiRuolo.ula (DIRIGENTI)</li>
     *   <li>comandatiOut     ← reportPersonale.personaleComandatoOut.ula (DIRIGENTI)</li>
     *   <li>totaleUnita      ← personaleRuolo + comandatiOut</li>
     * </ul>
     * In coda riga "Totale complessivo".
     */
    private static void createPersonaleDirigenzialePAL(Map<String, Object> result,
                                                       Map<String, Object> dotazioneOrganica,
                                                       Map<String, Object> personaleRuolo,
                                                       Map<String, Object> personaleComandatoOut) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("qualifiche", "Qualifiche");
        columns.put("postiDotazione", "Posti in Dotazione Organica");
        columns.put("personaleRuolo", "Personale di ruolo al 31/12/ anno t-1");
        columns.put("comandatiOut", "Comandati OUT al 31/12/ anno t-1");
        columns.put("totaleUnita", "Totale unità");
        result.put("columns", columns);

        List<Map<String, Object>> doRows = castRows(dotazioneOrganica);
        List<Map<String, Object>> prRows = castRows(personaleRuolo);
        List<Map<String, Object>> coRows = castRows(personaleComandatoOut);

        if (allEmpty(doRows, prRows, coRows)) {
            result.put("rows", new ArrayList<>());
            return;
        }

        BigDecimal posti = lookupSumByArea(doRows, DIRIGENTI_PAL, "ula");
        BigDecimal pr = lookupSumByArea(prRows, DIRIGENTI_PAL, "ula");
        BigDecimal co = lookupSumByArea(coRows, DIRIGENTI_PAL, "ula");
        BigDecimal totale = pr.add(co);

        List<Map<String, Object>> rowsCustom = new ArrayList<>(2);
        Map<String, Object> dirRow = new LinkedHashMap<>();
        dirRow.put("qualifiche", DIRIGENTI_PAL);
        dirRow.put("postiDotazione", posti);
        dirRow.put("personaleRuolo", pr);
        dirRow.put("comandatiOut", co);
        dirRow.put("totaleUnita", totale);
        rowsCustom.add(dirRow);

        Map<String, Object> totaleRow = new LinkedHashMap<>();
        totaleRow.put("qualifiche", TOTALE_COMPLESSIVO);
        totaleRow.put("postiDotazione", scale2(posti));
        totaleRow.put("personaleRuolo", scale2(pr));
        totaleRow.put("comandatiOut", scale2(co));
        totaleRow.put("totaleUnita", scale2(totale));
        rowsCustom.add(totaleRow);

        result.put("rows", rowsCustom);
    }

    /**
     * PAL - Consistenza Personale Non Dirigenziale.
     * Aree non dirigenziali (tutte ≠ "DIRIGENTI"): FUNZIONARI ED E.Q., ISTRUTTORI,
     * OPERATORI ESPERTI, OPERATORI.
     * <ul>
     *   <li>areaContrattuale ← areaGiuridica (da reportDotazioneOrganicaERimodulata.dotazioneOrganica)</li>
     *   <li>postiDotazione   ← reportDotazioneOrganicaERimodulata.dotazioneOrganica.ula (lookup per area)</li>
     *   <li>personaleRuolo   ← reportPersonale.personaleDiRuolo.ula (lookup per area)</li>
     *   <li>comandatiOut     ← reportPersonale.personaleComandatoOut.ula (lookup per area)</li>
     *   <li>comandatiIn      ← reportPersonale.personaleComandatoIn.ula (lookup per area)</li>
     *   <li>totaleUnita      ← personaleRuolo + comandatiOut + comandatiIn</li>
     * </ul>
     * In coda riga "Totale complessivo" con somma colonne.
     */
    private static void createPersonaleNonDirigenzialePAL(Map<String, Object> result,
                                                          Map<String, Object> dotazioneOrganica,
                                                          Map<String, Object> personaleRuolo,
                                                          Map<String, Object> personaleComandatoOut,
                                                          Map<String, Object> personaleComandatoIn) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("areaContrattuale", "Area contrattuale CCNL");
        columns.put("postiDotazione", "Posti in Dotazione Organica");
        columns.put("personaleRuolo", "Personale di ruolo al 31/12/ anno t-1");
        columns.put("comandatiOut", "Comandati OUT al 31/12/ anno t-1");
        columns.put("comandatiIn", "Comandati IN al 31/12/anno t-1");
        columns.put("totaleUnita", "Totale unità");
        result.put("columns", columns);

        List<Map<String, Object>> doRows = castRows(dotazioneOrganica);
        List<Map<String, Object>> prRows = castRows(personaleRuolo);
        List<Map<String, Object>> coRows = castRows(personaleComandatoOut);
        List<Map<String, Object>> ciRows = castRows(personaleComandatoIn);

        if (allEmpty(doRows, prRows, coRows, ciRows)) {
            result.put("rows", new ArrayList<>());
            return;
        }

        List<Map<String, Object>> rowsCustom = new ArrayList<>();
        BigDecimal totPosti = BigDecimal.ZERO;
        BigDecimal totPr = BigDecimal.ZERO;
        BigDecimal totCo = BigDecimal.ZERO;
        BigDecimal totCi = BigDecimal.ZERO;
        BigDecimal totUnita = BigDecimal.ZERO;

        for (Map<String, Object> row : doRows) {
            String area = String.valueOf(row.get("areaGiuridica"));
            // Esclude i dirigenti (PAL): unica voce "DIRIGENTI"
            if (DIRIGENTI_PAL.equals(area)) continue;

            BigDecimal posti = toBigDecimalSafe(row.get("ula"));
            BigDecimal pr = lookupSumByArea(prRows, area, "ula");
            BigDecimal co = lookupSumByArea(coRows, area, "ula");
            BigDecimal ci = lookupSumByArea(ciRows, area, "ula");
            BigDecimal totale = pr.add(co).add(ci);

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("areaContrattuale", area);
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

        if (rowsCustom.isEmpty()) {
            result.put("rows", new ArrayList<>());
            return;
        }

        Map<String, Object> totaleRow = new LinkedHashMap<>();
        totaleRow.put("areaContrattuale", TOTALE_COMPLESSIVO);
        totaleRow.put("postiDotazione", scale2(totPosti));
        totaleRow.put("personaleRuolo", scale2(totPr));
        totaleRow.put("comandatiOut", scale2(totCo));
        totaleRow.put("comandatiIn", scale2(totCi));
        totaleRow.put("totaleUnita", scale2(totUnita));
        rowsCustom.add(totaleRow);

        result.put("rows", rowsCustom);
    }

    /**
     * PAL - Consistenza personale dirigenziale a tempo determinato (al 31.12 anno precedente).
     * <ul>
     *   <li>qualifiche       ← "DIRIGENTI" (unica voce in PAL)</li>
     *   <li>valoreEconomico  ← reportPersonale.dirigentiTempoDeterminato.valoreFinanziario (where areaGiuridica=DIRIGENTI)</li>
     *   <li>norma            ← "" (Norma di riferimento non trovata su Minerva, da mappare in futuro)</li>
     *   <li>totaleUnita      ← reportPersonale.dirigentiTempoDeterminato.ula (where areaGiuridica=DIRIGENTI)</li>
     * </ul>
     * In coda riga "Totale complessivo" con somma di valoreEconomico e totaleUnita.
     */
    private static void createPersonaleDirigenzialeTempoDeterminatoPAL(Map<String, Object> result,
                                                                        Map<String, Object> dirigentiTempoDeterminato) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("qualifiche", "Qualifiche");
        columns.put("valoreEconomico", "Valore economico");
        columns.put("norma", "Norma di riferimento");
        columns.put("totaleUnita", "Totale unità");
        result.put("columns", columns);

        List<Map<String, Object>> tdRows = castRows(dirigentiTempoDeterminato);
        if (allEmpty(tdRows)) {
            result.put("rows", new ArrayList<>());
            return;
        }

        BigDecimal valore = lookupSumByArea(tdRows, DIRIGENTI_PAL, "valoreFinanziario");
        BigDecimal unita = lookupSumByArea(tdRows, DIRIGENTI_PAL, "ula");

        List<Map<String, Object>> rowsCustom = new ArrayList<>(2);

        Map<String, Object> dirRow = new LinkedHashMap<>();
        dirRow.put("qualifiche", DIRIGENTI_PAL);
        dirRow.put("valoreEconomico", scale2(valore));
        dirRow.put("norma", "");
        dirRow.put("totaleUnita", unita);
        rowsCustom.add(dirRow);

        Map<String, Object> totaleRow = new LinkedHashMap<>();
        totaleRow.put("qualifiche", TOTALE_COMPLESSIVO);
        totaleRow.put("valoreEconomico", scale2(valore));
        totaleRow.put("norma", "");
        totaleRow.put("totaleUnita", scale2(unita));
        rowsCustom.add(totaleRow);

        result.put("rows", rowsCustom);
    }

    /**
     * PAL - Consistenza personale NON dirigenziale a tempo determinato (al 31.12 anno precedente).
     * <ul>
     *   <li>areaContrattuale ← reportDotazioneOrganicaERimodulata.dotazioneOrganica.areaGiuridica
     *       filtrato escludendo "DIRIGENTI"</li>
     *   <li>valoreEconomico  ← reportPersonale.dirigentiTempoDeterminato.valoreFinanziario
     *       (lookup per areaGiuridica, where areaGiuridica != DIRIGENTI)</li>
     *   <li>norma            ← "" (Norma di riferimento non trovata su Minerva, da mappare in futuro)</li>
     *   <li>totaleUnita      ← reportPersonale.dirigentiTempoDeterminato.ula (lookup per areaGiuridica)</li>
     * </ul>
     * In coda riga "Totale complessivo" con somma di valoreEconomico e totaleUnita.
     * Se la sorgente {@code dirigentiTempoDeterminato} non contiene righe per aree non dirigenziali → tabella vuota.
     */
    private static void createPersonaleNonDirigenzialeTempoDeterminatoPAL(Map<String, Object> result,
                                                                           Map<String, Object> dotazioneOrganica,
                                                                           Map<String, Object> dirigentiTempoDeterminato) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("areaContrattuale", "Area contrattuale CCNL");
        columns.put("valoreEconomico", "Valore economico");
        columns.put("norma", "Norma di riferimento");
        columns.put("totaleUnita", "Totale unità");
        result.put("columns", columns);

        List<Map<String, Object>> doRows = castRows(dotazioneOrganica);
        List<Map<String, Object>> tdRows = castRows(dirigentiTempoDeterminato);

        if (doRows.isEmpty() || allEmpty(tdRows)) {
            result.put("rows", new ArrayList<>());
            return;
        }

        List<Map<String, Object>> rowsCustom = new ArrayList<>(doRows.size() + 1);
        BigDecimal totValore = BigDecimal.ZERO;
        BigDecimal totUnita = BigDecimal.ZERO;
        boolean hasAnyRow = false;

        for (Map<String, Object> row : doRows) {
            String area = String.valueOf(row.get("areaGiuridica"));
            if (DIRIGENTI_PAL.equals(area)) continue;

            BigDecimal valore = lookupSumByArea(tdRows, area, "valoreFinanziario");
            BigDecimal unita = lookupSumByArea(tdRows, area, "ula");

            // Mostra solo le aree che hanno effettivamente personale a tempo determinato
            if (valore.signum() == 0 && unita.signum() == 0) continue;

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("areaContrattuale", area);
            rowObj.put("valoreEconomico", scale2(valore));
            rowObj.put("norma", "");
            rowObj.put("totaleUnita", unita);
            rowsCustom.add(rowObj);

            totValore = totValore.add(valore);
            totUnita = totUnita.add(unita);
            hasAnyRow = true;
        }

        if (!hasAnyRow) {
            result.put("rows", new ArrayList<>());
            return;
        }

        Map<String, Object> totaleRow = new LinkedHashMap<>();
        totaleRow.put("areaContrattuale", TOTALE_COMPLESSIVO);
        totaleRow.put("valoreEconomico", scale2(totValore));
        totaleRow.put("norma", "");
        totaleRow.put("totaleUnita", scale2(totUnita));
        rowsCustom.add(totaleRow);

        result.put("rows", rowsCustom);
    }

    /**
     * PAL - Riepilogo cessazioni al 31/12 dell'anno precedente.
     * Sorgente unità/risorse: {@code reportCessazioni.cessazioniAnnoPrecedente}
     * (campi {@code ula} e {@code valoreEconomico}). Le aree vengono prese da
     * {@code reportDotazioneOrganicaERimodulata.dotazioneOrganica} per garantire
     * la presenza di tutte le righe; in coda riga "Totale complessivo".
     * <p>
     * NB: in PAL non esistono le sotto-categorie I^/II^ fascia, quindi non vengono
     * generate righe "Totale di cui ...".
     */
    private static void createRiepilogoCessazioniPAL(Map<String, Object> result,
                                                     Map<String, Object> dotazioneOrganica,
                                                     Map<String, Object> cessazioniAnnoPrecedente) {
        buildCessazioniTablePAL(result, dotazioneOrganica, cessazioniAnnoPrecedente,
                "Totale unità cessate", "Totale risorse da cessazione");
    }

    /**
     * PAL - Prospetto previsionale cessazioni dal servizio e risparmio su base annua per
     * l'anno corrente (Sezione 3.3.1 - Tabella 18). Stessa logica di
     * {@link #createRiepilogoCessazioniPAL(Map, Map, Map)} ma su sorgente
     * {@code reportCessazioni.cessazioniAnnoCorrente} e con header colonne specifici
     * "Totale unità anno" / "Totale risorse da cessazione anno" (come da foglio mapping
     * Sezione 3.3.1).
     */
    private static void createCessazioniAnnoCorrentePAL(Map<String, Object> result,
                                                        Map<String, Object> dotazioneOrganica,
                                                        Map<String, Object> cessazioniAnnoCorrente) {
        buildCessazioniTablePAL(result, dotazioneOrganica, cessazioniAnnoCorrente,
                "Totale unità anno", "Totale risorse da cessazione anno");
    }

    /**
     * PAL - Prospetto previsionale cessazioni dal servizio e risparmio su base annua per
     * il primo anno successivo all'anno corrente (Sezione 3.3.1 - Tabella 21).
     * Stessa logica di {@link #createCessazioniAnnoCorrentePAL(Map, Map, Map)} ma con sorgente
     * {@code reportCessazioni.cessazioniAnno2}. Label colonne identici a Tab 18.
     */
    private static void createCessazioniAnno2PAL(Map<String, Object> result,
                                                 Map<String, Object> dotazioneOrganica,
                                                 Map<String, Object> cessazioniAnno2) {
        buildCessazioniTablePAL(result, dotazioneOrganica, cessazioniAnno2,
                "Totale unità anno", "Totale risorse da cessazione anno");
    }

    /**
     * Helper comune per le tabelle di cessazioni PAL (Riepilogo anno precedente / anno corrente).
     * <ul>
     *   <li>columns: areaGiuridica ("Area contrattuale CCNL e qualifiche"), ula (label dinamica),
     *       valoreEconomico (label dinamica)</li>
     *   <li>rows: una riga per ogni area presente in {@code dotazioneOrganica}, con somma
     *       per area di {@code ula} e {@code valoreEconomico} dalla sorgente cessazioni</li>
     *   <li>riga finale "Totale complessivo" con somme di colonna</li>
     * </ul>
     */
    private static void buildCessazioniTablePAL(Map<String, Object> result,
                                                Map<String, Object> dotazioneOrganica,
                                                Map<String, Object> cessazioni,
                                                String labelUla,
                                                String labelValoreEconomico) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("areaGiuridica", "Area contrattuale CCNL e qualifiche");
        columns.put("ula", labelUla);
        columns.put("valoreEconomico", labelValoreEconomico);
        result.put("columns", columns);

        List<Map<String, Object>> doRows = castRows(dotazioneOrganica);
        List<Map<String, Object>> cessRows = castRows(cessazioni);

        if (doRows.isEmpty() || allEmpty(cessRows)) {
            result.put("rows", new ArrayList<>());
            return;
        }

        List<Map<String, Object>> rowsCustom = new ArrayList<>(doRows.size() + 1);
        BigDecimal totUla = BigDecimal.ZERO;
        BigDecimal totVal = BigDecimal.ZERO;

        for (Map<String, Object> row : doRows) {
            String area = String.valueOf(row.get("areaGiuridica"));
            BigDecimal ula = lookupSumByArea(cessRows, area, "ula");
            BigDecimal val = lookupSumByArea(cessRows, area, "valoreEconomico");

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("areaGiuridica", area);
            rowObj.put("ula", ula);
            rowObj.put("valoreEconomico", scale2(val));
            rowsCustom.add(rowObj);

            totUla = totUla.add(ula);
            totVal = totVal.add(val);
        }

        rowsCustom.add(buildCessazioneTotaleRow(TOTALE_COMPLESSIVO, totUla, totVal));
        result.put("rows", rowsCustom);
    }

    /**
     * PAL - Prospetto calcolo facoltà assunzioni: Tabella entrate degli ultimi 3 rendiconti approvati.
     * <p>
     * <b>Mapping NON disponibile</b>: la sorgente Minerva non espone (ancora) i dati relativi
     * ai rendiconti delle entrate (Titolo I/II/III, FCDE, ecc.). La tabella viene quindi
     * generata con la sola struttura delle colonne e rows vuote, in attesa che la "Norma di
     * riferimento"/sorgente dati venga definita.
     * <ul>
     *   <li>columns: titolo, rendicontoT4, rendicontoT3, rendicontoT2, valoreFCDE, media</li>
     *   <li>rows: [] (placeholder)</li>
     * </ul>
     */
    private static void createRendicontiEntrateUltimi3AnniPAL(Map<String, Object> result) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("titolo", "Entrate relative ai primi tre titoli delle entrate degli ultimi tre rendiconti approvati");
        columns.put("rendicontoT4", "Rendiconto anno t-4");
        columns.put("rendicontoT3", "Rendiconto anno t-3");
        columns.put("rendicontoT2", "Rendiconto anno t-2");
        columns.put("valoreFCDE", "Valore FCDE");
        columns.put("media", "Media");
        result.put("columns", columns);
        // Mapping non ancora disponibile → rows vuote
        result.put("rows", new ArrayList<>());
    }

    /**
     * PAL - Prospetto calcolo facoltà assunzioni: Tabella delle spese del personale anno t-2.
     * <p>
     * Sorgente: {@code reportFacoltaAssunzionali.facoltaAssunzionale.dati}
     * con i campi {@code descrizione} e {@code valore}.
     * <ul>
     *   <li>columns: vociSpesa (Voci di spesa), importoSpesa (Importo spesa €)</li>
     *   <li>rows: una riga per ogni elemento di {@code dati} con
     *       (descrizione → vociSpesa, valore → importoSpesa)</li>
     *   <li>riga finale "Totale spesa del personale": somma di {@code valore}</li>
     * </ul>
     * NB: il nome di colonna chiave {@code valore} viene rinominato a {@code importoSpesa}
     * (alias "spesa" indicato nel foglio mapping) per coerenza con la UI.
     */
    private static void createSpesePersonaleAnnoT2PAL(Map<String, Object> result,
                                                      Map<String, Object> facoltaAssunzionale) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("vociSpesa", "Voci di spesa");
        columns.put("importoSpesa", "Importo spesa (€)");
        result.put("columns", columns);

        List<Map<String, Object>> rows = castRows(facoltaAssunzionale);
        if (allEmpty(rows)) {
            result.put("rows", new ArrayList<>());
            return;
        }

        List<Map<String, Object>> rowsCustom = new ArrayList<>(rows.size() + 1);
        BigDecimal totale = BigDecimal.ZERO;

        for (Map<String, Object> row : rows) {
            Object descrizione = row.get("descrizione");
            BigDecimal valore = toBigDecimalSafe(row.get("valore"));

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("vociSpesa", descrizione == null ? "" : descrizione);
            rowObj.put("importoSpesa", scale2(valore));
            rowsCustom.add(rowObj);

            totale = totale.add(valore);
        }

        Map<String, Object> totaleRow = new LinkedHashMap<>();
        totaleRow.put("vociSpesa", "Totale spesa del personale");
        totaleRow.put("importoSpesa", scale2(totale));
        rowsCustom.add(totaleRow);

        result.put("rows", rowsCustom);
    }

    /**
     * PAL - Prospetto calcolo facoltà assunzioni: Verifica del valore soglia.
     * <p>
     * Sorgente: {@code reportFacoltaAssunzionali.facoltaAssunzionale.dati}
     * con i campi {@code descrizione} e {@code valore} (Tabella 12 - mapping foglio
     * Sezione 3.3.1, riga "Verifica del valore soglia").
     * <ul>
     *   <li>columns: vociVerifica (Voci di verifica), totaliVerifica (Totali di verifica)</li>
     *   <li>rows: una riga per ogni elemento di {@code dati}
     *       (descrizione → vociVerifica, valore → totaliVerifica)</li>
     * </ul>
     * NB: nessuna riga di totale finale (la UI di riferimento non la prevede,
     * essendo già una tabella di indicatori sintetici eterogenei: medie, percentuali,
     * incidenze, ecc.).
     */
    private static void createVerificaValoreSogliaPAL(Map<String, Object> result,
                                                      Map<String, Object> facoltaAssunzionale) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("vociVerifica", "Voci di verifica");
        columns.put("totaliVerifica", "Totali di verifica");
        result.put("columns", columns);

        List<Map<String, Object>> rows = castRows(facoltaAssunzionale);
        if (allEmpty(rows)) {
            result.put("rows", new ArrayList<>());
            return;
        }

        List<Map<String, Object>> rowsCustom = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Object descrizione = row.get("descrizione");
            BigDecimal valore = toBigDecimalSafe(row.get("valore"));

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("vociVerifica", descrizione == null ? "" : descrizione);
            rowObj.put("totaliVerifica", scale2(valore));
            rowsCustom.add(rowObj);
        }

        result.put("rows", rowsCustom);
    }

    /**
     * PAL - Limiti spesa del personale ai sensi dell'art. 1, comma 557, legge n. 296/2006:
     * Tabella limite spesa del personale 2011-2013 (Sezione 3.3.1 - Tabella 13).
     * <p>
     * <b>Mapping NON disponibile</b>: la sorgente Minerva (oggetto JSON di input) non espone
     * i dati storici della spesa di personale degli anni 2011, 2012, 2013 né il limite
     * spesa media triennio ai fini del comma 557 (D). La tabella viene quindi generata
     * con la sola struttura delle colonne e rows vuote, in attesa che la sorgente
     * dati venga definita.
     * <ul>
     *   <li>columns: spesaPersonale, anno2011, anno2012, anno2013, media</li>
     *   <li>rows: [] (placeholder)</li>
     * </ul>
     * Quando la sorgente sarà disponibile, le righe attese sono:
     * <ul>
     *   <li>"Totale spesa di personale" (valori per 2011/2012/2013, media calcolata)</li>
     *   <li>"Limite spesa media triennio ai fini del comma 557 (D)" (solo colonna Media,
     *       calcolata come media dei 3 importi)</li>
     * </ul>
     */
    private static void createLimiteSpesaPersonale20112013PAL(Map<String, Object> result) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("spesaPersonale", "Spesa del personale");
        columns.put("anno2011", "Anno 1 - 2011");
        columns.put("anno2012", "Anno 2 - 2012");
        columns.put("anno2013", "Anno 3 - 2013");
        columns.put("media", "Media");
        result.put("columns", columns);
        // Mapping non ancora disponibile → rows vuote
        result.put("rows", new ArrayList<>());
    }

    /**
     * PAL - Limiti spesa del personale: Tabella spesa personale (Sezione 3.3.1 - Tabella 14).
     * <p>
     * <b>Mapping NON disponibile</b>: la sorgente Minerva (oggetto JSON di input) non espone
     * i dati di dettaglio delle voci di spesa per anno t / t+1 / t+2 né i totali
     * derivati (Componenti escluse B, Componenti assoggettate al limite A-B=C,
     * Limite spesa media triennio 2011-2013 D, Differenza spesa di personale e limite C-D).
     * La tabella viene quindi generata con la sola struttura delle colonne e rows vuote,
     * in attesa che la sorgente dati venga definita.
     * <ul>
     *   <li>columns: spesaPersonale, annoT, annoT1, annoT2, media</li>
     *   <li>rows: [] (placeholder)</li>
     * </ul>
     * Quando la sorgente sarà disponibile, le righe attese sono:
     * <ul>
     *   <li>N righe "Voce di spesa" (valori per anno t/t+1/t+2, media calcolata per colonna)</li>
     *   <li>"Totale spesa personale (A)" — somma per colonna</li>
     *   <li>"Componenti escluse (B)"</li>
     *   <li>"Componenti assoggettate al limite di spesa (A-B=C)" — Totale spesa personale - B</li>
     *   <li>"Limite spesa media triennio 2011-2013 (D)" — riferimento a Tabella 13 (solo Media)</li>
     *   <li>"Differenza spesa di personale e limite spesa (C-D)" — C - D per colonna</li>
     * </ul>
     */
    private static void createLimiteSpesaPersonalePAL(Map<String, Object> result) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("spesaPersonale", "Spesa del personale");
        columns.put("annoT", "Anno t");
        columns.put("annoT1", "Anno t+1");
        columns.put("annoT2", "Anno t+2");
        columns.put("media", "Media");
        result.put("columns", columns);
        // Mapping non ancora disponibile → rows vuote
        result.put("rows", new ArrayList<>());
    }

    /**
     * PAL - Copertura del fabbisogno dell'anno corrente: Tabella del personale dirigenziale
     * (Sezione 3.3.1 - Tabella 16).
     * <p>
     * NB: il mapping descritto nel foglio Excel si riferisce al flusso PAC (sorgenti
     * {@code reclutamentoAutorizzatoAnnoCorrente}, {@code reportAssunzioniAnnoCorrente.autorizzazione},
     * {@code reportAssunzioniAnno2.oneriFinanziari}) che NON esistono nel JSON PAL.
     * Per PAL la sorgente è {@code reclutamentoAnnoCorrente.dati} che contiene già in un'unica
     * struttura tutte le informazioni necessarie (ula, area, procedura, tipologia, oneri).
     * <p>
     * In PAL esiste un'unica voce dirigenziale ("DIRIGENTI"), quindi non vengono generate
     * le sotto-sezioni I^/II^ fascia tipiche della PAC.
     * <ul>
     *   <li>qualifiche  ← "DIRIGENTI" (unica voce PAL)</li>
     *   <li>tipologia   ← reclutamentoAnnoCorrente.dati[].proceduraSelettiva
     *       (analogo PAL di {@code tipologiaReclutamento} della PAC)</li>
     *   <li>fonte       ← reclutamentoAnnoCorrente.dati[].tipologiaProcedura
     *       (analogo PAL del campo "Autorizzazione" della PAC)</li>
     *   <li>totaleUnita ← reclutamentoAnnoCorrente.dati[].ula</li>
     *   <li>totaleOneri ← reclutamentoAnnoCorrente.dati[].oneriFinanziari</li>
     * </ul>
     * Una riga per ogni elemento di {@code reclutamentoAnnoCorrente.dati} con
     * {@code areaGiuridica = "DIRIGENTI"}, seguita da riga "Totale di cui Dirigenti"
     * con somma di unità e oneri.
     * Se la sorgente non contiene righe per DIRIGENTI → tabella vuota.
     */
    private static void createCoperturaFabbisognoDirigenzialePAL(Map<String, Object> result,
                                                                 Map<String, Object> dotazioneOrganica,
                                                                 Map<String, Object> reclutamentoAnno) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("qualifiche", "Qualifiche");
        columns.put("tipologia", "Tipologia di reclutamento");
        columns.put("fonte", "Fonte di finanziamento");
        columns.put("totaleUnita", "Totale Unità");
        // PAL: la colonna "Totale oneri assunzionali" NON è prevista (rimossa rispetto al PAC).
        result.put("columns", columns);

        // Pre-condizione: la dotazione organica deve avere la voce DIRIGENTI (PAL),
        // altrimenti la tabella dirigenziale non è applicabile.
        List<Map<String, Object>> doRows = castRows(dotazioneOrganica);
        boolean hasDirigentiInDO = doRows.stream()
                .anyMatch(r -> DIRIGENTI_PAL.equals(String.valueOf(r.get("areaGiuridica"))));
        if (!hasDirigentiInDO) {
            result.put("rows", new ArrayList<>());
            return;
        }

        List<Map<String, Object>> recRows = castRows(reclutamentoAnno);
        if (allEmpty(recRows)) {
            result.put("rows", new ArrayList<>());
            return;
        }

        List<Map<String, Object>> rowsCustom = new ArrayList<>();
        BigDecimal totUnita = BigDecimal.ZERO;

        for (Map<String, Object> row : recRows) {
            if (!DIRIGENTI_PAL.equals(String.valueOf(row.get("areaGiuridica")))) continue;

            BigDecimal unita = toBigDecimalSafe(row.get("ula"));

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("qualifiche", DIRIGENTI_PAL);
            rowObj.put("tipologia", row.get("proceduraSelettiva"));
            rowObj.put("fonte", row.get("tipologiaProcedura"));
            rowObj.put("totaleUnita", unita);
            rowsCustom.add(rowObj);

            totUnita = totUnita.add(unita);
        }

        if (rowsCustom.isEmpty()) {
            result.put("rows", new ArrayList<>());
            return;
        }

        // PAL: riga di totale "Totale dirigenti" con la sola somma di totaleUnita.
        // Questo totale (unità) confluisce inoltre nel "Totale complessivo assunzioni"
        // calcolato in {@link #createCoperturaFabbisognoAreeContrattualiPAL}.
        Map<String, Object> totRow = new LinkedHashMap<>();
        totRow.put("qualifiche", "Totale dirigenti");
        totRow.put("tipologia", "");
        totRow.put("fonte", "");
        totRow.put("totaleUnita", totUnita);
        rowsCustom.add(totRow);
        result.put("rows", rowsCustom);
    }

    /**
     * PAL - Copertura del fabbisogno dell'anno corrente: Tabella delle aree contrattuali
     * (Sezione 3.3.1 - Tabella 17).
     * <p>
     * La colonna "Obiettivo Operativo" è eliminata (barrata in rosso nel mapping) e la
     * colonna "Totale oneri assunzionali" non è prevista lato PAL.
     * Sorgente PAL: {@code reclutamentoAnnoCorrente.dati} (in PAL <strong>non</strong> esiste
     * {@code reclutamentoAutorizzatoAnnoCorrente}: il campo è semplicemente {@code reclutamentoAnnoCorrente}).
     * Le righe sono filtrate sulle aree NON dirigenziali (tutte le righe con
     * {@code areaGiuridica != "DIRIGENTI"}); le righe DIRIGENTI confluiscono solo
     * nel "Totale complessivo assunzioni".
     * <ul>
     *   <li>areaContrattuale ← reclutamentoAnnoCorrente.dati[].areaGiuridica</li>
     *   <li>profiliRuolo     ← reclutamentoAnnoCorrente.dati[].profiloRuolo</li>
     *   <li>tipologia        ← reclutamentoAnnoCorrente.dati[].proceduraSelettiva</li>
     *   <li>fonte            ← reclutamentoAnnoCorrente.dati[].tipologiaProcedura</li>
     *   <li>totaleUnita      ← reclutamentoAnnoCorrente.dati[].ula</li>
     * </ul>
     * Le righe sono raggruppate per area contrattuale preservando l'ordine di apparizione;
     * dopo ogni gruppo una riga "Totale" con la sola somma delle unità. In coda riga
     * "Totale aree" (somma unità di tutte le aree) e "Totale complessivo assunzioni"
     * (= unità aree + unità dirigenti).
     */
    private static void createCoperturaFabbisognoAreeContrattualiPAL(Map<String, Object> result,
                                                                     Map<String, Object> reclutamentoAnno) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("areaContrattuale", "Area contrattuale CCNL");
        columns.put("profiliRuolo", "Profili di ruolo");
        columns.put("tipologia", "Tipologia di reclutamento");
        columns.put("fonte", "Fonte di finanziamento");
        columns.put("totaleUnita", "Totale unità");
        // PAL: la colonna "Totale oneri assunzionali" NON è prevista (rimossa rispetto al PAC).
        result.put("columns", columns);

        List<Map<String, Object>> recRows = castRows(reclutamentoAnno);
        if (allEmpty(recRows)) {
            result.put("rows", new ArrayList<>());
            return;
        }

        // Raggruppamento per area contrattuale, preservando l'ordine di apparizione.
        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        Map<String, BigDecimal> unitaPerArea = new LinkedHashMap<>();
        BigDecimal totUnitaAree = BigDecimal.ZERO;
        // PAL: unità totali dei DIRIGENTI dalla stessa sorgente, da sommare nel
        // "Totale complessivo assunzioni" insieme al "Totale aree".
        BigDecimal totUnitaDirigenti = BigDecimal.ZERO;

        for (Map<String, Object> row : recRows) {
            String area = String.valueOf(row.get("areaGiuridica"));
            BigDecimal unita = toBigDecimalSafe(row.get("ula"));
            // Esclude la voce DIRIGENTI (PAL: unica voce dirigenziale, gestita nella Tab. dirigenziale)
            // ma ne accumula le unità per il "Totale complessivo assunzioni".
            if (DIRIGENTI_PAL.equals(area)) {
                totUnitaDirigenti = totUnitaDirigenti.add(unita);
            }

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("areaContrattuale", area);
            rowObj.put("profiliRuolo", row.get("profiloRuolo"));
            rowObj.put("tipologia", row.get("proceduraSelettiva"));
            rowObj.put("fonte", row.get("tipologiaProcedura"));
            rowObj.put("totaleUnita", unita);

            grouped.computeIfAbsent(area, k -> new ArrayList<>()).add(rowObj);
            unitaPerArea.merge(area, unita, BigDecimal::add);
            totUnitaAree = totUnitaAree.add(unita);
        }

        if (grouped.isEmpty()) {
            result.put("rows", new ArrayList<>());
            return;
        }

        List<Map<String, Object>> rowsCustom = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
            rowsCustom.addAll(entry.getValue());
            rowsCustom.add(buildAreaContrattualeTotaleRowPAL(TOTALE, unitaPerArea.get(entry.getKey())));
        }
        rowsCustom.add(buildAreaContrattualeTotaleRowPAL("Totale aree", totUnitaAree));
        // "Totale complessivo assunzioni" = unità aree contrattuali + unità dirigenziale
        // (proveniente dalla stessa sorgente reclutamentoAnno, righe DIRIGENTI).
        rowsCustom.add(buildAreaContrattualeTotaleRowPAL("Totale complessivo assunzioni",
                totUnitaAree.add(totUnitaDirigenti)));

        result.put("rows", rowsCustom);
    }

    /**
     * Riga "Totale"/"Totale aree"/"Totale complessivo assunzioni" per la tabella
     * AREE CONTRATTUALI PAL: valorizza solo {@code totaleUnita} (la colonna
     * {@code totaleOneri} non è prevista lato PAL). Le altre colonne sono vuote.
     */
    private static Map<String, Object> buildAreaContrattualeTotaleRowPAL(String label, BigDecimal totUnita) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("areaContrattuale", label);
        row.put("profiliRuolo", "");
        row.put("tipologia", "");
        row.put("fonte", "");
        row.put("totaleUnita", totUnita);
        return row;
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
        // Se non ci sono dati di cessazioni → tabella vuota (i totali sarebbero tutti zero)
        if (allEmpty(cessazioniRows)) {
            result.put("rows", new ArrayList<>());
            return;
        }
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

    /**
     * Riepilogo assunzioni dirigenziale (anno precedente) - nuovo mapping da Minerva:
     * sorgente: {@code reclutamentoAutorizzatoAnnoPrecedente.dati}.
     * Per ogni riga con {@code areaGiuridica} ∈ {DIRIGENTI DI 1^ FASCIA, DIRIGENTI DI 2^ FASCIA}:
     * <ul>
     *   <li>qualifiche      ← areaGiuridica</li>
     *   <li>tipologia       ← tipologiaReclutamento</li>
     *   <li>fonte           ← autorizzazione</li>
     *   <li>totaleUnita     ← ula</li>
     * </ul>
     * In coda due righe di totale: "di cui Dirigenti I fascia" e "di cui Dirigenti II fascia".
     */
    private static void createAssunzioniDirigenzialeAnnoPrecedente(Map<String, Object> result,
                                                                   Map<String, Object> reclutamentoAutorizzato) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("qualifiche", "Qualifiche");
        columns.put("tipologia", "Tipologia di reclutamento");
        columns.put("fonte", "Fonte di finanziamento");
        columns.put("totaleUnita", "Totale unità");
        result.put("columns", columns);

        List<Map<String, Object>> rows = castRows(reclutamentoAutorizzato);
        if (rows.isEmpty()) {
            result.put("rows", new ArrayList<>());
            return;
        }

        List<Map<String, Object>> rowsCustom = new ArrayList<>(rows.size() + 2);
        BigDecimal totaleUnita1 = BigDecimal.ZERO;
        BigDecimal totaleUnita2 = BigDecimal.ZERO;

        for (Map<String, Object> row : rows) {
            String area = String.valueOf(row.get("areaGiuridica"));
            if (!isDirigenteArea(area)) continue;

            BigDecimal ula = toBigDecimalSafe(row.get("ula"));

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("qualifiche", area);
            rowObj.put("tipologia", row.get("tipologiaReclutamento"));
            rowObj.put("fonte", row.get("autorizzazione"));
            rowObj.put("totaleUnita", ula);
            rowsCustom.add(rowObj);

            if (DIRIGENTI_1_FASCIA.equals(area)) {
                totaleUnita1 = totaleUnita1.add(ula);
            } else {
                totaleUnita2 = totaleUnita2.add(ula);
            }
        }

        rowsCustom.add(buildAssunzioneTotaleRow(TOTALE_PRIMA_FASCIA_DIR, totaleUnita1));
        rowsCustom.add(buildAssunzioneTotaleRow(TOTALE_SECONDA_FASCIA_DIR_NO_AREE, totaleUnita2));
        result.put("rows", rowsCustom);
    }

    /**
     * Copertura fabbisogno anno corrente (dirigenziale) - nuovo mapping da Minerva:
     * <ul>
     *   <li>qualifiche       ← reportDotazioneOrganicaERimodulata.dotazioneOrganica.areaGiuridica
     *       filtrato su {DIRIGENTI DI 1^ FASCIA, DIRIGENTI DI 2^ FASCIA}</li>
     *   <li>tipologia        ← reclutamentoAutorizzatoAnnoCorrente.tipologiaReclutamento
     *       (lookup per areaGiuridica)</li>
     *   <li>fonte            ← reportAssunzioniAnnoCorrente.autorizzazione.autorizzazione</li>
     *   <li>totaleUnita      ← reportAssunzioniAnnoCorrente.autorizzazione.dirigenti1 (I fascia)
     *       o dirigenti2 (II fascia)</li>
     *   <li>totaleOneri      ← reportAssunzioniAnno2.oneriFinanziari.oneriAssunzioniTotali
     *       (lookup per areaGiuridica) — come da spec foglio mapping</li>
     * </ul>
     * Per ogni fascia genera N righe quante sono le autorizzazioni; in coda i due subtotali.
     */
    /**
     * Copertura fabbisogno (dirigenziale) - mapping unico per Anno Corrente / Anno1 / Anno2.
     * <ul>
     *   <li>qualifiche   ← reportDotazioneOrganicaERimodulata.dotazioneOrganica.areaGiuridica
     *       filtrato su {DIRIGENTI DI 1^ FASCIA, DIRIGENTI DI 2^ FASCIA}</li>
     *   <li>tipologia    ← reclutamentoAutorizzato&lt;Anno&gt;.tipologiaReclutamento
     *       (lookup per areaGiuridica)</li>
     *   <li>fonte        ← reportAssunzioni&lt;Anno&gt;.autorizzazione.autorizzazione</li>
     *   <li>totaleUnita  ← reportAssunzioni&lt;Anno&gt;.autorizzazione.dirigenti1 (I fascia)
     *       o dirigenti2 (II fascia)</li>
     *   <li>totaleOneri  ← reportAssunzioni&lt;Anno&gt;.oneriFinanziari.oneriAssunzioniTotali
     *       (lookup per areaGiuridica)</li>
     * </ul>
     * Per ogni fascia genera N righe quante sono le autorizzazioni; in coda i due subtotali.
     */
    private static void buildCoperturaFabbisognoDirigenziale(
            Map<String, Object> result,
            Map<String, Object> dotazioneOrganica,
            Map<String, Object> reportAssunzioniAutorizzazione,
            Map<String, Object> reclutamentoAutorizzato,
            Map<String, Object> reportAssunzioniOneriFinanziari) {

        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("qualifiche", "Qualifiche");
        columns.put("tipologia", "Tipologia di reclutamento");
        columns.put("fonte", "Fonte di finanziamento");
        columns.put("totaleUnita", "Totale Unità");
        columns.put("totaleOneri", "Totale oneri assunzionali");
        result.put("columns", columns);

        List<Map<String, Object>> rowsDirigenti = filterDirigenti(castRows(dotazioneOrganica));
        if (rowsDirigenti.isEmpty()) {
            result.put("rows", new ArrayList<>());
            return;
        }

        List<Map<String, Object>> autorizzazioni = castRows(reportAssunzioniAutorizzazione);
        List<Map<String, Object>> reclutamentoRows = castRows(reclutamentoAutorizzato);
        List<Map<String, Object>> oneriRows = castRows(reportAssunzioniOneriFinanziari);

        // Se nessuna sorgente dati ha contenuto reale → tabella vuota
        if (allEmpty(autorizzazioni, reclutamentoRows, oneriRows)) {
            result.put("rows", new ArrayList<>());
            return;
        }

        BigDecimal oneriDir1 = lookupSumByArea(oneriRows, DIRIGENTI_1_FASCIA, "oneriAssunzioniTotali");
        BigDecimal oneriDir2 = lookupSumByArea(oneriRows, DIRIGENTI_2_FASCIA, "oneriAssunzioniTotali");

        List<Map<String, Object>> rowsCustom = new ArrayList<>();
        BigDecimal totaleUnita1 = BigDecimal.ZERO;
        BigDecimal totaleUnita2 = BigDecimal.ZERO;

        for (Map<String, Object> dirRow : rowsDirigenti) {
            String area = String.valueOf(dirRow.get("areaGiuridica"));
            boolean isDir1 = DIRIGENTI_1_FASCIA.equals(area);
            String dirKey = isDir1 ? "dirigenti1" : "dirigenti2";

            // Tipologia di reclutamento: prima riga di reclutamentoAutorizzato matchante per area
            String tipologiaReclutamento = reclutamentoRows.stream()
                    .filter(r -> area.equals(r.get("areaGiuridica")))
                    .map(r -> r.get("tipologiaReclutamento") == null ? "" : String.valueOf(r.get("tipologiaReclutamento")))
                    .findFirst().orElse("");

            BigDecimal oneri = isDir1 ? oneriDir1 : oneriDir2;

            // Una riga per ogni autorizzazione presente
            for (Map<String, Object> autorizzazione : autorizzazioni) {
                BigDecimal unita = toBigDecimalSafe(autorizzazione.get(dirKey));

                Map<String, Object> rowObj = new LinkedHashMap<>();
                rowObj.put("qualifiche", area);
                rowObj.put("tipologia", tipologiaReclutamento);
                rowObj.put("fonte", autorizzazione.get("autorizzazione"));
                rowObj.put("totaleUnita", unita);
                rowObj.put("totaleOneri", scale2(oneri));
                rowsCustom.add(rowObj);

                if (isDir1) totaleUnita1 = totaleUnita1.add(unita);
                else totaleUnita2 = totaleUnita2.add(unita);
            }
        }

        rowsCustom.add(buildCoperturaDirigenzialeRow(TOTALE_PRIMA_FASCIA_DIR, totaleUnita1, oneriDir1));
        rowsCustom.add(buildCoperturaDirigenzialeRow(TOTALE_SECONDA_FASCIA_DIR, totaleUnita2, oneriDir2));
        result.put("rows", rowsCustom);
    }

    /**
     * Copertura fabbisogno aree contrattuali - le righe vengono divise in 3 sezioni:
     * Dirigenti I fascia, Dirigenti II fascia, Non dirigenti. Ciascuna sezione (se non vuota)
     * è seguita da una riga "Totale" che mostra solo la somma degli oneri assunzionali.
     */
    private static void createCoperturaFabbisognoAnnoCorrenteAreeContrattuali(
            Map<String, Object> result,
            Map<String, Object> reclutamentoAutorizzatoAnnoCorrente,
            Map<String, Object> reportAssunzioniAnno2OneriFinanziari) {

        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("areaContrattuale", "Area contrattuale CCNL");
        columns.put("profiliRuolo", "Profili di ruolo");
        columns.put("tipologia", "Tipologia di reclutamento");
        columns.put("fonte", "Fonte di finanziamento");
        columns.put("totaleUnita", "Totale unità");
        columns.put("totaleOneri", "Totale oneri assunzionali");
        result.put("columns", columns);

        List<Map<String, Object>> rows = castRows(reclutamentoAutorizzatoAnnoCorrente);
        if (rows.isEmpty()) {
            result.put("rows", new ArrayList<>());
            return;
        }

        List<Map<String, Object>> oneriRows = castRows(reportAssunzioniAnno2OneriFinanziari);

        // Tre sezioni: Dirigenti 1^ fascia, Dirigenti 2^ fascia, Non dirigenti
        List<Map<String, Object>> sectionDir1 = new ArrayList<>();
        List<Map<String, Object>> sectionDir2 = new ArrayList<>();
        List<Map<String, Object>> sectionNonDir = new ArrayList<>();
        BigDecimal totOneriDir1 = BigDecimal.ZERO;
        BigDecimal totOneriDir2 = BigDecimal.ZERO;
        BigDecimal totOneriNonDir = BigDecimal.ZERO;

        for (Map<String, Object> row : rows) {
            String area = String.valueOf(row.get("areaGiuridica"));
            BigDecimal unita = toBigDecimalSafe(row.get("ula"));
            BigDecimal oneri = lookupSumByArea(oneriRows, area, "oneriAssunzioniTotali");

            Map<String, Object> rowObj = new LinkedHashMap<>();
            rowObj.put("areaContrattuale", area);
            rowObj.put("profiliRuolo", row.get("profiloRuolo"));
            rowObj.put("tipologia", row.get("tipologiaReclutamento"));
            rowObj.put("fonte", row.get("autorizzazione"));
            rowObj.put("totaleUnita", unita);
            rowObj.put("totaleOneri", scale2(oneri));

            if (DIRIGENTI_1_FASCIA.equals(area)) {
                sectionDir1.add(rowObj);
                totOneriDir1 = totOneriDir1.add(oneri);
            } else if (DIRIGENTI_2_FASCIA.equals(area)) {
                sectionDir2.add(rowObj);
                totOneriDir2 = totOneriDir2.add(oneri);
            } else {
                sectionNonDir.add(rowObj);
                totOneriNonDir = totOneriNonDir.add(oneri);
            }
        }

        List<Map<String, Object>> rowsCustom = new ArrayList<>(rows.size() + 4);

        // Sezione Dirigenti 1^ fascia
        if (!sectionDir1.isEmpty()) {
            rowsCustom.addAll(sectionDir1);
            rowsCustom.add(buildAreaContrattualeTotaleRow(TOTALE, totOneriDir1));
        }
        // Sezione Dirigenti 2^ fascia
        if (!sectionDir2.isEmpty()) {
            rowsCustom.addAll(sectionDir2);
            rowsCustom.add(buildAreaContrattualeTotaleRow(TOTALE, totOneriDir2));
        }
        // Sezione Non Dirigenti
        if (!sectionNonDir.isEmpty()) {
            rowsCustom.addAll(sectionNonDir);
            rowsCustom.add(buildAreaContrattualeTotaleRow(TOTALE, totOneriNonDir));
        }

        // Prima del "Totale complessivo" aggiungiamo la riga "Totale aree":
        // somma degli oneri delle sole aree contrattuali (NON dirigenti).
        // "Totale complessivo": somma oneri di TUTTE le sezioni (dirigenti + aree).
        if (!rowsCustom.isEmpty()) {
            rowsCustom.add(buildAreaContrattualeTotaleRow("Totale aree", totOneriNonDir));
            BigDecimal totOneriComplessivo = totOneriDir1.add(totOneriDir2).add(totOneriNonDir);
            rowsCustom.add(buildAreaContrattualeTotaleRow("Totale complessivo assunzioni", totOneriComplessivo));
        }

        result.put("rows", rowsCustom);
    }

    /**
     * Riga "Totale"/"Totale complessivo" per la tabella aree contrattuali PAC:
     * solo somma di {@code totaleOneri}; {@code totaleUnita} e le altre colonne
     * vengono lasciate vuote (come da spec FE PAC).
     */
    private static Map<String, Object> buildAreaContrattualeTotaleRow(String label, BigDecimal totOneri) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("areaContrattuale", label);
        row.put("profiliRuolo", "");
        row.put("tipologia", "");
        row.put("fonte", "");
        row.put("totaleUnita", "");
        row.put("totaleOneri", scale2(totOneri));
        return row;
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

        // Se non ci sono dati di personale → tabella vuota
        if (allEmpty(prRows, coRows, ciRows)) {
            result.put("rows", new ArrayList<>());
            return;
        }

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

        // Se non ci sono dati di personale dirigenziale → tabella vuota
        if (allEmpty(prRows, coRows, tdRows)) {
            result.put("rows", new ArrayList<>());
            return;
        }

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

        // Mostra solo le righe Dirigenti 1^/2^ fascia, poi i totali "di cui ..."
        // (Totale 1^ fascia, Totale 2^ fascia) e il totale complessivo.
        // Allineato a {@link #createDotazioneOrganicaRimodulata}.
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

    /**
     * Ritorna true se TUTTE le liste passate sono null o vuote.
     * Usato dai builder per stabilire se le sorgenti dati effettive non hanno alcun dato
     * (in tal caso la tabella va considerata "vuota" anche se i builder generano righe di totale).
     */
    @SafeVarargs
    private static boolean allEmpty(List<?>... lists) {
        if (lists == null) return true;
        for (List<?> l : lists) {
            if (l != null && !l.isEmpty()) return false;
        }
        return true;
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
