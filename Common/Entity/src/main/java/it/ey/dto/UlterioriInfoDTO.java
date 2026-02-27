package it.ey.dto;


import it.ey.enums.Sezione;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class UlterioriInfoDTO extends BaseMongoDTO {
    private Sezione tipoSezione;



}
