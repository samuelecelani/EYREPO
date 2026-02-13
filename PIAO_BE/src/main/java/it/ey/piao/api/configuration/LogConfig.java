package it.ey.piao.api.configuration;

import ch.qos.logback.classic.LoggerContext;
import it.ey.piao.api.log.JpaLogAppender;
import it.ey.piao.api.service.impl.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogConfig {

    @Bean
    public CommandLineRunner initJpaAppender(LogService logService) {
        return args -> {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

            JpaLogAppender appender = new JpaLogAppender();
            appender.setContext(context);
            JpaLogAppender.setLogService(logService); // imposta il servizio statico
            appender.start(); // avvia l'appender (e quindi anche l'executor interno)

            ch.qos.logback.classic.Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.addAppender(appender);
        };
    }
}


