package it.ey.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class SezioneBaseDTO extends BaseDTO{


    private UlterioriInfoDTO ulterioriInfo;
    private List<AllegatoDTO> allegati;



}
