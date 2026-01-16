package it.ey.piao.bff;

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
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * Questa classe è la classe di avvio di tutta l'applicazione Spring Boot. Lo start avviene sul server embedded Tomcat.
 * Le configurazioni sono inserite nel file resources/application.properties
 *
 * @author EY
 * @version 1.0
 */



@SpringBootApplication(
    scanBasePackages = "it.ey.piao.bff",
    exclude = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
})
@EnableCaching

        @EnableTransactionManagement
@OpenAPIDefinition(
        info = @Info(
                title = "PiaoApplication",
                version = "0.0",
                description = "Description",
                license = @License(name = "License of API", url = "API license URL"),
                contact = @Contact(url = "http://www.ey.com", name = "EY")
        )
)

@SecurityScheme(
    name = "sessionCookie",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.COOKIE,
    paramName = "SESSIONID" // o il nome del tuo cookie
)
@Slf4j
public class PiaoApplication {

    public static void main(String[] args) {
        //log.debug("Starting PiaoApplication...");

        SpringApplication.run(PiaoApplication.class, args);
    }

}
