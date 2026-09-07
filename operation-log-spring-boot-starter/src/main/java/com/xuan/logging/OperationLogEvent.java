package com.xuan.logging;

import java.io.Serializable;
import java.time.LocalDateTime;

public record OperationLogEvent(
        String serviceSource,
        String operationType,
        Long userId,
        String username,
        String email,
        String phone,
        Long tenantId,
        String tenantName,
        Long roleId,
        String roleName,
        String description,
        LocalDateTime currentTime,
        String level
) implements Serializable {
}
