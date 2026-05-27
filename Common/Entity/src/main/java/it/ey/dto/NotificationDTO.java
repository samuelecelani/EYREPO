package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.ey.enums.TypeNotification;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String message;
    private String sender;
    private Boolean ready;
    private Boolean read;
    private LocalDateTime creationDate;
    private LocalDateTime readDate;
    private TypeNotification type;
    private String idModulo;
    private String nomeModulo;
    private List<String> ruoli;
    private String codiceFiscale;
    private String codicePa;
    private String amministrazioneId;
    private String codiceFiscaleChiamante;
    private Boolean confirmed;

    /**
     * URL presigned S3 per il download diretto del file generato (PDF/Excel).
     * Viene popolato dal BFF prima dell'emit SSE al FE.
     */
    private String downloadUrl;

    /**
     * Nome/chiave del file generato su S3 (PDF/Excel).
     * Se valorizzato, viene usato per generare il presigned URL di download.
     */
    private String nomeFile;
}