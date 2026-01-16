package it.ey.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CampiTecniciDTO {

    private Boolean validity;

    private String createdBy;

    private LocalDate createdTs;

    private String updatedBy;

    private LocalDate updatedTs;
}
