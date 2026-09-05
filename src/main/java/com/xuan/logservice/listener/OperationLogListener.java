package com.xuan.logservice.listener;

import com.xuan.logging.OperationLogEvent;
import com.xuan.logservice.entity.model.OperationLog;
import com.xuan.logservice.service.IOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperationLogListener {
    private final IOperationLogService operationLogService;

    @RabbitListener(queues = "#{@operationLogQueueNames}")
    public void consume(OperationLogEvent event) {
        OperationLog entity = new OperationLog()
                .setServiceSource(event.serviceSource())
                .setOperationType(event.operationType())
                .setUserId(event.userId())
                .setUsername(event.username())
                .setTenantId(event.tenantId())
                .setTenantName(event.tenantName())
                .setRoleId(event.roleId())
                .setRoleName(event.roleName())
                .setDescription(event.description())
                .setCurrentTime(event.currentTime())
                .setLevel(event.level());
        operationLogService.save(entity);
    }
}
