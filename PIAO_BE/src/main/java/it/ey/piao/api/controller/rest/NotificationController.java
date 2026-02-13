package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.NotificationDTO;
import it.ey.piao.api.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//Rest Controller per gestione delle notifiche/messaggi su code. Viene invocato dal BFF

@ApiV1Controller("/notification")
public class NotificationController {

    private final INotificationService notificationService;

    @Autowired
    public NotificationController(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // GET /worker/notification
    @GetMapping("/subscribe")
    public ResponseEntity<List<NotificationDTO>> getNotifications(@RequestParam("idModulo")  String idModulo) {
        List<NotificationDTO> notifications = notificationService.getNotifications(idModulo);
        return ResponseEntity.ok(notifications);
    }

    // PUT /worker/notification/read
    @PutMapping("/readNotify")
    public ResponseEntity<Void> readNotification(@RequestBody NotificationDTO notificationDTO) {
        notificationService.readNotifications(notificationDTO);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
    // PUT /worker/notification/read
    @PutMapping("/unreadNotify")
    public ResponseEntity<Void> unreadNotification(@RequestBody NotificationDTO notificationDTO) {
        notificationService.unreadNotification(notificationDTO);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
