package com.redisson.distributelock.common.distributelock.service.impl;

import com.redisson.distributelock.common.distributelock.service.LockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("LockServiceMenuImpl")
@RequiredArgsConstructor
@Slf4j
class LockServiceMenuImpl implements LockService {
  private static final String REDISSON_KEY_PREFIX = "LOCK_";

  private final RedissonClient redissonClient;

  @Override
  public List<RLock> lock(String key, String useType) {
    List<RLock> locks = new ArrayList<>();

    String lockKey = REDISSON_KEY_PREFIX + useType.toUpperCase() + "_" + key;
    RLock lock = redissonClient.getLock(lockKey);
    locks.add(lock);

    return locks;
  }
}
