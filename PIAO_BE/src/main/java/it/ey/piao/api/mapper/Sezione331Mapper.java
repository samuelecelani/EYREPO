package it.ey.piao.api.mapper;

import it.ey.dto.Sezione331DTO;
import it.ey.entity.Sezione331;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class}
)

public interface Sezione331Mapper {

    @Mapping(target = "idPiao", source = "piao.id")

    @Mapping(target = "statoSezione", ignore = true)
    Sezione331DTO toDto(Sezione331 entity, @Context CycleAvoidingMappingContext context);



    @Mapping(target = "piao.id", source = "idPiao")

    @Mapping(target = "idStato", ignore = true)
    Sezione331 toEntity(Sezione331DTO dto, @Context CycleAvoidingMappingContext context);
}
