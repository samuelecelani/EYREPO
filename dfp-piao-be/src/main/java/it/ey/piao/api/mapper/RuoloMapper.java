package it.ey.piao.api.mapper;

import it.ey.dto.RichiestaApprovazioneDTO;
import it.ey.dto.RuoloDTO;
import it.ey.entity.RichiestaApprovazione;
import it.ey.entity.Ruolo;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = { CommonMapper.class }
)
public interface RuoloMapper {

    RuoloDTO toDto(Ruolo entity, @Context CycleAvoidingMappingContext context);

    Ruolo toEntity(RuoloDTO dto,@Context CycleAvoidingMappingContext context);

    List<RuoloDTO> toDtoList(List<Ruolo> entities, @Context CycleAvoidingMappingContext context);
}
