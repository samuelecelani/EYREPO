package it.ey.piao.api.mapper;

import it.ey.dto.FondiEuropeiDTO;
import it.ey.entity.FondiEuropei;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OVPStrategiaMapper.class, StakeHolderMapper.class}
)
public interface FondiEuropeiMapper {

    @Mapping(target = "sezione21.id", source = "idSezione21") // Mappa idSezione21 da sezione21.id
    FondiEuropei fondiEuropeiDtoToEntity(FondiEuropeiDTO dto, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "idSezione21", source = "sezione21.id")
    FondiEuropeiDTO fondiEuropeiEntityToDto(FondiEuropei entity, @Context CycleAvoidingMappingContext context);

List<FondiEuropeiDTO> fondiEuropeiEntityListToDtoList(List<FondiEuropei> entities, @Context CycleAvoidingMappingContext context);
List<FondiEuropei> fondiEuropeiDtoListToEntityList(List<FondiEuropeiDTO> dtos, @Context CycleAvoidingMappingContext context);

}
