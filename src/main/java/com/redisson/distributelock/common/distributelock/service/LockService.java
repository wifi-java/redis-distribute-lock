package com.redisson.distributelock.common.distributelock.service;

import org.redisson.api.RLock;

import java.util.List;

public interface LockService {

    List<RLock> lock(String key, String useType);
}
