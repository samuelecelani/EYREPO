package it.ey.piao.api.mapper;

import it.ey.dto.DatiPubblicatiDTO;
import it.ey.entity.DatiPubblicati;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class, }
)
public interface DatiPubblicatiMapper {
    @Mapping(source = "obbligoLegge.id", target = "idObbligoLegge")
    @Mapping(target = "ulterioriInfo", ignore = true) // Mongo gestito nel service
    DatiPubblicatiDTO toDTO(DatiPubblicati entity, @Context CycleAvoidingMappingContext context);


    @Mapping(source = "idObbligoLegge", target = "obbligoLegge.id")
    DatiPubblicati toEntity(DatiPubblicatiDTO dto, @Context CycleAvoidingMappingContext context);

}
