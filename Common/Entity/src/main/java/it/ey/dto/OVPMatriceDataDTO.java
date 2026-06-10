package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OVPMatriceDataDTO implements Serializable {

    private List<OVPDTO> ovpList;
    private List<PrioritaPoliticaDTO> allPrioritaPolitiche;
    private List<AreaOrganizzativaDTO> allAreeOrganizzative;
}
