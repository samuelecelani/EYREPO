package it.ey.piao.bff.property;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "auth")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PropertyAuthentication {

    private String clientId;
    private String oauth2Url;
    private List<String> scopes;


}
