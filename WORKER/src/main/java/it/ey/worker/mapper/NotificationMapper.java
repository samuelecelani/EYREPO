package it.ey.worker.mapper;

import it.ey.worker.dto.NotificationDTO;
import it.ey.worker.entity.Notification;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per Notification entity ↔ NotificationDTO.
 * Ignora l'ID in fase di toEntity per evitare merge su righe inesistenti
 * (l'ID viene generato dalla sequence del DB).
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface NotificationMapper {

    /**
     * Entity → DTO (include l'ID per la risposta)
     */
    NotificationDTO toDto(Notification entity);

    /**
     * DTO → Entity per INSERT: ignora l'ID così JPA usa la sequence.
     */
    @Mapping(target = "id", ignore = true)
    Notification toEntity(NotificationDTO dto);

    List<NotificationDTO> toDtoList(List<Notification> entities);

    List<Notification> toEntityList(List<NotificationDTO> dtos);
}
