package com.xuan.logservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "operation-log.consumer")
public class OperationLogConsumerProperties {
    private List<String> services = new ArrayList<>(List.of("user-service", "role-service"));
    private boolean durable = true;
}
