package it.ey.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginDataDTO {

private String sessionId;
private  String keycloakAuthUri;

}
