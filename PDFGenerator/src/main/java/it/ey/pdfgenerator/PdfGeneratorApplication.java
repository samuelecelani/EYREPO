package it.ey.pdfgenerator;

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
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(
        scanBasePackages = {"it.ey.worker","it.ey.utils"},

        exclude = {
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
        }, excludeName = {
        "io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration"
})


@EnableTransactionManagement
@OpenAPIDefinition(
        info = @Info(
                title = "PdfApplication",
                version = "0.0",
                description = "Description",
                license = @License(name = "License of API", url = "API license URL"),
                contact = @Contact(url = "http://www.ey.com", name = "EY")
        )
)
@SecurityScheme(name = "jwtauth", scheme = "bearer", bearerFormat = "JWT", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
@Slf4j
public class PdfGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdfGeneratorApplication.class, args);
    }
    /**
     * Bean di utilità per la conversione di Entity a DTO e viceversa
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
