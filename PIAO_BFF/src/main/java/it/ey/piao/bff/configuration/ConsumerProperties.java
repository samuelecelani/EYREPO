package it.ey.piao.bff.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Properties per il consumer JMS del BFF.
 */
@Component
@Getter
@Setter
public class ConsumerProperties {
    @Value("${consumer.variable.confirmation-topic}")
    private String confirmationTopic;
}
