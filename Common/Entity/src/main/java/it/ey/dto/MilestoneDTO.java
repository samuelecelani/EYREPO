package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class MilestoneDTO extends BaseDTO
{
    private Long id;
    private Long idSottofaseMonitoraggio;
    private String descrizione;
    private LocalDateTime data;
    private Boolean isPromemoria;
    private LocalDateTime dataPromemoria;
    private Long idPromemoria;
}