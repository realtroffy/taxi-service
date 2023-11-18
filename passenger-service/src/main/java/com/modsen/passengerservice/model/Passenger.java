package com.modsen.passengerservice.model;

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
import javax.persistence.PrePersist;
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
@Table(name = "passengers")
public class Passenger implements Serializable {

  private static final long serialVersionUID = 4603749101621919833L;

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

  @OneToMany(
      mappedBy = "passenger",
      cascade = {CascadeType.ALL},
      orphanRemoval = true)
  @ToString.Exclude
  private List<BankCard> bankCards = new ArrayList<>();

  public void addCard(BankCard bankCard) {
    bankCards.add(bankCard);
    bankCard.setPassenger(this);
  }

  public void removeBankCard(BankCard bankCard) {
    bankCards.remove(bankCard);
    bankCard.setPassenger(null);
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
    Passenger passenger = (Passenger) o;
    return Objects.equals(id, passenger.id) && Objects.equals(email, passenger.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, email);
  }
}
