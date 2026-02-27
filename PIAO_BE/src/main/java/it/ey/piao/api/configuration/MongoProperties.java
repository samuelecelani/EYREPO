package it.ey.piao.api.configuration;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.second-datasource")
public class MongoProperties {

    // Getter e Setter
    private String uri;
    private String database;

}
