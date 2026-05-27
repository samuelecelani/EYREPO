package it.ey.piao.api.mapper;

import it.ey.dto.StorageMinervaDTO;
import it.ey.entity.StorageMinerva;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface StorageMinervaMapper
{
    StorageMinervaDTO toDto(StorageMinerva entity);

    StorageMinerva toEntity(StorageMinervaDTO dto);

    List<StorageMinervaDTO> toDtoList(List<StorageMinerva> entities);

    void updateEntityFromDto(StorageMinervaDTO dto, @MappingTarget StorageMinerva entity);
}

