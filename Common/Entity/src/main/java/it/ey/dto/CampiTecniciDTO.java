package it.ey.dto;

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
}
