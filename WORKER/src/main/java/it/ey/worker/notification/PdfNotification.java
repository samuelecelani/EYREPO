package it.ey.worker.notification;

import it.ey.worker.dto.NotificationDTO;

/**
 * Gestione notifiche di tipo PDF.
 * Scheletro da completare con la logica di generazione PDF.
 */
public class PdfNotification extends BaseNotification {

    public PdfNotification() {
        super();
    }

    @Override
    public NotificationDTO sendNotification(NotificationDTO notification) {
        if (notification != null) {
            log.info("Elaborazione notifica PDF per modulo={}", notification.getIdModulo());

            // TODO: implementare la logica di generazione PDF
            // Es. chiamare il PDFGenerator, salvare il file, ecc.
            generatePdf(notification);

            // Salva la notifica su DB tramite BaseNotification
            return super.sendNotification(notification);
        }
        return null;
    }

    /**
     * Logica di generazione PDF da implementare.
     */
    private void generatePdf(NotificationDTO notification) {
        log.info("Generazione PDF per modulo={}, messaggio='{}'",
            notification.getIdModulo(), notification.getMessage());
        // TODO: implementare generazione PDF
    }
}
