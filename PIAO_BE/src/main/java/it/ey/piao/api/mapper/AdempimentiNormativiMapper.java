package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per AdempimentiNormativi.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AdempimentiNormativiMapper {

    /**
     * Mappa AdempimentiNormativi entity → DTO
     */
    @Mapping(target = "idSezione23", source = "sezione23.id")
    AdempimentiNormativiDTO toDto(AdempimentiNormativi entity,@Context CycleAvoidingMappingContext context);

    /**
     * Mappa AdempimentiNormativiDTO → AdempimentiNormativi entity
     */
    @Mapping(target = "sezione23.id", source = "idSezione23") // Gestito manualmente
    AdempimentiNormativi toEntity(AdempimentiNormativiDTO dto,@Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista AdempimentiNormativi entity → DTO
     */
    List<AdempimentiNormativiDTO> toDtoList(List<AdempimentiNormativi> entities,@Context CycleAvoidingMappingContext context);
    List<AdempimentiNormativi>toEntityList(List<AdempimentiNormativiDTO> dtos,@Context CycleAvoidingMappingContext context);
}
