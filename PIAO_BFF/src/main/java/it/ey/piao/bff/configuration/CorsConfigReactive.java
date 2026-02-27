package it.ey.piao.bff.configuration;

import org.springframework.context.annotation.Configuration;

/**
 * La configurazione CORS è gestita interamente in SecurityConfigReactive
 * tramite CorsConfigurationSource + cors().configurationSource(...).
 * Questo file è mantenuto vuoto per evitare conflitti di bean duplicati.
 */
@Configuration
public class CorsConfigReactive {
    // CORS configurato in SecurityConfigReactive.corsConfigurationSource()
}
