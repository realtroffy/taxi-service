package com.modsen.rideservice.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
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
@Table(name = "rides")
public class Ride implements Serializable {

  private static final long serialVersionUID = 7844962484586042640L;
  public static final BigDecimal MIN_COST_FOR_RIDE = new BigDecimal("10.0");
  public static final BigDecimal MAX_COST_FOR_RIDE = new BigDecimal("20.0");
  public static final int TIME_TO_PASSENGER = 10;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "start_location")
  private String startLocation;

  @Column(name = "end_location")
  private String endLocation;

  @Column(name = "passenger_id")
  private Long passengerId;

  @Column(name = "driver_id")
  private Long driverId;

  @Column(name = "driver_rating")
  private Integer driverRating;

  @Column(name = "passenger_rating")
  private Integer passengerRating;

  @Column(name = "booking_time")
  private LocalDateTime bookingTime;

  @Column(name = "approved_time")
  private LocalDateTime approvedTime;

  @Column(name = "start_time")
  private LocalDateTime startTime;

  @Column(name = "finish_time")
  private LocalDateTime finishTime;

  @Column(name = "passenger_bank_card_id")
  private Long passengerBankCardId;

  @ManyToOne(fetch = FetchType.LAZY)
  @ToString.Exclude
  private PromoCode promoCode;

  @Column(name = "cost", nullable = false)
  private BigDecimal cost;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private Status status;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Ride ride = (Ride) o;
    return Objects.equals(id, ride.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
