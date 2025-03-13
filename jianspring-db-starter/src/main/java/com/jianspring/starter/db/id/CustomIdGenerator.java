package com.jianspring.starter.db.id;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

/**
 * @Author: InfoInsights
 * @Date: 2023/2/23 下午4:58
 * @Version: 1.0.0
 */
public class CustomIdGenerator implements IdentifierGenerator {

    @Override
    public Number nextId(Object entity) {
        long dataCenterId = IdUtil.getDataCenterId(31L);
        long workerId = IdUtil.getWorkerId(dataCenterId, 31L);
        return IdUtil.getSnowflake(workerId, dataCenterId).nextId();
    }

}
