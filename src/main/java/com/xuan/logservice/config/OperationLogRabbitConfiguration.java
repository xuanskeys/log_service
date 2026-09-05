package com.xuan.logservice.config;

import com.xuan.logging.OperationLogAutoConfiguration;
import com.xuan.logging.OperationType;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OperationLogRabbitConfiguration {

    @Bean
    public JacksonJsonMessageConverter operationLogMessageConverter() {
        return new JacksonJsonMessageConverter("com.xuan.logging");
    }

    @Bean
    public Declarables consumedOperationLogTopology(OperationLogConsumerProperties properties) {
        List<Declarable> declarations = new ArrayList<>();
        for (String serviceName : properties.getServices()) {
            TopicExchange exchange = new TopicExchange(
                    OperationLogAutoConfiguration.exchangeName(serviceName), properties.isDurable(), false);
            declarations.add(exchange);
            for (OperationType type : OperationType.values()) {
                Queue queue = new Queue(
                        OperationLogAutoConfiguration.queueName(serviceName, type), properties.isDurable());
                declarations.add(queue);
                declarations.add(BindingBuilder.bind(queue).to(exchange).with(type.routingKey()));
            }
        }
        return new Declarables(declarations);
    }

    @Bean("operationLogQueueNames")
    public String[] operationLogQueueNames(OperationLogConsumerProperties properties) {
        return properties.getServices().stream()
                .flatMap(service -> java.util.Arrays.stream(OperationType.values())
                        .map(type -> OperationLogAutoConfiguration.queueName(service, type)))
                .toArray(String[]::new);
    }
}
