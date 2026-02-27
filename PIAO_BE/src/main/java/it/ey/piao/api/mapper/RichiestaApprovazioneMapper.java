package it.ey.piao.api.mapper;

import it.ey.dto.RichiestaApprovazioneDTO;
import it.ey.entity.RichiestaApprovazione;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;


@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = { CommonMapper.class }
)
public interface RichiestaApprovazioneMapper {


    @Mapping(target = "idPiao", source = "piao.id")
    RichiestaApprovazioneDTO toDto(RichiestaApprovazione entity,  @Context CycleAvoidingMappingContext context);



    @Mapping(target = "piao.id", source = "idPiao")
    RichiestaApprovazione toEntity(RichiestaApprovazioneDTO dto,@Context CycleAvoidingMappingContext context);

}
