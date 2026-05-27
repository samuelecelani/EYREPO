package it.ey.piao.api.mapper;

import it.ey.dto.AutoritaApprovatoreDTO;
import it.ey.entity.AutoritaApprovatore;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class}
)
public interface AutoritaApprovatoreMapper {
    AutoritaApprovatoreDTO toDto(AutoritaApprovatore entity, @Context CycleAvoidingMappingContext context);

    AutoritaApprovatore toEntity(AutoritaApprovatoreDTO dto, @Context CycleAvoidingMappingContext context);

    List<AutoritaApprovatoreDTO> toDtoList(List<AutoritaApprovatore> entityList, @Context CycleAvoidingMappingContext context);

    List<AutoritaApprovatore> toEntityList(List<AutoritaApprovatoreDTO> dtoList, @Context CycleAvoidingMappingContext context);
}
