package it.ey.piao.api.mapper;

import it.ey.dto.AvvisoDTO;
import it.ey.entity.Avviso;
import it.ey.enums.StatoAvviso;
import it.ey.enums.TipologiaContenuto;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class},
    imports = {StatoAvviso.class, TipologiaContenuto.class}
)
public interface AvvisoMapper {

    @Mapping(target = "tipologiaContenuto", expression = "java(entity.getTipologiaContenuto() != null ? entity.getTipologiaContenuto().name() : null)")
    @Mapping(target = "stato", expression = "java(entity.getStato() != null ? entity.getStato().name() : null)")
    AvvisoDTO toDto(Avviso entity, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "tipologiaContenuto", expression = "java(dto.getTipologiaContenuto() != null ? TipologiaContenuto.valueOf(dto.getTipologiaContenuto()) : null)")
    @Mapping(target = "stato", expression = "java(dto.getStato() != null ? StatoAvviso.valueOf(dto.getStato()) : null)")
    Avviso toEntity(AvvisoDTO dto, @Context CycleAvoidingMappingContext context);

    List<AvvisoDTO> toDtoList(List<Avviso> entityList, @Context CycleAvoidingMappingContext context);

    List<Avviso> toEntityList(List<AvvisoDTO> dtoList, @Context CycleAvoidingMappingContext context);
}
