package it.ey.piao.api.mapper;

import it.ey.entity.Piao;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper di supporto (riusato via `uses`) per conversioni comuni NON Mongo.
 * Serve per mappare idPiao <-> Piao senza riscrivere logica.
 */


@Mapper(componentModel = "spring",
unmappedTargetPolicy = ReportingPolicy.IGNORE,
nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
uses ={ CommonMapper.class})
public interface SezioneBaseMapper {


}
