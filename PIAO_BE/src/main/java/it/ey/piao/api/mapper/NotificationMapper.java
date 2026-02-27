package it.ey.piao.api.mapper;

import it.ey.dto.NotificationDTO;
import it.ey.entity.Notification;
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

    NotificationDTO toDto(Notification entity);

    @Mapping(target = "id", ignore = true)
    Notification toEntity(NotificationDTO dto);

    List<NotificationDTO> toDtoList(List<Notification> entities);
}
