package it.ey.entity.campiTecnici;

import it.ey.entity.listener.CampiTecniciListener;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@MappedSuperclass
@FilterDef(
        name = "activeFilter",
        defaultCondition = "X_ACTIVE = true"
        )

@Filter(name = "activeFilter")
@EntityListeners(CampiTecniciListener.class)
public class CampiTecnici {

    @Column(name = "X_VALIDITY_IN", nullable = false, columnDefinition = "bool")
    @Builder.Default
    private Boolean validity = true;

    @Column(name = "X_CREATEDBY", nullable = false, length = 20)
    private String createdBy;

    @Column(name = "X_CREATED_TS", nullable = false)
    @Builder.Default
    private LocalDate createdTs = LocalDate.now();

    @Column(name = "X_UPDATEDBY", length = 20)
    private String updatedBy;

    @Column(name = "X_UPDATED_TS")
    private LocalDate updatedTs;

    @Column(name = "X_CREATEDBYROLE", length = 50)
    private String createdByRole;

    @Column(name = "X_UPDATEDBYROLE", length = 50)
    private String updatedByRole;

    @Column(name = "X_CREATEDBYNAMESURNAME", length = 100)
    private String createdByNameSurname;

    @Column(name = "X_UPDATEDBYNAMESURNAME", length = 100)
    private String updatedByNameSurname;

    @Column(name = "X_ACTIVE")
    @Builder.Default
    private Boolean active = Boolean.TRUE;

    @Column(name = "X_DEACTIVATIONTIME")
    private LocalDateTime deactivationTime;
}