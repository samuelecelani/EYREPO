package it.ey.piao.api.mapper;

import it.ey.dto.StoricoDichiarazioneDFPDTO;
import it.ey.entity.DichiarazioneScadenza;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface StoricoDichiarazioneDFPMapper
{
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codePA", source = "piao.codPAFK")
    @Mapping(target = "amministrazione", source = "piao.denominazionePA")
    @Mapping(target = "dataRicezione", source = "createdTs")
    @Mapping(target = "dataPrevistaPubblicazione", source = "dataPubblicazione")
    @Mapping(target = "stato", source = "stato")
    StoricoDichiarazioneDFPDTO toDto(DichiarazioneScadenza entity);

    List<StoricoDichiarazioneDFPDTO> toDtoList(List<DichiarazioneScadenza> entities);
}

