package it.ey.sync.configuration;

import it.ey.sync.enums.WebServiceType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Permette di avere a runtime gli URL per le chiamate a servizi esterni.
 * Legge dalle property e li setta sull'enum {@link WebServiceType}.
 */
@Component
public class ServiceTypeInitializer {

    @Value("${external.service.url.gateway:}")
    private String gatewayUrl;

    @PostConstruct
    public void init() {
        WebServiceType.BFF.setUrl(gatewayUrl);
    }
}
