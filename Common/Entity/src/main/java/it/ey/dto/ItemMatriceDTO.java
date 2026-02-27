package it.ey.dto;

import lombok.*;

import java.util.List;
import java.util.Map;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemMatriceDTO {
    private String politicalPriority;
    private Map<String, List<OvpItemDTO>> organisationalAreas;

}


