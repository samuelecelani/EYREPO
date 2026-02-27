package it.ey.pdfgenerator.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ConsumerProperties {
    @Value("${consumer.variable.topic}")
    private String customTopic;
    @Value("${consumer.variable.destination}")
    private String destination;
}

