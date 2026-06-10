package com.project.dfp.dfpGatewayService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {

    private String id;

    private String name;

    private String superRoleName;

    private String typology;

    private List<String> privileges;

}
