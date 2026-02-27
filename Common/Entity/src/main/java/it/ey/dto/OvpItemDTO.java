package it.ey.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public  class OvpItemDTO {
    private String code;
    private String description;
    private  String denominazione;

}
