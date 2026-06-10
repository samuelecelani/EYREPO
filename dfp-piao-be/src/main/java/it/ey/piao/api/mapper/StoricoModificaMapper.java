package it.ey.piao.api.mapper;

import it.ey.dto.StoricoModificaDTO;
import it.ey.entity.StoricoModifica;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per StoricoModifica entity ↔ StoricoModificaDTO.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface StoricoModificaMapper {

    /**
     * Mappa StoricoModifica entity → StoricoModificaDTO
     */
    @Mapping(source = "piao.id", target = "idPiao")
    StoricoModificaDTO toDto(StoricoModifica entity);

    /**
     * Mappa StoricoModificaDTO → StoricoModifica entity
     */
    @Mapping(source = "idPiao", target = "piao.id")
    @Mapping(target = "createdTs", ignore = true)
    @Mapping(target = "updatedTs", ignore = true)
    StoricoModifica toEntity(StoricoModificaDTO dto);

    /**
     * Mappa lista StoricoModifica entity → lista StoricoModificaDTO
     */
    List<StoricoModificaDTO> toDtoList(List<StoricoModifica> entities);
}
