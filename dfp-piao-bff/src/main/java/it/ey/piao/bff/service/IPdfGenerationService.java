package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PdfNotificationDTO;
import it.ey.enums.Sezione;
import reactor.core.publisher.Mono;

public interface IPdfGenerationService {

    /**
     * Genera un PDF per il PIAO o per una singola sezione.
     * Costruisce un PdfNotificationDTO e lo invia al Notifica_BE su /api/v1/pdf/generation.
     *
     * @param idPiao    ID del PIAO
     * @param sezione   enum Sezione (PIAO = tutte le sezioni, altrimenti solo quella specificata)
     * @param codicePa  codice della Pubblica Amministrazione
     * @return Mono con il risultato della richiesta di generazione PDF
     */
    Mono<GenericResponseDTO<PdfNotificationDTO>> generatePdf(Long idPiao, Sezione sezione, String codicePa);
}
