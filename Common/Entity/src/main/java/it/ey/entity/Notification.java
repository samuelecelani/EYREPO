package it.ey.entity;

import io.micrometer.common.util.StringUtils;
import it.ey.dto.NotificationDTO;
import it.ey.entity.listener.NotificationListner;
import it.ey.enums.TypeNotification;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(NotificationListner.class)
@Getter
@Setter
@Table(name = "Notification")
public class Notification {



    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notify_seq")
    @SequenceGenerator(name = "notify_seq", sequenceName = "notify_seq", allocationSize = 1)
    private Long id;
    @Column(name = "message", nullable = false)
    private String message;
    @Column(name = "sender")
    private String sender;
    @Column(name = "isReady")
    private Boolean ready;
    @Column(name = "isRead")
    private Boolean read;
    @Column(name = "creationDate")
    private LocalDate creationDate;
    @Column(name = "readDate")
    private LocalDate readDate;
    @Column(name = "type",  nullable = false,columnDefinition = "VARCHAR(20)")
    private TypeNotification type;
    @Column(name = "id_Modulo",  nullable = false,columnDefinition = "VARCHAR(50)")
    private String idModulo;

    public Notification(NotificationDTO dto) {
        this.id =  dto.getId();
        this.message = dto.getMessage();
        if(StringUtils.isNotBlank(dto.getSender())) {
            this.sender = dto.getSender();
        }
        this.ready = dto.getReady();
        this.read = dto.getRead();
        if(dto.getCreationDate() != null) {
            this.creationDate = dto.getCreationDate();
        }
        if(dto.getReadDate() != null) {
            this.readDate = dto.getReadDate();
        }
        this.idModulo = dto.getIdModulo();
        this.type = dto.getType();
    }

    }
