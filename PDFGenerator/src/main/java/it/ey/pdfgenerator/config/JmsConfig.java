package it.ey.pdfgenerator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import jakarta.jms.ConnectionFactory;

@Configuration
public class JmsConfig {


        // Factory per le code (ANYCAST)
        @Bean
        public JmsListenerContainerFactory<?> queueListenerFactory(ConnectionFactory connectionFactory) {
            DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
            factory.setConnectionFactory(connectionFactory);
            factory.setPubSubDomain(false); // Queue
            return factory;
        }

        // Factory per i topic (MULTICAST)
        @Bean
        public JmsListenerContainerFactory<?> topicListenerFactory(ConnectionFactory connectionFactory) {
            DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
            factory.setConnectionFactory(connectionFactory);
            factory.setPubSubDomain(true); // Topic

            return factory;
        }

}
