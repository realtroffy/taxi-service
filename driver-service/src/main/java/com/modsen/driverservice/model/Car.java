package com.modsen.driverservice.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "cars")
public class Car implements Serializable {

  private static final long serialVersionUID = 112560736894642598L;

  @Id
  @Column(name = "driver_id")
  private Long id;

  private String model;

  private String colour;

  @Column(unique = true)
  private String number;

  @OneToOne
  @MapsId
  @JoinColumn(name = "driver_id")
  @ToString.Exclude
  private Driver driver;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Car car = (Car) o;
    return Objects.equals(id, car.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
