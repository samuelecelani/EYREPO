package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ConfigurazioniDTO extends BaseDTO
{
    private Long id;

    private String codice;

    private String valore;

    private String TypeDato;

    private Boolean isConfigUI;
}
