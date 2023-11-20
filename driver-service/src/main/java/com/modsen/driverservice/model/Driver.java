package com.modsen.driverservice.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "drivers")
public class Driver implements Serializable {

  private static final long serialVersionUID = -8233816174403483575L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, name = "first_name")
  private String firstName;

  @Column(nullable = false, name = "last_name")
  private String lastName;

  @Column(nullable = false)
  private Double rating;

  @Column(name = "is_available", nullable = false)
  private boolean isAvailable;

  @OneToMany(
      mappedBy = "driver",
      cascade = {CascadeType.ALL},
      orphanRemoval = true)
  @ToString.Exclude
  private List<BankCard> bankCards = new ArrayList<>();

  @OneToOne(mappedBy = "driver", cascade = CascadeType.MERGE)
  @PrimaryKeyJoinColumn
  @ToString.Exclude
  private Car car;

  public void addCard(BankCard bankCard) {
    bankCards.add(bankCard);
    bankCard.setDriver(this);
  }

  public void removeBankCard(BankCard bankCard) {
    bankCards.remove(bankCard);
    bankCard.setDriver(null);
  }

  @PrePersist
  public void prePersist() {
    if (rating != null) {
      rating = Math.round(rating * 10.0) / 10.0;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Driver driver = (Driver) o;
    return Objects.equals(id, driver.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
