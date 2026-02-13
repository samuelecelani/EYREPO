package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per UtenteRuoloPa e le sue relazioni.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UtenteRuoloPaMapper {

    /**
     * Mappa UtenteRuoloPa entity → DTO
     */
    @Mapping(target = "sezioni", source = "sezioni")
    @Mapping(target = "codicePA", source = "codicePA")
    @Mapping(target = "ruoli", source = "ruoli")
    UtenteRuoloPaDTO toDto(UtenteRuoloPa entity);

    /**
     * Mappa UtenteRuoloPaDTO → UtenteRuoloPa entity
     * NOTA: Le liste di sezioni, codicePA e ruoli devono essere gestite manualmente
     * per impostare le back-reference correttamente
     */
    @Mapping(target = "sezioni", ignore = true)  // Gestito manualmente per back-reference
    @Mapping(target = "codicePA", ignore = true) // Gestito manualmente per back-reference
    @Mapping(target = "ruoli", ignore = true)    // Gestito manualmente per back-reference
    UtenteRuoloPa toEntity(UtenteRuoloPaDTO dto);

    /**
     * Mappa lista UtenteRuoloPa entity → DTO
     */
    List<UtenteRuoloPaDTO> toDtoList(List<UtenteRuoloPa> entities);

    // ========== UtenteRuoliPaSezione ==========

    @Mapping(target = "strutturaPiao", source = "strutturaPiao")
    UtenteRuoliPaSezioneDTO sezioneToDto(UtenteRuoliPaSezione entity);

    @Mapping(target = "utenteRuoloPa", ignore = true) // Back-reference gestita manualmente
    UtenteRuoliPaSezione sezioneDtoToEntity(UtenteRuoliPaSezioneDTO dto);

    // ========== UtentePa ==========

    UtentePaDTO utentePaToDto(UtentePa entity);

    @Mapping(target = "utente", ignore = true) // Back-reference gestita manualmente
    UtentePa utentePaDtoToEntity(UtentePaDTO dto);

    // ========== RuoloUtente ==========

    RuoloUtenteDTO ruoloUtenteToDto(RuoloUtente entity);

    @Mapping(target = "utente", ignore = true) // Back-reference gestita manualmente
    RuoloUtente ruoloUtenteDtoToEntity(RuoloUtenteDTO dto);

    // ========== StrutturaPiao ==========

    @Mapping(target = "children", ignore = true)
    @Mapping(target = "statoSezione", ignore = true)
    @Mapping(target = "updatedTs", ignore = true)
    StrutturaPiaoDTO strutturaPiaoToDto(StrutturaPiao entity);
}
