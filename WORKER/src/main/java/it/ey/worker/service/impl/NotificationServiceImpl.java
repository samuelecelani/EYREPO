package it.ey.worker.service.impl;

import it.ey.dto.NotificationDTO;
import it.ey.entity.Notification;
import it.ey.worker.repository.INotificationRepository;
import it.ey.worker.service.INotificationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements INotificationService {

    private final INotificationRepository notificationRepository;

    @Autowired
    public NotificationServiceImpl(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }



    @Override
        public void saveNotification(NotificationDTO notificationDTO) {
            try {
                notificationRepository.save(new Notification(notificationDTO));
            }catch (Exception e){
            }
        }
        @Override
        public List<NotificationDTO> getNotifications(String idModulo) {
        List<NotificationDTO> notificationDTOList = new ArrayList<>();
            try {
                List<Notification> notifications = notificationRepository.findAllByIdModulo(idModulo);
                for(Notification notification : notifications) {
                    notificationDTOList.add(new NotificationDTO(notification));
                }
            }catch (Exception e){

            }
            return notificationDTOList;
        }


    @Override
    public void readNotifications(NotificationDTO notificationDTO) {
        try {
            Notification notification = notificationRepository.findById(notificationDTO.getId()).orElse(null);
            if (notification != null) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        } catch (Exception e) {

        }
    }

}
