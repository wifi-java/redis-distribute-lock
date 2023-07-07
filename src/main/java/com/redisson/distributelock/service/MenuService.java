package com.redisson.distributelock.service;

import com.redisson.distributelock.common.distributelock.annotation.DistributeLock;
import com.redisson.distributelock.repository.MenuRepository;
import com.redisson.distributelock.repository.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuService {

  private final MenuRepository menuRepository;

  @DistributeLock(key = "#idx", useType = "MENU")
  public void decreaseStockForDistributeLock(Long idx) {
    Menu menu = menuRepository.findById(idx).orElseThrow();
    menu.decreaseStock();
  }

  @Transactional
  public void decreaseStockForNotDistributeLock(Long idx) {
    Menu menu = menuRepository.findById(idx).orElseThrow();
    menu.decreaseStock();
  }
}
