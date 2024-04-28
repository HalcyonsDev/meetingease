package ru.halcyon.meetingease.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Aspect
@Slf4j
public class LoggingAspect {

    @Before("execution(* ru.halcyon.meetingease.service.*.*.*(..))")
    public void logServiceMethodCall(JoinPoint joinPoint) {
        log.info(
                "Method {} called with arguments {}",
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs())
        );
    }

    @AfterReturning(pointcut = "execution(* ru.halcyon.meetingease.service.*.*.*(..))", returning = "result")
    public void logServiceMethodReturn(JoinPoint joinPoint, Object result) {
        log.info(
                "Method {} returned {}",
                joinPoint.getSignature().getName(),
                result
        );
    }

    @AfterThrowing(pointcut = "execution(* ru.halcyon.meetingease.service.*.*.*(..))", throwing = "ex")
    public void logServiceMethodThrow(JoinPoint joinPoint, Exception ex) {
        log.info(
                "Method {} throw exception {}",
                joinPoint.getSignature().getName(),
                ex.getMessage()
        );
    }
}
