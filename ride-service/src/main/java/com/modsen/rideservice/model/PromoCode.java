package com.modsen.rideservice.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "promo_codes")
public class PromoCode implements Serializable {

  private static final long serialVersionUID = 4732487333554991204L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String name;

  @Column(nullable = false)
  private BigDecimal discount;

  @Column(name = "start_date", nullable = false)
  private LocalDateTime start;

  @Column(name = "finish_time", nullable = false)
  private LocalDateTime end;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PromoCode promoCode = (PromoCode) o;
    return Objects.equals(id, promoCode.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
