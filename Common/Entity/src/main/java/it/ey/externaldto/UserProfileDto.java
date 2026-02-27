package it.ey.externaldto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {

    private String id;

    private String name;

    private String surname;

    private String codiceFiscale;

    private String phone;

    private String email;

    private List<AmministrazioneDto>  amministrazioni;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AmministrazioneDto {

        private String id;

        private String nome;

        private List<RoleDto> ruoli;

    }
}

