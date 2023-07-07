package com.redisson.distributelock.common.distributelock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributeLock {
    // 락 이름
    String key();

    // 사용 타입
    String useType();

    // 시간 단위
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    // 락을 획득하기 위해 대기하는 시간
    long waitTime() default 10L;

    // 락 점유 시간 해당 시간이 지나면 자동으로 락이 해제된다.
    long leaseTime() default 5L;

}