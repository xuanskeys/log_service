package com.xuan.logservice.entity.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 操作日志表
 * </p>
 *
 * @author xuan
 * @since 2026-09-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("operation_log")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 服务来源
     */
    private String serviceSource;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 执行人用户ID，逻辑关联user_service.user.id
     */
    private Long userId;

    /**
     * 执行人用户名快照
     */
    private String username;

    /**
     * 操作涉及的邮箱快照
     */
    private String email;

    /**
     * 操作涉及的手机号快照
     */
    private String phone;

    /**
     * 租户ID，逻辑关联user_service.tenant.id
     */
    private Long tenantId;

    /**
     * 租户名称快照
     */
    private String tenantName;

    /**
     * 角色ID，逻辑关联role_service.role.id
     */
    private Long roleId;

    /**
     * 角色名称快照
     */
    private String roleName;

    /**
     * 执行描述
     */
    private String description;

    /**
     * 执行时间
     */
    @TableField("`current_time`")
    private LocalDateTime currentTime;

    /**
     * 安全等级或日志等级
     */
    private String level;


}
