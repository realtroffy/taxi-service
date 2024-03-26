package com.modsen.rideservice.service.impl;

import com.modsen.rideservice.dto.BankCardDto;
import com.modsen.rideservice.dto.CarDto;
import com.modsen.rideservice.dto.DriverPageDto;
import com.modsen.rideservice.dto.DriverRatingDto;
import com.modsen.rideservice.dto.DriverRideDto;
import com.modsen.rideservice.dto.PassengerDto;
import com.modsen.rideservice.dto.PassengerRatingFinishDto;
import com.modsen.rideservice.dto.RideDto;
import com.modsen.rideservice.dto.RideSearchDto;
import com.modsen.rideservice.exception.AlreadyGetRatingException;
import com.modsen.rideservice.exception.DriverServiceException;
import com.modsen.rideservice.exception.FinishDateEarlyThanStartDateException;
import com.modsen.rideservice.exception.PassengerBankCardNotEnoughMoneyException;
import com.modsen.rideservice.exception.RideStatusException;
import com.modsen.rideservice.exception.UnfinishedBookingRideException;
import com.modsen.rideservice.mapper.RideMapper;
import com.modsen.rideservice.model.PromoCode;
import com.modsen.rideservice.model.Ride;
import com.modsen.rideservice.model.Status;
import com.modsen.rideservice.repository.RideRepository;
import com.modsen.rideservice.service.DriverServiceFeignClient;
import com.modsen.rideservice.service.PromoCodeService;
import com.modsen.rideservice.service.RideService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

  public static final BigDecimal MIN_COST_FOR_RIDE = new BigDecimal("10.0");
  public static final BigDecimal MAX_COST_FOR_RIDE = new BigDecimal("20.0");
  public static final String NO_SUCH_RIDE_EXCEPTION_MESSAGE = "Ride was not found by id = ";

  private final RideRepository rideRepository;
  private final DriverServiceWebClient driverServiceWebClient;
  private final PassengerServiceWebClient passengerServiceWebClient;
  private final PromoCodeService promoCodeService;
  private final RideMapper rideMapper;
  private final MessageChannel toKafkaChannel;
  private final DriverServiceFeignClient driverServiceFeignClient;
  @PersistenceContext private final EntityManager entityManager;

  @Value(value = "${spring.kafka.topic-order-new-ride}")
  private String topicOrderNewRide;

  @Override
  @Transactional
  @CircuitBreaker(name = "CircuitBreakerRideService")
  @Retry(name = "retryRideService")
  public RideDto order(RideDto rideDto) {
    checkUnfinishedRide(rideDto);

    BigDecimal randomCost = generateRandomCost();
    rideDto.setCost(randomCost);

    PromoCode discountPromoCodeIfExist = getDiscountPromoCodeIfExist(rideDto, randomCost);

    checkPassengerExistAndHaveEnoughMoneyOnPassengerBankCard(rideDto);

    rideDto.setBookingTime(LocalDateTime.now());
    rideDto.setStatus(Status.PENDING);

    Ride ride = rideMapper.toEntity(rideDto);
    ride.setPromoCode(discountPromoCodeIfExist);
    Ride savedRide = rideRepository.save(ride);
    RideDto savedRideDto = rideMapper.toDto(savedRide);

    RideSearchDto rideSearchDto = RideSearchDto.builder().rideId(ride.getId()).build();

    toKafkaChannel.send(
        MessageBuilder.withPayload(rideSearchDto)
            .setHeader(KafkaHeaders.TOPIC, topicOrderNewRide)
            .build());

    return savedRideDto;
  }

  @Transactional
  public void getAvailableDriver(DriverRideDto driverRideDto) {
    Ride ride = getRide(driverRideDto.getRideId());
    if (ride.getStatus() == Status.PENDING) {
      ride.setStatus(Status.ACTIVE);
      ride.setDriverId(driverRideDto.getId());
      ride.setApprovedTime(LocalDateTime.now());
      ride.setStartTime(LocalDateTime.now());
      rideRepository.save(ride);
    } else {
      updateDriverAvailabilityAfterRide(ride);
    }
  }

  @Transactional
  public void getNotFoundDriver(DriverRideDto driverRideDto) {
    Ride ride = getRide(driverRideDto.getRideId());
    if (ride.getStatus() == Status.PENDING) {
      ride.setStatus(Status.NO_DRIVERS);
    }
  }

  private PromoCode getDiscountPromoCodeIfExist(RideDto rideDto, BigDecimal randomCost) {
    PromoCode promoCodeByName = null;
    if (rideDto.getPromoCodeName() != null) {
      promoCodeByName = promoCodeService.getByName(rideDto.getPromoCodeName());
      BigDecimal discount = promoCodeByName.getDiscount();
      rideDto.setCost(randomCost.multiply(discount));
    }
    return promoCodeByName;
  }

  private void checkPassengerExistAndHaveEnoughMoneyOnPassengerBankCard(RideDto rideDto) {
    ResponseEntity<PassengerDto> passengerDtoResponseEntity =
        passengerServiceWebClient.getPassengerDtoById(rideDto.getPassengerId());

    PassengerDto passengerDto = passengerDtoResponseEntity.getBody();
    if (passengerDto == null) {
      throw new NoSuchElementException(
          "Passenger was not found by such id = " + rideDto.getPassengerId());
    }
    Optional<BankCardDto> optionalBankCardDto =
        passengerDto.getBankCards().stream()
            .filter(
                bankCardDto ->
                    Objects.equals(
                        bankCardDto.getId(), Long.valueOf(rideDto.getPassengerBankCardId())))
            .findFirst();
    if (optionalBankCardDto.isEmpty()) {
      throw new NoSuchElementException(
          "Passenger bank card was not found by such id = " + rideDto.getPassengerBankCardId());
    } else {
      BankCardDto bankCardDto = optionalBankCardDto.get();
      if (bankCardDto.getBalance().compareTo(rideDto.getCost()) < 1) {
        throw new PassengerBankCardNotEnoughMoneyException(
            "Not enough money on your bank card. Choose another bank card to pay or pay cash");
      }
    }
  }

  private void checkUnfinishedRide(RideDto rideDto) {
    int size =
        rideRepository.findByPassengerIdAndFinishTimeNotNull(rideDto.getPassengerId()).size();
    if (size == 1) {
      throw new UnfinishedBookingRideException(
          "You have unfinished ride. You could order new ride after finished current ride");
    }
  }

  private BigDecimal generateRandomCost() {
    BigDecimal randomFactor = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble());
    BigDecimal range = MAX_COST_FOR_RIDE.subtract(MIN_COST_FOR_RIDE);
    BigDecimal scaled = randomFactor.multiply(range);
    return scaled.add(MIN_COST_FOR_RIDE).setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  @Transactional(readOnly = true)
  @CircuitBreaker(name = "CircuitBreakerRideService")
  @Retry(name = "retryRideService")
  public RideDto getById(long id) {
    Ride ride = getRide(id);
    CarDto carDto = null;
    if (ride.getDriverId() != null) {
      try {
        carDto = driverServiceFeignClient.getById(ride.getDriverId()).getBody().getCarDto();
      } catch (NullPointerException exception) {
        throw new DriverServiceException(
            "Driver service return null body or driver without car. May be driver or his car was deleted");
      }
    }
    RideDto rideDto = rideMapper.toDto(ride);
    if (carDto != null) {
      rideDto.setCarDto(carDto);
    }
    return rideDto;
  }

  private Ride getRide(long id) {
    return rideRepository
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException(NO_SUCH_RIDE_EXCEPTION_MESSAGE + id));
  }

  @Override
  @Transactional
  @CircuitBreaker(name = "CircuitBreakerRideService")
  @Retry(name = "retryRideService")
  public void finishByDriver(Long rideId, PassengerRatingFinishDto passengerRatingFinishDto) {
    Ride ride = updateRideAfterFinish(rideId, passengerRatingFinishDto);
    updateDriverAvailabilityAfterRide(ride);
    updatePassengerAfterRide(ride);
  }

  @Override
  @Transactional
  public RideDto cancelByPassenger(Long rideId) {
    Ride ride = getRide(rideId);
    Status rideStatus = ride.getStatus();
    if (rideStatus == Status.PENDING || rideStatus == Status.NO_DRIVERS) {
      ride.setStatus(Status.CANCELED);
      ride.setFinishTime(LocalDateTime.now());
      return rideMapper.toDto(ride);
    } else {
      throw new RideStatusException(
          "You could cancel ride only if it has status 'pending' or 'no drivers'");
    }
  }

  private void updateDriverAvailabilityAfterRide(Ride ride) {
    driverServiceWebClient.updateDriverAvailabilityToTrueAfterRide(ride.getDriverId());
  }

  private void updatePassengerAfterRide(Ride ride) {
    Double averagePassengerRatingByPassengerId =
        rideRepository.findAveragePassengerRatingByPassengerId(ride.getPassengerId());
    passengerServiceWebClient.updatePassengerAfterRide(ride, averagePassengerRatingByPassengerId);
  }

  private Ride updateRideAfterFinish(
      Long rideId, PassengerRatingFinishDto passengerRatingFinishDto) {
    Ride ride = getRide(rideId);

    if (ride.getStatus() != Status.ACTIVE) {
      throw new RideStatusException("You could finish only active ride");
    }

    ride.setFinishTime(LocalDateTime.now());
    ride.setStatus(Status.FINISHED);
    ride.setPassengerRating(passengerRatingFinishDto.getPassengerRating());

    rideRepository.save(ride);
    entityManager.flush();
    return ride;
  }

  @Override
  @Transactional
  @CircuitBreaker(name = "CircuitBreakerRideService")
  @Retry(name = "retryRideService")
  public void updateDriverRatingAfterRide(Long rideId, Integer driverRating) {
    Ride ride = getRide(rideId);
    if (ride.getDriverRating() != null) {
      throw new AlreadyGetRatingException("Driver already get rating for ride with id =" + rideId);
    }
    Status status = ride.getStatus();
    if (!(status == Status.FINISHED || status == Status.ACTIVE)) {
      throw new RideStatusException("You could rate driver only if ride is active or finished");
    }
    ride.setDriverRating(driverRating);
    entityManager.flush();
    Double averageDriverRatingByDriverId =
        rideRepository.findAverageDriverRatingByDriverId(ride.getDriverId());

    driverServiceWebClient.updateDriverRatingAfterRide(
        ride.getDriverId(),
        DriverRatingDto.builder().rating(averageDriverRatingByDriverId).build());
  }

  @Override
  @Transactional(readOnly = true)
  @CircuitBreaker(name = "CircuitBreakerRideService")
  @Retry(name = "retryRideService")
  public List<RideDto> getAll(Pageable pageable) {
    List<RideDto> rideDtoListWithDriver = getAllRidesWithDriver(pageable);

    List<Long> driversIdList = getListIdsFromRidesWhereExistDriverId(rideDtoListWithDriver);

    ResponseEntity<DriverPageDto> driverPageWithCars =
        getDriversFromDriverServiceByListIds(driversIdList);

    setCarDtoToRides(rideDtoListWithDriver, driverPageWithCars);

    List<RideDto> rideDtoListWithoutDriver = getAllRidesWithoutDriver(pageable);

    rideDtoListWithDriver.addAll(rideDtoListWithoutDriver);

    return rideDtoListWithDriver;
  }

  private List<RideDto> getAllRidesWithoutDriver(Pageable pageable) {
    return rideRepository.findAll(pageable).getContent().stream()
        .filter(ride -> ride.getDriverId() == null)
        .map(rideMapper::toDto)
        .collect(Collectors.toList());
  }

  private List<RideDto> getAllRidesWithDriver(Pageable pageable) {
    return rideRepository.findAll(pageable).getContent().stream()
        .filter(ride -> ride.getDriverId() != null)
        .map(rideMapper::toDto)
        .collect(Collectors.toList());
  }

  private void setCarDtoToRides(
      List<RideDto> rideDtoListWithoutCars, ResponseEntity<DriverPageDto> driverPageWithCars) {

    List<DriverRideDto> driverRideDtoListWithCars;
    try {
      driverRideDtoListWithCars = driverPageWithCars.getBody().getDriverDtoList();
    } catch (NullPointerException exception) {
      throw new DriverServiceException("Exception while get drivers with cars from driver service");
    }
    rideDtoListWithoutCars.forEach(
        rideWithoutCar -> {
          for (DriverRideDto driverWithCar : driverRideDtoListWithCars) {
            if (driverWithCar.getId() == rideWithoutCar.getDriverId()) {
              rideWithoutCar.setCarDto(driverWithCar.getCarDto());
            }
          }
        });
  }

  private ResponseEntity<DriverPageDto> getDriversFromDriverServiceByListIds(
      List<Long> driversIdList) {
    return driverServiceWebClient.getDriverPageDtoByListIdsDriver(driversIdList);
  }

  private List<Long> getListIdsFromRidesWhereExistDriverId(List<RideDto> rideDtoList) {
    return rideDtoList.stream()
        .map(RideDto::getDriverId)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void deleteById(long id) {
    getRide(id);
    rideRepository.deleteById(id);
  }

  @Override
  @Transactional
  public void update(long id, RideDto rideDto) {
    getRide(id);
    PromoCode promoCode = null;
    if (rideDto.getPromoCodeName() != null) {
      promoCode = promoCodeService.getByName(rideDto.getPromoCodeName());
    }
    checkDateOrder(rideDto);
    rideDto.setId(id);
    Ride ride = rideMapper.toEntity(rideDto);
    ride.setPromoCode(promoCode);
    rideRepository.save(ride);
  }

  private void checkDateOrder(RideDto rideDto) {
    List<LocalDateTime> localDateTimeList = new ArrayList<>();
    if (rideDto.getBookingTime() != null) {
      localDateTimeList.add(rideDto.getBookingTime());
    }
    if (rideDto.getApprovedTime() != null) {
      localDateTimeList.add(rideDto.getApprovedTime());
    }
    if (rideDto.getStartTime() != null) {
      localDateTimeList.add(rideDto.getStartTime());
    }
    if (rideDto.getFinishTime() != null) {
      localDateTimeList.add(rideDto.getFinishTime());
    }
    List<LocalDateTime> sortDate = localDateTimeList.stream().sorted().collect(Collectors.toList());

    if (!localDateTimeList.equals(sortDate)) {
      throw new FinishDateEarlyThanStartDateException(
          "Check date order. Booking -> Approved -> Start -> Finish");
    }
  }
}
