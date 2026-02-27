package it.ey.piao.bff.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import jakarta.jms.ConnectionFactory;

/**
 * Configura la factory JMS per il BFF.
 * Il BFF si sottoscrive al Topic "eventi" con selector "confirmed = TRUE"
 * per ricevere solo i messaggi già salvati su DB dal WORKER.
 */
@Configuration
@EnableJms
public class JmsConfig {

    /**
     * Factory per il Topic "eventi" (ANYCAST pub/sub).
     * Il BFF la usa per ricevere eventi dal WORKER.
     */
    @Bean
    public JmsListenerContainerFactory<?> topicListenerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(true); // Topic pub/sub
        return factory;
    }
}
