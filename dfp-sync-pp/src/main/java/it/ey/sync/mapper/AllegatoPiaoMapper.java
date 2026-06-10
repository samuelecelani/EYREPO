package it.ey.sync.mapper;

import it.ey.sync.dto.AllegatoPiaoDTO;
import it.ey.sync.entity.AllegatoPiao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AllegatoPiaoMapper {

    @Mapping(source = "documentoPiao.id", target = "idPiao")
    AllegatoPiaoDTO toDto(AllegatoPiao entity);

    @Mapping(source = "idPiao", target = "documentoPiao.id")
    AllegatoPiao toEntity(AllegatoPiaoDTO dto);
}

