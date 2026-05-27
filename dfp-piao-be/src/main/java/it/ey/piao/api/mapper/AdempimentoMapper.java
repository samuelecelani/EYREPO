package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per Adempimento e le sue relazioni (Azione, UlterioriInfo MongoDB).
 * Usa CommonMapper per i mapping comuni (UlterioriInfo, Azione).
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class}
)
public interface AdempimentoMapper {

    /**
     * Mappa Adempimento entity → DTO
     */
    @Mapping(target = "azione", ignore = true) // MongoDB - gestito separatamente
    @Mapping(target = "ulterioriInfo", ignore = true) // MongoDB - gestito separatamente
    @Mapping(target = "idSezione22",source = "sezione22.id")
    AdempimentoDTO toDto(Adempimento entity);

    /**
     * Mappa Adempimento DTO → entity
     */
    @Mapping(target = "sezione22.id",source = "idSezione22") // Gestito manualmente
    Adempimento toEntity(AdempimentoDTO dto);

    /**
     * Mappa lista Adempimento entity → DTO
     */
    List<AdempimentoDTO> toDtoList(List<Adempimento> entities);

    // I metodi per UlterioriInfo e Azione sono ereditati da CommonMapper:
    // - CommonMapper.ulterioriInfoDtoToEntity / ulterioriInfoEntityToDto
    // - CommonMapper.azioneDtoToEntity / azioneEntityToDto
}
