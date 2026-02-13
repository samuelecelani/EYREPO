package it.ey.piao.bff.configuration;

import org.springframework.boot.autoconfigure.web.reactive.WebFluxRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

/**
 * Configurazione per abilitare il supporto dell'annotazione @ApiV1Controller in WebFlux.
 */
@Configuration
public class ApiV1ControllerConfig {

    @Bean
    public WebFluxRegistrations webFluxRegistrations() {
        return new WebFluxRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new ApiV1ReactiveRequestMappingHandlerMapping();
            }
        };
    }
}
