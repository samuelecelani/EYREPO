package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per UtenteRuoliPaSezione e StrutturaPiao.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UtenteRuoloPaMapper {

    // ========== UtenteRuoliPaSezione ==========

    @Mapping(target = "strutturaPiao", source = "strutturaPiao")
    @Mapping(target = "externalUserId", source = "externalUserId")
    @Mapping(target = "codiceRuolo", source = "ruolo.codRuolo")
    @Mapping(target = "descrizioneRuolo", source = "ruolo.descrizione")
    UtenteRuoliPaSezioneDTO sezioneToDto(UtenteRuoliPaSezione entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ruolo", ignore = true)
    UtenteRuoliPaSezione sezioneDtoToEntity(UtenteRuoliPaSezioneDTO dto);

    List<UtenteRuoliPaSezioneDTO> sezioniToDtoList(List<UtenteRuoliPaSezione> entities);

    // ========== StrutturaPiao ==========

    @Mapping(target = "children", ignore = true)
    @Mapping(target = "statoSezione", ignore = true)
    @Mapping(target = "updatedTs", ignore = true)
    @Mapping(target = "statoPiao", ignore = true)
    StrutturaPiaoDTO strutturaPiaoToDto(StrutturaPiao entity);
}
