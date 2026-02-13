package it.ey.piao.api.mapper;

import it.ey.dto.DatiPubblicatiDTO;
import it.ey.entity.DatiPubblicati;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class, }
)
public interface DatiPubblicatiMapper {
    @Mapping(source = "obbligoLegge.id", target = "idObbligoLegge")
    @Mapping(target = "ulterioriInfo", ignore = true) // Mongo gestito nel service
    DatiPubblicatiDTO toDTO(DatiPubblicati entity);


    @Mapping(source = "idObbligoLegge", target = "obbligoLegge.id")
    DatiPubblicati toEntity(DatiPubblicatiDTO dto);

}
