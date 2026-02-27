package it.ey.worker.notification;

import it.ey.worker.dto.EmailMessageDTO;
import it.ey.worker.dto.NotificationDTO;
import it.ey.worker.utils.WorkerUtil;


import java.io.IOException;

public class EmailNotification extends BaseNotification {




    public EmailNotification() {
        super();
    }
    @Override
    public NotificationDTO sendNotification(NotificationDTO notification) {
        try {
            if (notification != null){
                EmailMessageDTO emailMessageDTO = (EmailMessageDTO) WorkerUtil.getObject(notification.getMessage(),EmailMessageDTO.class);
                sendMail(emailMessageDTO);
               return   super.sendNotification(notification);

                //TODO: IMPLEMTARE METODO INVIO EMAIL
                }


           } catch (IOException e) {
                throw new RuntimeException(e);
            }

        return null;
    }

    private void sendMail(EmailMessageDTO emailMessageDTO) {
        super.log.info("invio email {}",emailMessageDTO );
    }

}
