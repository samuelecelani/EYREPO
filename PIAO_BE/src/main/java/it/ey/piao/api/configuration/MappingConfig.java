package it.ey.piao.api.configuration;

// src/main/java/it/ey/piao/api/config/MappingConfig.java

import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class MappingConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) // ogni getObject() => nuova istanza
    public CycleAvoidingMappingContext cycleAvoidingMappingContext() {
        return new CycleAvoidingMappingContext();
    }
}
