

package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationTeamDTO {
    private Long id;
    private Long idSezione1;
    private String membro;
    private String ruolo;
}
