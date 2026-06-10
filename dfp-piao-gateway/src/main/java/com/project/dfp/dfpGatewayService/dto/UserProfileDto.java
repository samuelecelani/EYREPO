package com.project.dfp.dfpGatewayService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    private LocalDate dataDiNascita;

    private String luogoDiNascita;

    private String typology;

    private RoleDto dfpRole;

    private String phone;

    private String email;

    private List<AmministrazioneDto> amministrazioni;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AmministrazioneDto {

        private String id;

        private String name;

        private String fiscalCode;

        private String phone;

        private String email;

        private String qualifica;

        private String ipaCode;

        private String tipoAmministrazione;

        private List<RoleDto> ruoli;

    }
}
