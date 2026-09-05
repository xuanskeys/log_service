package com.xuan.logservice.service.impl;

import com.xuan.logservice.entity.model.OperationLog;
import com.xuan.logservice.mapper.OperationLogMapper;
import com.xuan.logservice.service.IOperationLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 操作日志表 服务实现类
 * </p>
 *
 * @author xuan
 * @since 2026-09-03
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements IOperationLogService {

}
