package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per StakeHolder.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface StakeHolderMapper {

    /**
     * Mappa StakeHolder entity → DTO
     */
    @Mapping(target = "idPiao", source = "piao.id")
    StakeHolderDTO toDto(StakeHolder entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa StakeHolderDTO → StakeHolder entity
     */
    @Mapping(target = "piao.id", source = "idPiao") // Gestito manualmente
    StakeHolder toEntity(StakeHolderDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista StakeHolder entity → DTO
     */
    List<StakeHolderDTO> toDtoList(List<StakeHolder> entities, @Context CycleAvoidingMappingContext context);
List<StakeHolder> toEntityList(List<StakeHolderDTO> dtos, @Context CycleAvoidingMappingContext context);

}
