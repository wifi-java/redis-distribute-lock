package com.redisson.distributelock.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import javax.persistence.*;

@Entity
@Table(name = "TB_MENU")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "IDX")
  @Comment("번호")
  private Long idx;

  @Column(name = "NAME", length = 50, nullable = false)
  @Comment("이름")
  private String name;

  @Column(name = "STOCK", nullable = false)
  @Comment("재고")
  private int stock;

  private void checkStock() {
    if (stock < 1) {
      throw new IllegalArgumentException("재고가 부족합니다.");
    }
  }

  public void decreaseStock() {
    checkStock();
    stock = stock - 1;
  }
}
