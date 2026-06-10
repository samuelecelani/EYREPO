package it.ey.piao.api.mapper;

import it.ey.dto.external.*;
import it.ey.entity.*;
import it.ey.enums.TipologiaObbiettivo;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper per la conversione delle entity PIAO nelle DTO External.
 * Utilizzato per l'esposizione esterna dei dati PIAO.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PiaoExternalMapper {

    /**
     * Mappa Anagrafica entity a AnagraficaExternalDTO.
     */
    @Mapping(target = "social", source = "social")
    AnagraficaExternalDTO toAnagraficaExternalDTO(Anagrafica anagrafica, Social social, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa Social entity (MongoDB) a SocialExternalDTO.
     */
    SocialExternalDTO toSocialExternalDTO(Social social, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa OVP entity a OvpExternalDTO.
     */
    @Mapping(target = "ovpStrategias", source = "ovpStrategias")
    OvpExternalDTO toOvpExternalDTO(OVP ovp, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista OVP a lista OvpExternalDTO.
     */
    List<OvpExternalDTO> toOvpExternalDTOList(List<OVP> ovpList, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa OVPStrategia entity a OvpStrategiaExternalDTO.
     */
    @Mapping(target = "indicatori", source = "indicatori")
    @Mapping(target = "obbiettivoPerformance", ignore = true)
    @Mapping(target = "obbiettivoDiPienaAccessibilitaDigitale", ignore = true)
    @Mapping(target = "obbiettivoDiPienaAccessibilitaFisica", ignore = true)
    @Mapping(target = "obbiettivoDiSemplificazione", ignore = true)
    @Mapping(target = "obbiettivoDiPariOpportunita", ignore = true)
    @Mapping(target = "obbiettivoDiPerformanceOrganizzativa", ignore = true)
    @Mapping(target = "obbiettivoDiPerformanceIndividuale", ignore = true)
    OvpStrategiaExternalDTO toOvpStrategiaExternalDTO(OVPStrategia ovpStrategia, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista OVPStrategia a lista OvpStrategiaExternalDTO.
     */
    List<OvpStrategiaExternalDTO> toOvpStrategiaExternalDTOList(List<OVPStrategia> ovpStrategiaList, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa OVPStrategiaIndicatore entity a IndicatoreExternalDTO.
     */
    @Mapping(target = "denominazione", source = "indicatore.denominazione")
    @Mapping(target = "unitaMisura", source = "indicatore.unitaMisura")
    @Mapping(target = "peso", source = "indicatore.peso")
    @Mapping(target = "polarita", source = "indicatore.polarita")
    @Mapping(target = "baseLine", source = "indicatore.baseLine")
    @Mapping(target = "fonteDati", source = "indicatore.fonteDati")
    @Mapping(target = "codTipologiaFK", source = "indicatore.codTipologiaFK")
    @Mapping(target = "tipAndValAnnoCorrente", source = "indicatore.tipAndValAnnoCorrente")
    @Mapping(target = "tipAndValAnno1", source = "indicatore.tipAndValAnno1")
    @Mapping(target = "tipAndValAnno2", source = "indicatore.tipAndValAnno2")
    IndicatoreExternalDTO toIndicatoreExternalDTO(OVPStrategiaIndicatore ovpStrategiaIndicatore, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista OVPStrategiaIndicatore a lista IndicatoreExternalDTO.
     */
    List<IndicatoreExternalDTO> toIndicatoreExternalDTOList(List<OVPStrategiaIndicatore> indicatoreList, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa ObbiettivoPerformance entity a ObiettivoExternalDTO.
     */
    @Mapping(target = "tipologia", source = "tipologia")
    @Mapping(target = "indicatori", ignore = true)
    ObiettivoExternalDTO toObiettivoExternalDTO(ObbiettivoPerformance obbiettivoPerformance, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista ObbiettivoPerformance a lista ObiettivoExternalDTO.
     */
    List<ObiettivoExternalDTO> toObiettivoExternalDTOList(List<ObbiettivoPerformance> obbiettiviPerformance, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa AmpiezzaOrganizzativa a PopolazioneSuddivisaEtaExternalDTO.
     */
    @Mapping(target = "descrizione", source = "unitaOrganizzativa")
    @Mapping(target = "value", expression = "java(parseNumRisorseUmane(ampiezzaOrganizzativa.getNumRisorseUmane()))")
    PopolazioneSuddivisaEtaExternalDTO toPopolazioneSuddivisaEtaExternalDTO(AmpiezzaOrganizzativa ampiezzaOrganizzativa, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista AmpiezzaOrganizzativa a lista PopolazioneSuddivisaEtaExternalDTO.
     */
    List<PopolazioneSuddivisaEtaExternalDTO> toPopolazioneSuddivisaEtaExternalDTOList(List<AmpiezzaOrganizzativa> ampiezzaOrganizzativaList, @Context CycleAvoidingMappingContext context);

    /**
     * Converte una stringa in Integer per il campo numRisorseUmane.
     */
    default Integer parseNumRisorseUmane(String numRisorseUmane) {
        if (numRisorseUmane == null || numRisorseUmane.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(numRisorseUmane.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converte TipologiaObbiettivo enum a String.
     */
    default String tipologiaObbiettivoToString(TipologiaObbiettivo tipologia) {
        return tipologia != null ? tipologia.name() : null;
    }

    /**
     * Mappa OvpStrategiaExternalDTO con gli obiettivi raggruppati per tipologia.
     */
    @AfterMapping
    default void mapObbiettiviByTipologia(@MappingTarget OvpStrategiaExternalDTO dto,
                                          OVPStrategia ovpStrategia,
                                          @Context CycleAvoidingMappingContext context) {
        if (ovpStrategia.getObbiettiviPerformance() == null || ovpStrategia.getObbiettiviPerformance().isEmpty()) {
            return;
        }

        List<ObbiettivoPerformance> obiettivi = ovpStrategia.getObbiettiviPerformance();

        dto.setObbiettivoPerformance(filterAndMapByTipologia(obiettivi, TipologiaObbiettivo.PERFORMANCE, context));
        dto.setObbiettivoDiPienaAccessibilitaDigitale(filterAndMapByTipologia(obiettivi, TipologiaObbiettivo.ACCESSI_DIGITALE, context));
        dto.setObbiettivoDiPienaAccessibilitaFisica(filterAndMapByTipologia(obiettivi, TipologiaObbiettivo.ACCESSI_FISICI, context));
        dto.setObbiettivoDiSemplificazione(filterAndMapByTipologia(obiettivi, TipologiaObbiettivo.SEMPLIFICAZIONE, context));
        dto.setObbiettivoDiPariOpportunita(filterAndMapByTipologia(obiettivi, TipologiaObbiettivo.PARI_OPPORTUNITA, context));
        dto.setObbiettivoDiPerformanceOrganizzativa(filterAndMapByTipologia(obiettivi, TipologiaObbiettivo.PERFORMANCE_ORGANIZZATIVA, context));
        dto.setObbiettivoDiPerformanceIndividuale(filterAndMapByTipologia(obiettivi, TipologiaObbiettivo.PERFORMANCE_INDIVIDUALE, context));
    }

    /**
     * Filtra e mappa gli obiettivi per tipologia.
     */
    default List<ObiettivoExternalDTO> filterAndMapByTipologia(List<ObbiettivoPerformance> obiettivi,
                                                                TipologiaObbiettivo tipologia,
                                                                CycleAvoidingMappingContext context) {
        if (obiettivi == null) {
            return new ArrayList<>();
        }
        return obiettivi.stream()
            .filter(o -> o.getTipologia() == tipologia)
            .map(o -> toObiettivoExternalDTO(o, context))
            .collect(Collectors.toList());
    }
}
