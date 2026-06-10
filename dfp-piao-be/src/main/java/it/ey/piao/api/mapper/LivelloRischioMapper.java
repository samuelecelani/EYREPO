package it.ey.piao.api.mapper;

import it.ey.dto.LivelloRischioDTO;
import it.ey.entity.LivelloRischio;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class}
)
public interface LivelloRischioMapper {

    LivelloRischioDTO toDto(LivelloRischio entity,@Context CycleAvoidingMappingContext context);

     LivelloRischio toEntity(LivelloRischioDTO entity,@Context CycleAvoidingMappingContext context);
}
