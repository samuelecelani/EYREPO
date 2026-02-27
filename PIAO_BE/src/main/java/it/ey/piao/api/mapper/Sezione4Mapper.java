package it.ey.piao.api.mapper;

import it.ey.dto.Sezione4DTO;
import it.ey.entity.Sezione4;
import it.ey.entity.SottofaseMonitoraggio;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses ={ CommonMapper.class, SottofaseMonitoraggioMapper.class, CategoriaObiettiviMapper.class}

)
public interface Sezione4Mapper {

    @Mapping(source = "piao.id", target = "idPiao")
    @Mapping(target = "sottofaseMonitoraggio", ignore = true)
    @Mapping(target = "categoriaObiettivi", ignore = true)
    @Mapping(target = "attore", ignore = true)
    Sezione4DTO toDto(Sezione4 entity, @Context CycleAvoidingMappingContext context);

    @Mapping(source = "idPiao", target = "piao.id")
    @Mapping(target = "sottofaseMonitoraggio", ignore = true)
    @Mapping(target = "categoriaObiettivi", ignore = true)
    Sezione4 toEntity(Sezione4DTO dto, @Context CycleAvoidingMappingContext context);



}
