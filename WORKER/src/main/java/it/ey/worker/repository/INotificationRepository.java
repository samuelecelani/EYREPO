package it.ey.worker.repository;

import it.ey.worker.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface INotificationRepository extends JpaRepository<Notification, Long> {
    public List<Notification> findAllByIdModulo(String modulo);

}
