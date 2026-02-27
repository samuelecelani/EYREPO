package it.ey.worker.config;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class JmsConfig {

    private static final Logger log = LoggerFactory.getLogger(JmsConfig.class);

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.activemq.user}")
    private String brokerUser;

    @Value("${spring.activemq.password}")
    private String brokerPassword;

    // Factory per le code (ANYCAST consumer) — usa la ConnectionFactory auto-configurata da Spring
    @Bean
    public JmsListenerContainerFactory<?> queueListenerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(false);
        return factory;
    }

    // Factory per i topic (MULTICAST consumer) — usa la ConnectionFactory auto-configurata da Spring
    @Bean
    public JmsListenerContainerFactory<?> topicListenerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(true);
        return factory;
    }

    /**
     * ConnectionFactory DEDICATA al producer, wrappata in SingleConnectionFactory
     * per mantenere la connessione aperta e ricrearla automaticamente se cade.
     * Separata dalla CachingConnectionFactory dei listener per evitare
     * conflitti di sessione ("The Session is closed").
     */
    @Bean
    public SingleConnectionFactory producerConnectionFactory() {
        ActiveMQConnectionFactory amqFactory = new ActiveMQConnectionFactory(brokerUrl);
        amqFactory.setUserName(brokerUser);
        amqFactory.setPassword(brokerPassword);

        SingleConnectionFactory scf = new SingleConnectionFactory(amqFactory);
        scf.setReconnectOnException(true); // ricrea la connessione se cade
        return scf;
    }

    /**
     * JmsTemplate che usa la SingleConnectionFactory dedicata al producer.
     * Ripubblica sul Topic "eventi" con JMS property confirmed=true
     * solo dopo che il salvataggio su DB è andato a buon fine.
     */
    @Bean
    public JmsTemplate jmsQueueTemplate() {
        JmsTemplate template = new JmsTemplate(producerConnectionFactory());
        template.setPubSubDomain(true); // Topic
        return template;
    }
}