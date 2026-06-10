package it.ey.sync.mapper;

import it.ey.sync.dto.AmministrazioneDTO;
import it.ey.sync.entity.Amministrazione;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AmministrazioneMapper {

    AmministrazioneDTO toDto(Amministrazione entity);

    Amministrazione toEntity(AmministrazioneDTO dto);
}

