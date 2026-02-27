package it.ey.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigFeInitializerDTO {
    private  String apiEndpoint;
    private boolean isProduction;
}
