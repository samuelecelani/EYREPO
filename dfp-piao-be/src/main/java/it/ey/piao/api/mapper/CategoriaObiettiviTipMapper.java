package it.ey.piao.api.mapper;

import it.ey.dto.CategoriaObiettiviTipDTO;
import it.ey.entity.CategoriaObiettiviTip;
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
public interface CategoriaObiettiviTipMapper {

    CategoriaObiettiviTipDTO toDto(CategoriaObiettiviTip entity, @Context CycleAvoidingMappingContext context);

    CategoriaObiettiviTip toEntity(CategoriaObiettiviTipDTO entity,@Context CycleAvoidingMappingContext context);
}
