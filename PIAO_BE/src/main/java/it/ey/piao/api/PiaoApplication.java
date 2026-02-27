package it.ey.piao.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * Questa classe è la classe di avvio di tutta l'applicazione Spring Boot. Lo start avviene sul server embedded Tomcat.
 * Le configurazioni sono inserite nel file resources/application.properties
 *
 * @author EY
 * @version 1.0
 */


@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration.class,
    org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration.class
}, excludeName = {
    "io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration"
})


@ComponentScan(basePackages = {
    "it.ey.piao.api",      // modulo principale
    "it.ey.mapper"         // modulo per i mapper
})

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
@EntityScan(basePackages = "it.ey.piao.api.repository.entity")
@SecurityScheme(name = "jwtauth", scheme = "bearer", bearerFormat = "JWT", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
@Slf4j
public class PiaoApplication {

    public static void main(String[] args) {
        //log.debug("Starting PiaoApplication...");

        SpringApplication.run(PiaoApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
