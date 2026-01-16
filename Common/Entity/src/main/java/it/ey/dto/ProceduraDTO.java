package it.ey.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import it.ey.entity.Sezione21;
import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProceduraDTO extends CampiTecniciDTO {

    private Long id;

    @JsonIgnore
    private Sezione21 sezione21;

    private String intro;

    private String denominazione;

    private String descrizione;

    private String unitaMisura;

    private String misurazione;

    private String target;

    private String uffResponsabile;

}

