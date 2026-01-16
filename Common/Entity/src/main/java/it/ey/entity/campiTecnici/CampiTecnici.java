package it.ey.entity.campiTecnici;

import it.ey.entity.listener.CampiTecniciListener;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDate;

@Data
@MappedSuperclass
@Audited
@EntityListeners(CampiTecniciListener.class)
@NoArgsConstructor
public abstract class CampiTecnici {

    @Column(name = "X_VALIDITY_IN", nullable = false, columnDefinition = "bool")
    private Boolean validity = true;

    @Column(name = "X_CREATEDBY", nullable = false, length = 20)
    private String createdBy;

    @Column(name = "X_CREATED_TS", nullable = false)
    private LocalDate createdTs = LocalDate.now();

    @Column(name = "X_UPDATEDBY", length = 20)
    private String updatedBy;

    @Column(name = "X_UPDATED_TS")
    private LocalDate updatedTs;
}
