package it.ey.piao.bff.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.dto.NotificationDTO;
import it.ey.utils.WorkerUtil;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
//Producer per scrivere messaggi sulle code che vengono lette dal Consumer sul modulo WORKER
@Service
public class NotificationProducer {

    private final JmsTemplate jmsTemplate;
    @Value("${producer.variable.topic}")
    private String customTopic;
    @Value("${producer.variable.destination}")
    private String destination;

    @Autowired
    public NotificationProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }


    // ANYCAST
    public void sendAnycast(NotificationDTO notification) throws JsonProcessingException {
        jmsTemplate.convertAndSend(new ActiveMQQueue( this.destination),   new ObjectMapper().writeValueAsString(notification));
    }
    // MULTICAST → FE (Topic)
    public void sendMulticast(NotificationDTO notification) throws IOException {


        jmsTemplate.convertAndSend( new ActiveMQTopic(this.customTopic),   WorkerUtil.toJson(notification));
    }
}
