package com.xuan.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(OperationLogProperties.class)
@ConditionalOnProperty(prefix = "operation-log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OperationLogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JacksonJsonMessageConverter operationLogMessageConverter() {
        return new JacksonJsonMessageConverter("com.xuan.logging");
    }

    @Bean
    public Declarables operationLogDeclarables(Environment environment, OperationLogProperties properties) {
        String serviceName = requiredServiceName(environment);
        TopicExchange exchange = new TopicExchange(exchangeName(serviceName), properties.isDurable(), false);
        List<Declarable> declarables = new ArrayList<>();
        declarables.add(exchange);
        for (OperationType type : OperationType.values()) {
            Queue queue = new Queue(queueName(serviceName, type), properties.isDurable());
            Binding binding = BindingBuilder.bind(queue).to(exchange).with(type.routingKey());
            declarables.add(queue);
            declarables.add(binding);
        }
        return new Declarables(declarables);
    }

    @Bean
    public OperationLogAspect operationLogAspect(RabbitTemplate rabbitTemplate, Environment environment) {
        return new OperationLogAspect(rabbitTemplate, requiredServiceName(environment));
    }

    public static String exchangeName(String serviceName) {
        return serviceName + ".log.exchange";
    }

    public static String queueName(String serviceName, OperationType type) {
        return serviceName + type.queueSuffix();
    }

    private static String requiredServiceName(Environment environment) {
        String serviceName = environment.getProperty("spring.application.name");
        if (!StringUtils.hasText(serviceName)) {
            throw new IllegalStateException("spring.application.name is required for operation logging");
        }
        return serviceName;
    }

    @Aspect
    public static class OperationLogAspect {
        private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);
        private final RabbitTemplate rabbitTemplate;
        private final String serviceName;

        OperationLogAspect(RabbitTemplate rabbitTemplate, String serviceName) {
            this.rabbitTemplate = rabbitTemplate;
            this.serviceName = serviceName;
        }

        @Around("@annotation(annotation)")
        public Object publish(ProceedingJoinPoint joinPoint, OperationLog annotation) throws Throwable {
            try {
                Object result = joinPoint.proceed();
                send(annotation, annotation.level(), annotation.description());
                return result;
            } catch (Throwable ex) {
                send(annotation, "ERROR", annotation.description() + " failed: " + safeMessage(ex));
                throw ex;
            } finally {
                OperationLogContext.clear();
            }
        }

        private void send(OperationLog annotation, String level, String description) {
            try {
                ServletRequestAttributes attributes = requestAttributes();
                OperationLogContext.Metadata context = OperationLogContext.current();
                OperationLogEvent event = new OperationLogEvent(
                        serviceName,
                        annotation.type().name(),
                        firstNonNull(longMetadata(attributes, "userId", "X-User-Id"),
                                context == null ? null : context.userId()),
                        firstText(metadata(attributes, "username", "X-Username"),
                                context == null ? null : context.username()),
                        context == null ? null : context.email(),
                        context == null ? null : context.phone(),
                        firstNonNull(longMetadata(attributes, "tenantId", "X-Tenant-Id"),
                                context == null ? null : context.tenantId()),
                        header(attributes, "X-Tenant-Name"),
                        longHeader(attributes, "X-Role-Id"),
                        header(attributes, "X-Role-Name"),
                        description,
                        LocalDateTime.now(),
                        level
                );
                rabbitTemplate.convertAndSend(exchangeName(serviceName), annotation.type().routingKey(), event);
            } catch (Exception ex) {
                // Logging must never change the business result.
                log.error("Unable to publish operation log for service {}", serviceName, ex);
            }
        }

        private static ServletRequestAttributes requestAttributes() {
            return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes value ? value : null;
        }

        private static String header(ServletRequestAttributes attributes, String name) {
            return attributes == null ? null : attributes.getRequest().getHeader(name);
        }

        private static String metadata(ServletRequestAttributes attributes, String attributeName, String headerName) {
            if (attributes == null) {
                return null;
            }
            Object attribute = attributes.getRequest().getAttribute(attributeName);
            return attribute == null ? header(attributes, headerName) : String.valueOf(attribute);
        }

        private static Long longMetadata(ServletRequestAttributes attributes, String attributeName, String headerName) {
            String value = metadata(attributes, attributeName, headerName);
            return parseLong(value);
        }

        private static Long longHeader(ServletRequestAttributes attributes, String name) {
            return parseLong(header(attributes, name));
        }

        private static Long parseLong(String value) {
            if (!StringUtils.hasText(value)) {
                return null;
            }
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        private static <T> T firstNonNull(T primary, T fallback) {
            return primary != null ? primary : fallback;
        }

        private static String firstText(String primary, String fallback) {
            return StringUtils.hasText(primary) ? primary : fallback;
        }

        private static String safeMessage(Throwable throwable) {
            String message = throwable.getMessage();
            return StringUtils.hasText(message) ? message : throwable.getClass().getSimpleName();
        }
    }
}
