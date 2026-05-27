package it.ey.piao.api.mapper;

import it.ey.dto.RichiestaApprovazioneDTO;
import it.ey.entity.RichiestaApprovazione;
import it.ey.enums.StatoEnum;
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
    @Mapping(target = "statoPiao", source = "piao.idStato", qualifiedByName = "idStatoToDescrizione")
    RichiestaApprovazioneDTO toDto(RichiestaApprovazione entity,  @Context CycleAvoidingMappingContext context);



    @Mapping(target = "piao.id", source = "idPiao")
    RichiestaApprovazione toEntity(RichiestaApprovazioneDTO dto,@Context CycleAvoidingMappingContext context);

    @Named("idStatoToDescrizione")
    default String idStatoToDescrizione(Long idStato) {
        if (idStato == null) return null;
        try {
            return StatoEnum.fromId(idStato).getDescrizione();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
