package com.modsen.rideservice.service.impl;

import com.modsen.rideservice.dto.BankCardBalanceDto;
import com.modsen.rideservice.dto.DriverDto;
import com.modsen.rideservice.dto.DriverPageDto;
import com.modsen.rideservice.dto.PassengerAfterRideDto;
import com.modsen.rideservice.dto.PromoCodeDto;
import com.modsen.rideservice.dto.RatingDto;
import com.modsen.rideservice.dto.RideDto;
import com.modsen.rideservice.exception.AlreadyFinishedRideException;
import com.modsen.rideservice.exception.DriverServiceAvailableDriversException;
import com.modsen.rideservice.exception.FinishDateEarlyThanStartDateException;
import com.modsen.rideservice.exception.PassengerBankCardNotEnoughMoneyException;
import com.modsen.rideservice.exception.PassengerBankCardNullException;
import com.modsen.rideservice.exception.UnfinishedBookingRideException;
import com.modsen.rideservice.mapper.RideMapper;
import com.modsen.rideservice.model.Ride;
import com.modsen.rideservice.repository.RideRepository;
import com.modsen.rideservice.service.PromoCodeService;
import com.modsen.rideservice.service.RideService;
import com.modsen.rideservice.service.WebClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

  public static final BigDecimal MIN_COST_FOR_RIDE = new BigDecimal("10.0");
  public static final BigDecimal MAX_COST_FOR_RIDE = new BigDecimal("20.0");
  public static final long TIME_TO_PASSENGER = 10;
  public static final String NO_SUCH_RIDE_EXCEPTION_MESSAGE = "Ride was not found by id = ";

  private final RideRepository rideRepository;
  private final WebClientService<DriverPageDto> webClientDriverServiceGetDriver;
  private final WebClientService<BankCardBalanceDto> webClientPassengerServiceGetBankCard;
  private final WebClientService<Void> webClientPassengerServiceUpdateAfterRideImpl;
  private final WebClientService<Void> webClientDriverServiceUpdateAfterRideImpl;
  private final PromoCodeService promoCodeService;
  private final RideMapper rideMapper;
  @PersistenceContext private final EntityManager entityManager;

  @Value(value = "${driver.service.available.free.driver.url}")
  private String driverServiceUrl;

  @Value(value = "${passenger.service.bank.card.url}")
  private String passengerServiceUpdateUrl;

  @Value(value = "${passenger.service.update.after.ride.url}")
  private String passengerServiceUpdateAfterRideUrl;

  @Value(value = "${driver.service.update.after.ride.url}")
  private String driverServiceUpdateAfterRideUrl;

  @Override
  @Transactional
  public RideDto order(RideDto rideDto) {
    checkUnfinishedRide(rideDto);

    BigDecimal randomCost = generateRandomCost();
    rideDto.setCost(randomCost);

    PromoCodeDto promoCodeDtoByName = getDiscountPromoCodeIfExist(rideDto, randomCost);

    checkEnoughMoneyOnPassengerBankCard(rideDto);

    rideDto.setBookingTime(LocalDateTime.now());

    List<DriverDto> driverDtoList = getAvailableDrivers();
    if (driverDtoList.isEmpty()) {
      throw new NoSuchElementException("Available driver was not found. Try later");
    } else {
      DriverDto driverDto = driverDtoList.get(0);
      rideDto.setApprovedTime(LocalDateTime.now());
      rideDto.setDriverId(driverDto.getId());
      rideDto.setStartTime(rideDto.getApprovedTime().plusMinutes(TIME_TO_PASSENGER));
      Ride ride = rideMapper.toEntity(rideDto);
      ride.setPromoCodeId(promoCodeDtoByName.getId());
      Ride savedRide = rideRepository.save(ride);
      RideDto savedDto = rideMapper.toDto(savedRide);
      savedDto.setCarDto(driverDto.getCarDto());
      savedDto.setPromoCodeName(promoCodeDtoByName.getName());
      return savedDto;
    }
  }

  private List<DriverDto> getAvailableDrivers() {
    DriverPageDto driverPageDto =
        webClientDriverServiceGetDriver.getResponseEntity(driverServiceUrl, null).getBody();
    if (driverPageDto == null) {
      throw new DriverServiceAvailableDriversException("Page of available drivers is null");
    }
    return driverPageDto.getDriverDtoList();
  }

  private PromoCodeDto getDiscountPromoCodeIfExist(RideDto rideDto, BigDecimal randomCost) {
    PromoCodeDto promoCodeDtoByName = new PromoCodeDto();
    if (rideDto.getPromoCodeName() != null) {
      promoCodeDtoByName = promoCodeService.getByName(rideDto.getPromoCodeName());
      BigDecimal discount = promoCodeDtoByName.getDiscount();
      rideDto.setCost(randomCost.multiply(discount));
    }
    return promoCodeDtoByName;
  }

  private void checkEnoughMoneyOnPassengerBankCard(RideDto rideDto) {
    if (rideDto.getPassengerBankCardId() != null) {
      BankCardBalanceDto bankCardBalanceDto =
          webClientPassengerServiceGetBankCard
              .getResponseEntity(
                  passengerServiceUpdateUrl + "/" + rideDto.getPassengerBankCardId(), null)
              .getBody();
      if (bankCardBalanceDto == null) {
        throw new PassengerBankCardNullException("Passenger bank card is null");
      }
      if (bankCardBalanceDto.getBalance().compareTo(rideDto.getCost()) < 1) {
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
  public RideDto getById(long id) {
    Ride ride = getRide(id);
    return rideMapper.toDto(ride);
  }

  private Ride getRide(long id) {
    return rideRepository
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException(NO_SUCH_RIDE_EXCEPTION_MESSAGE + id));
  }

  @Override
  @Transactional
  public void finish(Long rideId, RatingDto ratingDto) {
    Ride ride = updateRideAfterFinish(rideId, ratingDto);

    updatePassengerAfterRide(ride);

    updateDriverAfterRide(ride);
  }

  private void updateDriverAfterRide(Ride ride) {
    Double averageDriverRatingByDriverId =
        rideRepository.findAverageDriverRatingByDriverId(ride.getDriverId());

    webClientDriverServiceUpdateAfterRideImpl.getResponseEntity(
        driverServiceUpdateAfterRideUrl
            + "/"
            + ride.getDriverId()
            + "/"
            + averageDriverRatingByDriverId,
        null);
  }

  private void updatePassengerAfterRide(Ride ride) {
    Double averagePassengerRatingByPassengerId =
        rideRepository.findAveragePassengerRatingByPassengerId(ride.getPassengerId());

    webClientPassengerServiceUpdateAfterRideImpl.getResponseEntity(
        passengerServiceUpdateAfterRideUrl + "/" + ride.getPassengerId(),
        PassengerAfterRideDto.builder()
            .passengerRating(averagePassengerRatingByPassengerId)
            .rideCost(ride.getCost())
            .passengerBankCardId(ride.getPassengerBankCardId())
            .build());
  }

  private Ride updateRideAfterFinish(Long rideId, RatingDto ratingDto) {
    Ride ride = getRide(rideId);

    if (ride.getFinishTime() != null) {
      throw new AlreadyFinishedRideException("Ride is already finished");
    }

    ride.setFinishTime(LocalDateTime.now());
    ride.setDriverRating(ratingDto.getDriverRating());
    ride.setPassengerRating(ratingDto.getPassengerRating());

    rideRepository.save(ride);
    entityManager.flush();
    return ride;
  }

  @Override
  @Transactional(readOnly = true)
  public List<RideDto> getAll(Pageable pageable) {
    List<RideDto> rideDtoList = new ArrayList<>();
    rideRepository
        .findAll(pageable)
        .getContent()
        .forEach(ride -> rideDtoList.add(rideMapper.toDto(ride)));
    return rideDtoList;
  }

  @Override
  @Transactional
  public void deleteById(long id) {
    rideRepository.deleteById(id);
  }

  @Override
  @Transactional
  public void update(long id, RideDto rideDto) {
    if (rideDto.getPromoCodeName() != null) {
      promoCodeService.getByName(rideDto.getPromoCodeName());
    }
    checkDateOrder(rideDto);
    getRide(id);
    rideDto.setId(id);
    Ride ride = rideMapper.toEntity(rideDto);
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
