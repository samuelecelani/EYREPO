package it.ey.notifica.repository;

import it.ey.notifica.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface INotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByIdModulo(String modulo);

    List<Notification> findAllByIdModuloAndRuoloAndCodicePa(String idModulo, String ruolo, String codicePa);

    List<Notification> findAllByIdModuloAndRuoloAndCodicePaAndCodiceFiscale(String idModulo, String ruolo, String codicePa, String codiceFiscale);
}
