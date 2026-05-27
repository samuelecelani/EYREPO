package it.ey.dto.external;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmministrazioneDto {

    private String id;
    private String name;
    private String ipaCode;
    private String fiscalCode;
    private String email;
    private String administrationType;

}
