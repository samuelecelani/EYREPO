package it.ey.sync.mapper;

import it.ey.sync.dto.DocumentoPiaoDTO;
import it.ey.sync.entity.DocumentoPiao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = AllegatoPiaoMapper.class)
public interface DocumentoPiaoMapper {

    @Mapping(source = "amministrazione.codiceIpa", target = "codiceIpaRif")
    DocumentoPiaoDTO toDto(DocumentoPiao entity);

    @Mapping(source = "codiceIpaRif", target = "amministrazione.codiceIpa")
    DocumentoPiao toEntity(DocumentoPiaoDTO dto);
}

