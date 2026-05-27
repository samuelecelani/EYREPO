package it.ey.piao.api.mapper;

import it.ey.dto.StoricoStatoSezioneDTO;
import it.ey.entity.StoricoStatoSezione;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per StoricoStatoSezione entity ↔ StoricoStatoSezioneDTO.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface StoricoStatoSezioneMapper {

    /**
     * Mappa StoricoStatoSezione entity → StoricoStatoSezioneDTO
     */
    @Mapping(source = "statoSezione.testo", target = "testoStato")
    StoricoStatoSezioneDTO toDto(StoricoStatoSezione entity);

    /**
     * Mappa StoricoStatoSezioneDTO → StoricoStatoSezione entity (senza statoSezione, verrà valorizzato nel service)
     */
    @Mapping(target = "statoSezione", ignore = true)
    @Mapping(target = "createdTs", ignore = true)
    @Mapping(target = "updatedTs", ignore = true)
    StoricoStatoSezione toEntity(StoricoStatoSezioneDTO dto);

    /**
     * Mappa lista StoricoStatoSezione entity → lista StoricoStatoSezioneDTO
     */
    List<StoricoStatoSezioneDTO> toDtoList(List<StoricoStatoSezione> entities);
}
