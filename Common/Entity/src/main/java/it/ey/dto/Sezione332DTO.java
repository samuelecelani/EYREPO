package it.ey.dto;

import it.ey.entity.StatoSezione;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Sezione332DTO {

    private StatoSezione statoSezione;



}
