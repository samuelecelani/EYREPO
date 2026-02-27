package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class MilestoneDTO extends CampiTecniciDTO
{
    private Long id;
    private Long idSottofaseMonitoraggio;
    private String descrizione;
    private LocalDate data;
    private Boolean isPromemoria;
    private LocalDate dataPromemoria;
    private Long idPromemoria;
}