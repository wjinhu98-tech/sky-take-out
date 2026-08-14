package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static com.sky.constant.AutoFillConstant.*;

/**
 * 通用字段自动填充处理切面
 */
@Slf4j
@Aspect
@Component
public class AutoFillAspect {
    /**
     * 定义一个切入点，切入点为 com.sky.mapper 包下的所有带有@AutoFill 注解的方法
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {}

    /**
     * 前置通知：在方法执行之前执行通用字段自动填充逻辑
     * @param joinPoint
     */
    @Before("autoFillPointCut()")
    public void before(JoinPoint joinPoint) {
        log.info("开始执行通用字段自动填充...");

        //获取方法参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        Object[] args = joinPoint.getArgs();

        //判断不为空
        if (args == null && args.length == 0) {
            return;
        }
        Object entity = args[0];

        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        if(operationType == OperationType.UPDATE){

            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }else if (operationType == OperationType.INSERT){
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }
}
