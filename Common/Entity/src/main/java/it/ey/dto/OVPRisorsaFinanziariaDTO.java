package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OVPRisorsaFinanziariaDTO implements Serializable {
    private Long id;
    @JsonIgnore
    private OVPDTO ovpDTO;
    private Long idOvp;
    private String iniziativa;
    private String descrizione;
    private Long dotazioneFinanziaria;
    private String fonteFinanziamento;
}
