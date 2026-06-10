package it.ey.sync;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Modulo sync_pp: espone REST API reattive (WebFlux)
 * per la sincronizzazione della piattaforma del Portale Pubblico
 */
@SpringBootApplication(
    scanBasePackages = "it.ey.sync",
    exclude = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
    }
)
@OpenAPIDefinition(
    info = @Info(
        title = "dfp-sync-pp",
        version = "1.0",
        description = "Modulo di sincronizzazione per la piattaforma del Portale Pubblico",
        license = @License(name = "License of API", url = "API license URL"),
        contact = @Contact(url = "http://www.ey.com", name = "EY")
    )
)
@SecurityScheme(
    name = "jwtauth",
    scheme = "bearer",
    bearerFormat = "JWT",
    type = SecuritySchemeType.HTTP,
    in = SecuritySchemeIn.HEADER
)
@Slf4j
public class SyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyncApplication.class, args);
    }
}
