package it.ey.piao.api.mapper;

import it.ey.dto.TabellaFunzionaleDTO;
import it.ey.entity.TabellaFunzionale;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per TabellaFunzionale.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TabellaFunzionaleMapper {

    /**
     * Mappa TabellaFunzionale entity → DTO
     */
    @Mapping(target = "idOVP", source = "ovp.id")
    @Mapping(target = "idStakeholder", source = "stakeholder.id")
    TabellaFunzionaleDTO toDto(TabellaFunzionale entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa TabellaFunzionaleDTO → TabellaFunzionale entity
     */
    @Mapping(target = "ovp.id", source = "idOVP")
    @Mapping(target = "stakeholder.id", source = "idStakeholder")
    TabellaFunzionale toEntity(TabellaFunzionaleDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista TabellaFunzionale entity → DTO
     */
    List<TabellaFunzionaleDTO> toDtoList(List<TabellaFunzionale> entities, @Context CycleAvoidingMappingContext context);

    List<TabellaFunzionale> toEntityList(List<TabellaFunzionaleDTO> dtos, @Context CycleAvoidingMappingContext context);
}
