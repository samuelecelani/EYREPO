package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * DTO per la vista di validazione.
 * Estende StrutturaPiaoDTO con campi aggiuntivi necessari alla tabella di validazione:
 * - triennio piao (calcolato)
 * - stato rimappato per la validazione
 * - info sull'utente che ha inviato la richiesta di validazione
 * - info sull'utente che ha validato/rifiutato/revocato
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class StrutturaValidazioneDTO extends StrutturaPiaoDTO {


    private String triennio;
    private String statoValidazione;
    private String profUtenteInvioRichiesta;
    private LocalDate dataInvioRichiesta;
    private String profUtenteValidazione;
    private LocalDate dataValidazione;
    private String sezioneEnum;
}
