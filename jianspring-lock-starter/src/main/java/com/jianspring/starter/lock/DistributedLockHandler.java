package com.jianspring.starter.lock;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.jianspring.starter.commons.error.CommonErrorCode;
import com.jianspring.starter.commons.exception.BizException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

@Aspect
public class DistributedLockHandler {
    private static final Logger log = LoggerFactory.getLogger(DistributedLockHandler.class);

    private final RedissonLock redissonLock;

    public DistributedLockHandler(RedissonLock redissonLock) {
        this.redissonLock = redissonLock;
    }

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, DistributedLock distributedLock) throws Throwable {

        String redisKey = this.getRedisKey(proceedingJoinPoint, distributedLock);
        if (distributedLock.needMethodPrefix()) {
            String methodString = proceedingJoinPoint.getSignature().toShortString();
            redisKey = distributedLock.prefixKey() + methodString + ":" + redisKey;
        } else {
            redisKey = distributedLock.prefixKey() + redisKey;
        }

        int leaseTime = distributedLock.leaseTime();
        String errorDesc = distributedLock.errorDesc();
        int waitTime = distributedLock.waitTime();
        boolean needWait = distributedLock.needWait();

        Object o;
        try {
            boolean lock;
            if (needWait) {
                lock = redissonLock.tryLock(redisKey, leaseTime);
            } else {
                lock = redissonLock.tryLock(redisKey, leaseTime, waitTime);
            }
            if (!lock) {
                throw new BizException(CommonErrorCode.ERROR.getCode(), errorDesc);
            }
            o = proceedingJoinPoint.proceed();
        } catch (Exception e) {
            log.error("加锁方法异常" + e.getMessage());
            throw e;
        } finally {
            if (this.redissonLock.isHeldByCurrentThread(redisKey)) {
                redissonLock.unlock(redisKey);
            }
        }
        return o;
    }

    private String getRedisKey(ProceedingJoinPoint proceedingJoinPoint, DistributedLock distributedLock) {
        String key = distributedLock.key();
        Object[] parameterValues = proceedingJoinPoint.getArgs();
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();

        DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
        String[] parameterNames = nameDiscoverer.getParameterNames(method);

        if (!StringUtils.hasText(key)) {
            return "redissionLock";
        }

        // SpEL表达式
        SpelExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

        if (parameterNames != null && parameterNames.length > 0) {
            for (int i = 0; i < parameterNames.length; ++i) {
                evaluationContext.setVariable(parameterNames[i], parameterValues[i]);
            }
        }

        try {
            Expression expression = parser.parseExpression(key);
            Object expressionValue = expression.getValue(evaluationContext);

            return (expressionValue != null && !"".equals(expressionValue.toString())) ? expressionValue.toString() : key;
        } catch (Exception e) {
            log.error("执行spel表达式失败：{}", ExceptionUtil.stacktraceToString(e));
            return key;
        }
    }


}


