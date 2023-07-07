package com.redisson.distributelock.common.distributelock.aop;

import com.redisson.distributelock.common.distributelock.RedissonLock;
import com.redisson.distributelock.common.distributelock.annotation.DistributeLock;
import com.redisson.distributelock.common.distributelock.service.LockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributeLockAop {

  private final DistributeLockTransaction transaction;
  private final RedissonClient redissonClient;

  // DistributeLock 어노테이션에 패키지 경로
  @Around("@annotation(com.redisson.distributelock.common.distributelock.annotation.DistributeLock)")
  public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    DistributeLock distributeLock = method.getAnnotation(DistributeLock.class);

    String key = getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributeLock.key());
    String useType = getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributeLock.useType());

    LockService lockService = RedissonLock.getLockService(useType);
    List<RLock> locks = lockService.lock(key, useType);

    try {
      for (RLock lock : locks) {
        // 락 획득을 시도한다. 이미 다른 클라이언트가 락을 점유하고 있다면 설정된 대기 시간만큼 기다린다.
        boolean isLocked = lock.tryLock(distributeLock.waitTime(), distributeLock.leaseTime(), distributeLock.timeUnit());
        log.info("lock key => {}, isLock {}", lock.getName(), isLocked);

        if (!isLocked) {
          throw new Exception("사용자가 많아 잠시 후에 다시 시도해주시기 바랍니다.");
        }
      }

      // 동시성 처리를 하기 위해서는 락 획득 이후 트랜잭션이 시작되어야 하고 트랜잭션이 커밋되고 난 이후 락이 해제되어야
      // 하기때문에 AOP로 한번 감싸준다.
      return transaction.proceed(joinPoint);
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    } finally {
      for (RLock lock : locks) {
        if (lock.isLocked()) {
          lock.unlock();
        }
        log.info("unlock key => {}", lock.getName());
      }

      locks.clear();
    }
  }

  // 메서드에 파라미터로 넘어온 값을 추출한다.
  private String getDynamicValue(String[] parameterNames, Object[] args, String key) {
    ExpressionParser parser = new SpelExpressionParser();
    StandardEvaluationContext context = new StandardEvaluationContext();

    for (int i = 0; i < parameterNames.length; i++) {
      context.setVariable(parameterNames[i], args[i]);
    }

    if (key != null && !key.startsWith("#")) {
      return key;
    }

    return parser.parseExpression(key).getValue(context, String.class);
  }
}