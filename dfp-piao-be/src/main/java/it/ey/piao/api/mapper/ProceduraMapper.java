package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per Procedura.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProceduraMapper {

    /**
     * Mappa Procedura entity → DTO
     */
    @Mapping(target = "idSezione21", source = "sezione21.id")// Evita riferimenti circolari
    ProceduraDTO toDto(Procedura entity,@Context CycleAvoidingMappingContext context);

    /**
     * Mappa ProceduraDTO → Procedura entity
     */
    @Mapping(target = "sezione21.id", source = "idSezione21")
    Procedura toEntity(ProceduraDTO dto,@Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista Procedura entity → DTO
     */
    List<ProceduraDTO> toDtoList(List<Procedura> entities, @Context CycleAvoidingMappingContext context);
    List<Procedura> toEntityList(List<ProceduraDTO> dtos, @Context CycleAvoidingMappingContext context);
}
