package it.ey.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "app_log")
public class AppLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "log_seq")
    @SequenceGenerator(name = "log_seq", sequenceName = "log_seq", allocationSize = 1)
    Long id;

    @Column(name = "level")
    private String level;

    @Column(name = "logger", nullable = false)
    private String logger;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "thread", nullable = false)
    private String thread;
    @Column(name = "timestamp")
    private LocalDateTime timestamp;


}
