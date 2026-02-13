package it.ey.worker.notification;

import it.ey.dto.EmailMessageDTO;
import it.ey.dto.NotificationDTO;
import it.ey.utils.WorkerUtil;


import java.io.IOException;

public class EmailNotification extends BaseNotification {




    public EmailNotification() {
        super();
    }
    @Override
    public void sendNotification(NotificationDTO notification) {
        try {
            if (notification != null){
                EmailMessageDTO emailMessageDTO = (EmailMessageDTO) WorkerUtil.getObject(notification.getMessage(),EmailMessageDTO.class);
                sendMail(emailMessageDTO);
                super.sendNotification(notification);

                //TODO: IMPLEMTARE METODO INVIO EMAIL
                }


           } catch (IOException e) {
                throw new RuntimeException(e);
            }

    }

    private void sendMail(EmailMessageDTO emailMessageDTO) {
        super.log.info("invio email {}",emailMessageDTO );
    }

}
