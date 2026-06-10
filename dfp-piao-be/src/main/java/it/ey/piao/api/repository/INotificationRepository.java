package it.ey.piao.api.repository;

import it.ey.entity.Notification;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface INotificationRepository extends BaseRepository<Notification, Long> {
    public List<Notification> findAllByIdModulo(String modulo);
}
