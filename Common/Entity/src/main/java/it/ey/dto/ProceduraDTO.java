package it.ey.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import it.ey.entity.Sezione21;
import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ProceduraDTO extends CampiTecniciDTO {

    private Long id;

    private Long idSezione21;

    private String denominazione;

    private String descrizione;

    private String unitaMisura;

    private String misurazione;

    private String target;

    private String uffResponsabile;

}

