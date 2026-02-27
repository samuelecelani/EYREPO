package it.ey.pdfgenerator.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.pdfgenerator.dto.PdfRequest;
import it.ey.pdfgenerator.service.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfQueueConsumer {

    private final PdfGeneratorService pdfGeneratorService;
    private final ObjectMapper objectMapper;

    @JmsListener(destination = "${pdf.queue.name:pdf-generation-queue}")
    public void receivePdfRequest(String message) {
        try {
            log.info("Ricevuto messaggio dalla coda: {}", message);
            
            // Deserializza il messaggio JSON in PdfRequest
            PdfRequest pdfRequest = objectMapper.readValue(message, PdfRequest.class);
            
            // Genera il PDF
            byte[] pdfBytes = pdfGeneratorService.generatePdf(
                pdfRequest.getTemplateName(),
                pdfRequest.getData()
            );
            
            // Salva o invia il PDF
            log.info("PDF generato con successo: {} byte", pdfBytes.length);
            pdfGeneratorService.savePdf(pdfBytes, pdfRequest.getOutputFileName());
            log.info("PDF salvato con successo: {} byte", pdfBytes.length);

            //TODO:invia notifica alla coda
            
        } catch (Exception e) {
            log.error("Errore durante la generazione del PDF", e);
        }
    }
}
