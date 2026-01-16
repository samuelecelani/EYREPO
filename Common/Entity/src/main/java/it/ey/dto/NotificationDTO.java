package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micrometer.common.util.StringUtils;
import it.ey.entity.Notification;
import it.ey.enums.TypeNotification;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

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
    private LocalDate creationDate;
    private LocalDate readDate;
    private TypeNotification type;
    private String idModulo;


    public NotificationDTO(Notification entity) {
        this.id =  entity.getId();
        this.message = entity.getMessage();
        if(StringUtils.isNotBlank(entity.getSender())) {
            this.sender = entity.getSender();
        }
        this.ready = entity.getReady();
        this.read = entity.getRead();
        if(entity.getCreationDate() != null) {
            this.creationDate = entity.getCreationDate();
        }
        if(entity.getReadDate() != null) {
            this.readDate = entity.getReadDate();
        }
        this.idModulo = entity.getIdModulo();
        this.type = entity.getType();
    }

}
