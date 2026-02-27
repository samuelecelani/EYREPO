package it.ey.dto;

import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CampiTecniciDTO {

    private Boolean validity;

    private String createdBy;

    private LocalDate createdTs;

    private String updatedBy;

    private LocalDate updatedTs;

    private String createdByRole;

    private String updatedByRole;

    private String createdByNameSurname;

    private String updatedByNameSurname;
}
