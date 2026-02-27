package it.ey.worker.entity;

import it.ey.worker.entity.listener.NotificationListner;
import it.ey.worker.enums.TypeNotification;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    private LocalDateTime creationDate;

    @Column(name = "readDate")
    private LocalDateTime readDate;

    @Column(name = "type", nullable = false, columnDefinition = "VARCHAR(20)")
    private TypeNotification type;

    @Column(name = "id_Modulo", nullable = false, columnDefinition = "VARCHAR(50)")
    private String idModulo;

    @Column(name = "ruolo", nullable = false, columnDefinition = "VARCHAR(255)")
    private String ruolo;

    @Column(name = "codiceFiscale", nullable = false, columnDefinition = "VARCHAR(255)")
    private String codiceFiscale;

    @Column(name = "codicePa", nullable = false, columnDefinition = "VARCHAR(255)")
    private String codicePa;
}
