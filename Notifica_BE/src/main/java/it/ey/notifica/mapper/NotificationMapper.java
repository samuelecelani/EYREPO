package it.ey.notifica.mapper;

import it.ey.notifica.dto.NotificationDTO;
import it.ey.notifica.entity.Notification;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per Notification entity ↔ NotificationDTO.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface NotificationMapper {

    /**
     * Mappa Notification entity → NotificationDTO
     */
    NotificationDTO toDto(Notification entity);

    /**
     * Mappa NotificationDTO → Notification entity.
     * Ignora l'ID: JPA genera l'ID dalla sequence (INSERT, non merge).
     */
    @Mapping(target = "id", ignore = true)
    Notification toEntity(NotificationDTO dto);

    /**
     * Mappa lista Notification entity → lista NotificationDTO
     */
    List<NotificationDTO> toDtoList(List<Notification> entities);

    /**
     * Mappa lista NotificationDTO → lista Notification entity
     */
    List<Notification> toEntityList(List<NotificationDTO> dtos);



}
