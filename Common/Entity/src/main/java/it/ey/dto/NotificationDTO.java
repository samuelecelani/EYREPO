package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.ey.enums.TypeNotification;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String ruolo;
    private String codiceFiscale;
    private String codicePa;

    // true = il WORKER ha salvato su DB con successo → il BFF può propagare al FE via SSE
    private Boolean confirmed;

}
