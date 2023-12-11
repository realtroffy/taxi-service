package com.modsen.passengerservice.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "bank_cards")
public class BankCard implements Serializable {

  private static final long serialVersionUID = -5846079447491163597L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @ToString.Exclude
  private Passenger passenger;

  @Column(name = "card_number", nullable = false, unique = true)
  private String cardNumber;

  @Column(name = "balance", nullable = false)
  private BigDecimal balance;

  @Column(name = "is_default", nullable = false)
  private boolean isDefault;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BankCard bankCard = (BankCard) o;
    return Objects.equals(id, bankCard.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
