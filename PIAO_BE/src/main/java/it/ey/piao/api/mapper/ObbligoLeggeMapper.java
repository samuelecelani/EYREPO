package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per ObbligoLegge e le sue relazioni (DatiPubblicati, UlterioriInfo MongoDB).
 * Usa CommonMapper per i mapping comuni (UlterioriInfo).
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class, DatiPubblicatiMapper.class}
)
public interface ObbligoLeggeMapper {


    @Mapping(target = "idSezione23", source = "sezione23.id")
    @Mapping(target = "datiPubblicati", source = "datiPubblicati")
    ObbligoLeggeDTO toDto(ObbligoLegge entity,@Context CycleAvoidingMappingContext context);

    /**
     * DTO → Entity con context
     */
    @Mapping(target = "sezione23", ignore = true) // gestito manualmente nel service
    @Mapping(target = "datiPubblicati", ignore = true) // lista gestita manualmente se serve
    @Mapping(target = "createdTs", ignore = true)
    @Mapping(target = "updatedTs", ignore = true)
    ObbligoLegge toEntity(ObbligoLeggeDTO dto, @Context CycleAvoidingMappingContext context);



    /**
     * Lista Entity → DTO
     */
    List<ObbligoLeggeDTO> toDtoList(List<ObbligoLegge> entity,@Context CycleAvoidingMappingContext context);
}
