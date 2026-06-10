package it.ey.piao.bff.configuration;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;

import jakarta.jms.ConnectionFactory;

/**
 * Configura la factory JMS per il BFF.
 * Il BFF si sottoscrive al Topic "eventiProcessati" per ricevere
 * i messaggi già salvati su DB dal WORKER.
 * Usa failover transport per il reconnect automatico ad ActiveMQ.
 */
@Configuration
@EnableJms
public class JmsConfig {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.activemq.user}")
    private String username;

    @Value("${spring.activemq.password}")
    private String password;

    /**
     * Costruisce l'URL failover a partire dal broker-url configurato.
     * Garantisce reconnect automatico in caso di disconnessione.
     */
    private String buildFailoverUrl(String brokerUrl) {
        return "failover:(" + brokerUrl
            + ")?timeout=5000"
            + "&maxReconnectDelay=5000"
            + "&maxReconnectAttempts=-1"
            + "&startupMaxReconnectAttempts=5";
    }

    /**
     * ConnectionFactory con failover transport per il reconnect automatico.
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory activeMQFactory = new ActiveMQConnectionFactory();
        activeMQFactory.setBrokerURL(buildFailoverUrl(brokerUrl));
        activeMQFactory.setUserName(username);
        activeMQFactory.setPassword(password);
        activeMQFactory.setTrustAllPackages(true);

        CachingConnectionFactory cachingFactory = new CachingConnectionFactory(activeMQFactory);
        cachingFactory.setSessionCacheSize(10);
        cachingFactory.setReconnectOnException(true);
        return cachingFactory;
    }

    /**
     * Factory per il Topic "eventiProcessati" (pub/sub).
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
