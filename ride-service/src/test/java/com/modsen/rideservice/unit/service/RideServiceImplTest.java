package com.modsen.rideservice.unit.service;

import com.modsen.rideservice.dto.BankCardDto;
import com.modsen.rideservice.dto.CarDto;
import com.modsen.rideservice.dto.DriverPageDto;
import com.modsen.rideservice.dto.DriverRatingDto;
import com.modsen.rideservice.dto.DriverRideDto;
import com.modsen.rideservice.dto.DriverWithCarDto;
import com.modsen.rideservice.dto.PassengerDto;
import com.modsen.rideservice.dto.PassengerRatingFinishDto;
import com.modsen.rideservice.dto.RideDto;
import com.modsen.rideservice.exception.AlreadyGetRatingException;
import com.modsen.rideservice.exception.FinishDateEarlyThanStartDateException;
import com.modsen.rideservice.exception.PassengerBankCardNotEnoughMoneyException;
import com.modsen.rideservice.exception.RideStatusException;
import com.modsen.rideservice.exception.UnfinishedBookingRideException;
import com.modsen.rideservice.mapper.RideMapper;
import com.modsen.rideservice.model.PromoCode;
import com.modsen.rideservice.model.Ride;
import com.modsen.rideservice.model.Status;
import com.modsen.rideservice.repository.RideRepository;
import com.modsen.rideservice.service.PromoCodeService;
import com.modsen.rideservice.service.impl.DriverServiceWebClient;
import com.modsen.rideservice.service.impl.PassengerServiceWebClient;
import com.modsen.rideservice.service.impl.RideServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideServiceImplTest {

  public static final Long EXIST_PASSENGER_ID = 1L;
  public static final Long EXIST_BANK_CARD_ID = 1L;
  public static final Long EXIST_RIDE_ID = 1L;
  public static final Long EXIST_DRIVER_ID = 1L;
  public static final Long NOT_EXIST_ID = 30L;
  public static final Integer RATING_AFTER_RIDE = 3;
  public static final BigDecimal ENOUGH_MONEY_BALANCE_ON_BANK_CARD = BigDecimal.valueOf(1000);
  public static final String EXIST_PROMO_CODE_NAME = "SUPER20";

  @Mock private RideRepository rideRepository;
  @Mock private DriverServiceWebClient driverServiceWebClient;
  @Mock private PassengerServiceWebClient passengerServiceWebClient;
  @Mock private PromoCodeService promoCodeService;
  @Mock private RideMapper rideMapper;
  @Mock private MessageChannel toKafkaChannel;
  @Mock private EntityManager entityManager;
  @InjectMocks private RideServiceImpl rideService;

  private RideDto rideDto;
  private Ride ride;
  private DriverWithCarDto driverWithCarDto;
  private DriverPageDto driverPageDto;
  private PromoCode promoCode;
  private DriverRideDto driverRideDto;
  private PassengerDto passengerDto;
  private BankCardDto bankCardDto;

  @BeforeEach
  protected void setUp() {
    rideDto =
        RideDto.builder()
            .id(1L)
            .startLocation("Minsk")
            .endLocation("London")
            .passengerId(EXIST_PASSENGER_ID)
            .driverId(EXIST_DRIVER_ID)
            .bookingTime(LocalDateTime.of(2023, 10, 10, 10, 10))
            .passengerBankCardId("1")
            .cost(BigDecimal.valueOf(10.12))
            .promoCodeName(EXIST_PROMO_CODE_NAME)
            .build();

    ride = new Ride();
    ride.setId(1L);
    ride.setStartLocation("Minsk");
    ride.setEndLocation("London");
    ride.setPassengerId(EXIST_PASSENGER_ID);
    ride.setDriverId(EXIST_DRIVER_ID);
    ride.setBookingTime(LocalDateTime.of(2023, 10, 10, 10, 10));
    ride.setPassengerBankCardId("3");
    ride.setCost(BigDecimal.valueOf(10.12));

    CarDto carDto =
        CarDto.builder()
            .driverId(EXIST_DRIVER_ID)
            .colour("green")
            .model("Lada vesta")
            .number("1123-AC7")
            .build();

    driverRideDto =
        DriverRideDto.builder()
            .id(EXIST_DRIVER_ID)
            .rideId(EXIST_RIDE_ID)
            .firstName("Ivan")
            .lastName("Ivanou")
            .carDto(carDto)
            .build();

    driverPageDto = new DriverPageDto();
    driverPageDto.setDriverDtoList(List.of(driverRideDto));

    driverWithCarDto = DriverWithCarDto.builder().carDto(carDto).build();

    promoCode = new PromoCode();
    promoCode.setId(1L);
    promoCode.setName("SUPER20");
    promoCode.setDiscount(BigDecimal.valueOf(0.2));
    promoCode.setStart(LocalDateTime.of(2022, 10, 10, 10, 10));
    promoCode.setEnd(LocalDateTime.of(2024, 10, 10, 10, 10));

    bankCardDto =
        BankCardDto.builder()
            .id(EXIST_BANK_CARD_ID)
            .balance(ENOUGH_MONEY_BALANCE_ON_BANK_CARD)
            .build();

    passengerDto =
        PassengerDto.builder().id(EXIST_PASSENGER_ID).bankCards(List.of(bankCardDto)).build();
  }

  @Test
  void getDtoById() {
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));
    when(driverServiceWebClient.getDriverById(EXIST_DRIVER_ID))
        .thenReturn(ResponseEntity.ok(driverWithCarDto));
    when(rideMapper.toDto(ride)).thenReturn(rideDto);

    RideDto actual = rideService.getById(EXIST_RIDE_ID);

    assertNotNull(actual);
    verify(rideRepository).findById(EXIST_RIDE_ID);
    verify(driverServiceWebClient).getDriverById(EXIST_DRIVER_ID);
    verify(rideMapper).toDto(ride);
  }

  @Test
  void getDtoByIdIfNotExistThanThrowNotSuchElementException() {
    when(rideRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> rideService.getById(NOT_EXIST_ID));
  }

  @Test
  void getAll() {
    when(rideRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(ride)));
    when(rideMapper.toDto(ride)).thenReturn(rideDto);
    when(driverServiceWebClient.getDriverPageDtoByListIdsDriver(List.of(EXIST_DRIVER_ID)))
        .thenReturn(ResponseEntity.ok(driverPageDto));

    List<RideDto> actual = rideService.getAll(PageRequest.of(0, 10));

    assertNotNull(actual);
    assertSame(1, actual.size());
    verify(rideRepository, times(2)).findAll(any(Pageable.class));
    verify(rideMapper).toDto(ride);
  }

  @Test
  void deleteById() {
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));

    rideService.deleteById(EXIST_RIDE_ID);

    verify(rideRepository).deleteById(EXIST_RIDE_ID);
  }

  @Test
  void deleteByIdIfNotExistThrowNoSuchElementException() {
    when(rideRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> rideService.deleteById(NOT_EXIST_ID));
    verify(rideRepository, never()).deleteById(NOT_EXIST_ID);
  }

  @Test
  void updateIfPromoCodeExist() {
    ride.setPromoCode(promoCode);
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));
    when(promoCodeService.getByName(ride.getPromoCode().getName())).thenReturn(promoCode);
    when(rideMapper.toEntity(rideDto)).thenReturn(ride);
    when(rideRepository.save(ride)).thenReturn(ride);

    rideService.update(EXIST_RIDE_ID, rideDto);

    verify(rideRepository).findById(anyLong());
    verify(promoCodeService).getByName(ride.getPromoCode().getName());
    verify(rideRepository).save(ride);
    verify(rideMapper).toEntity(rideDto);
  }

  @Test
  void updateIfPromoCodeNotExist() {
    rideDto.setPromoCodeName(null);
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));
    when(rideMapper.toEntity(rideDto)).thenReturn(ride);
    when(rideRepository.save(ride)).thenReturn(ride);

    rideService.update(EXIST_RIDE_ID, rideDto);

    verify(rideRepository).findById(anyLong());
    verify(rideRepository).save(ride);
    verify(rideMapper).toEntity(rideDto);
  }

  @Test
  void updateIfNotExistThrowNoSuchElementException() {
    when(rideRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> rideService.update(NOT_EXIST_ID, rideDto));
    verify(rideRepository, never()).save(ride);
  }

  @Test
  void updateIfDatesHaveIncorrectOrder() {
    rideDto.setPromoCodeName(null);
    rideDto.setApprovedTime(rideDto.getBookingTime().minusHours(10));
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));

    assertThrows(
        FinishDateEarlyThanStartDateException.class,
        () -> rideService.update(EXIST_RIDE_ID, rideDto));
    verify(rideRepository, never()).save(ride);
  }

  @Test
  void updateDriverRatingAfterRideIfRideExistAndStatusFinishedAndDriverNotTakeRatingYet() {
    DriverRatingDto driverRatingDto = DriverRatingDto.builder().rating(3.0).build();
    ride.setStatus(Status.FINISHED);
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));
    doNothing().when(entityManager).flush();
    when(rideRepository.findAverageDriverRatingByDriverId(EXIST_DRIVER_ID)).thenReturn(3.0);
    doNothing()
        .when(driverServiceWebClient)
        .updateDriverRatingAfterRide(ride.getDriverId(), driverRatingDto);

    rideService.updateDriverRatingAfterRide(EXIST_RIDE_ID, 3);

    verify(entityManager).flush();
    verify(rideRepository).findAverageDriverRatingByDriverId(ride.getDriverId());
    verify(rideRepository).findById(EXIST_RIDE_ID);
    verify(driverServiceWebClient).updateDriverRatingAfterRide(ride.getDriverId(), driverRatingDto);
  }

  @Test
  void updateDriverRatingAfterRideIfRideExistAndStatusFinishedAndDriverAlreadyTakeRating() {
    DriverRatingDto driverRatingDto = DriverRatingDto.builder().rating(3.0).build();
    ride.setStatus(Status.FINISHED);
    ride.setDriverRating(3);
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));

    assertThrows(
        AlreadyGetRatingException.class,
        () -> rideService.updateDriverRatingAfterRide(EXIST_RIDE_ID, 3));
    verify(driverServiceWebClient, never())
        .updateDriverRatingAfterRide(ride.getDriverId(), driverRatingDto);
  }

  @Test
  void updateDriverRatingAfterRideIfRideExistAndStatusNotFinishedOrActive() {
    DriverRatingDto driverRatingDto = DriverRatingDto.builder().rating(3.0).build();
    ride.setStatus(Status.PENDING);
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));

    assertThrows(
        RideStatusException.class, () -> rideService.updateDriverRatingAfterRide(EXIST_RIDE_ID, 3));
    verify(driverServiceWebClient, never())
        .updateDriverRatingAfterRide(ride.getDriverId(), driverRatingDto);
  }

  @Test
  void updateDriverRatingAfterRideIfRideNotExist() {
    DriverRatingDto driverRatingDto = DriverRatingDto.builder().rating(3.0).build();
    when(rideRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(
        NoSuchElementException.class,
        () -> rideService.updateDriverRatingAfterRide(NOT_EXIST_ID, 3));
    verify(driverServiceWebClient, never())
        .updateDriverRatingAfterRide(ride.getDriverId(), driverRatingDto);
  }

  @Test
  void cancelByPassengerIfRideExistAndStatusPending() {
    ride.setStatus(Status.PENDING);
    rideDto.setStatus(Status.CANCELED);
    rideDto.setFinishTime(LocalDateTime.now());
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));
    when(rideMapper.toDto(ride)).thenReturn(rideDto);

    RideDto actualCanceledRide = rideService.cancelByPassenger(EXIST_RIDE_ID);
    Status actualStatus = actualCanceledRide.getStatus();

    assertSame(Status.CANCELED, actualStatus);
    assertNotNull(actualCanceledRide.getFinishTime());
    verify(rideRepository).findById(EXIST_RIDE_ID);
    verify(rideMapper).toDto(ride);
  }

  @Test
  void cancelByPassengerIfRideExistAndStatusActive() {
    ride.setStatus(Status.ACTIVE);
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));

    assertThrows(RideStatusException.class, () -> rideService.cancelByPassenger(EXIST_RIDE_ID));
  }

  @Test
  void cancelByPassengerIfRideNotExist() {
    when(rideRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> rideService.cancelByPassenger(NOT_EXIST_ID));
  }

  @Test
  void finishByDriverIfRideExistAndStatusActive() {
    PassengerRatingFinishDto passengerRatingFinishDto = new PassengerRatingFinishDto();
    passengerRatingFinishDto.setPassengerRating(RATING_AFTER_RIDE);
    ride.setStatus(Status.ACTIVE);
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));
    when(rideRepository.save(ride)).thenReturn(ride);
    doNothing().when(entityManager).flush();
    doNothing().when(driverServiceWebClient).updateDriverAvailabilityToTrueAfterRide(EXIST_RIDE_ID);
    when(rideRepository.findAveragePassengerRatingByPassengerId(EXIST_PASSENGER_ID))
        .thenReturn(RATING_AFTER_RIDE.doubleValue());
    doNothing()
        .when(passengerServiceWebClient)
        .updatePassengerAfterRide(ride, RATING_AFTER_RIDE.doubleValue());

    rideService.finishByDriver(EXIST_RIDE_ID, passengerRatingFinishDto);

    verify(rideRepository).findById(EXIST_RIDE_ID);
    verify(rideRepository).save(ride);
    verify(entityManager).flush();
    verify(driverServiceWebClient).updateDriverAvailabilityToTrueAfterRide(EXIST_DRIVER_ID);
    verify(rideRepository).findAveragePassengerRatingByPassengerId(EXIST_PASSENGER_ID);
    verify(passengerServiceWebClient)
        .updatePassengerAfterRide(ride, RATING_AFTER_RIDE.doubleValue());
  }

  @Test
  void finishByDriverIfRideExistAndStatusNotActive() {
    PassengerRatingFinishDto passengerRatingFinishDto = new PassengerRatingFinishDto();
    passengerRatingFinishDto.setPassengerRating(RATING_AFTER_RIDE);
    ride.setStatus(Status.PENDING);
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));

    assertThrows(
        RideStatusException.class,
        () -> rideService.finishByDriver(EXIST_RIDE_ID, passengerRatingFinishDto));

    verify(rideRepository).findById(EXIST_RIDE_ID);
    verify(rideRepository, never()).save(ride);
  }

  @Test
  void finishByDriverIfRideNotExist() {
    PassengerRatingFinishDto passengerRatingFinishDto = new PassengerRatingFinishDto();
    passengerRatingFinishDto.setPassengerRating(RATING_AFTER_RIDE);
    when(rideRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(
        NoSuchElementException.class,
        () -> rideService.finishByDriver(NOT_EXIST_ID, passengerRatingFinishDto));
  }

  @Test
  void getAvailableDriverIfRideExistAndStatusPending() {
    ride.setStatus(Status.PENDING);
    ride.setDriverId(null);
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));
    when(rideRepository.save(ride)).thenReturn(ride);

    rideService.getAvailableDriver(driverRideDto);

    assertSame(Status.ACTIVE, ride.getStatus());
    assertNotNull(ride.getDriverId());
    assertNotNull(ride.getApprovedTime());
    assertNotNull(ride.getStartTime());
    verify(rideRepository).findById(EXIST_RIDE_ID);
    verify(rideRepository).save(ride);
  }

  @Test
  void getAvailableDriverIfRideExistAndStatusNotPending() {
    ride.setStatus(Status.ACTIVE);
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));
    doNothing()
        .when(driverServiceWebClient)
        .updateDriverAvailabilityToTrueAfterRide(EXIST_DRIVER_ID);

    rideService.getAvailableDriver(driverRideDto);

    assertSame(Status.ACTIVE, ride.getStatus());
    assertNull(ride.getApprovedTime());
    assertNull(ride.getStartTime());
    verify(rideRepository).findById(EXIST_RIDE_ID);
  }

  @Test
  void getAvailableDriverIfRideNotExist() {
    driverRideDto.setRideId(NOT_EXIST_ID);
    when(rideRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> rideService.getAvailableDriver(driverRideDto));
  }

  @Test
  void getNotFoundDriverIfStatusPending() {
    ride.setStatus(Status.PENDING);
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));

    rideService.getNotFoundDriver(driverRideDto);

    assertEquals(Status.NO_DRIVERS, ride.getStatus());
    verify(rideRepository).findById(EXIST_RIDE_ID);
  }

  @Test
  void getNotFoundDriverIfStatusNotPending() {
    ride.setStatus(Status.PENDING);
    when(rideRepository.findById(EXIST_RIDE_ID)).thenReturn(Optional.of(ride));

    rideService.getNotFoundDriver(driverRideDto);

    assertEquals(Status.NO_DRIVERS, ride.getStatus());
    verify(rideRepository).findById(EXIST_RIDE_ID);
  }

  @Test
  void getNotFoundDriverIfRideNotExist() {
    driverRideDto.setRideId(NOT_EXIST_ID);
    when(rideRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> rideService.getNotFoundDriver(driverRideDto));
  }

  @Test
  void orderRideIfPassengerPayBankCardAndHaveEnoughMoneyWithValidPromoCode() {
    rideDto.setBookingTime(null);
    when(rideRepository.findByPassengerIdAndFinishTimeNotNull(EXIST_PASSENGER_ID))
        .thenReturn(Collections.emptyList());
    when(promoCodeService.getByName(EXIST_PROMO_CODE_NAME)).thenReturn(promoCode);
    when(passengerServiceWebClient.getPassengerDtoById(EXIST_PASSENGER_ID))
        .thenReturn(ResponseEntity.ok(passengerDto));
    when(rideMapper.toEntity(rideDto)).thenReturn(ride);
    when(rideRepository.save(ride)).thenReturn(ride);
    when(rideMapper.toDto(ride)).thenReturn(rideDto);
    when(toKafkaChannel.send(any(Message.class))).thenReturn(true);

    rideService.order(rideDto);

    assertEquals(Status.PENDING, rideDto.getStatus());
    assertNotNull(rideDto.getBookingTime());
    verify(rideRepository).findByPassengerIdAndFinishTimeNotNull(EXIST_PASSENGER_ID);
    verify(promoCodeService).getByName(EXIST_PROMO_CODE_NAME);
    verify(passengerServiceWebClient).getPassengerDtoById(EXIST_PASSENGER_ID);
    verify(rideMapper).toEntity(rideDto);
    verify(rideRepository).save(ride);
    verify(rideMapper).toDto(ride);
    verify(toKafkaChannel).send(any(Message.class));
  }

  @Test
  void orderRideIfPassengerHasUnfinishedRide() {
    when(rideRepository.findByPassengerIdAndFinishTimeNotNull(EXIST_PASSENGER_ID))
        .thenReturn(List.of(ride));

    assertThrows(UnfinishedBookingRideException.class, () -> rideService.order(rideDto));
  }

  @Test
  void orderRideIfPromoCodeIncorrectOrNotFound() {
    when(rideRepository.findByPassengerIdAndFinishTimeNotNull(EXIST_PASSENGER_ID))
        .thenReturn(Collections.emptyList());
    when(promoCodeService.getByName(EXIST_PROMO_CODE_NAME)).thenThrow(NoSuchElementException.class);

    assertThrows(NoSuchElementException.class, () -> rideService.order(rideDto));
  }

  @Test
  void orderRideIfPassengerNotFoundById() {
    when(rideRepository.findByPassengerIdAndFinishTimeNotNull(EXIST_PASSENGER_ID))
        .thenReturn(Collections.emptyList());
    when(promoCodeService.getByName(EXIST_PROMO_CODE_NAME)).thenReturn(promoCode);
    when(passengerServiceWebClient.getPassengerDtoById(EXIST_PASSENGER_ID))
        .thenReturn(ResponseEntity.badRequest().body(null));

    assertThrows(NoSuchElementException.class, () -> rideService.order(rideDto));
  }

  @Test
  void orderRideIfPassengerBankCardNotFoundById() {
    passengerDto.setBankCards(Collections.emptyList());
    when(rideRepository.findByPassengerIdAndFinishTimeNotNull(EXIST_PASSENGER_ID))
        .thenReturn(Collections.emptyList());
    when(promoCodeService.getByName(EXIST_PROMO_CODE_NAME)).thenReturn(promoCode);
    when(passengerServiceWebClient.getPassengerDtoById(EXIST_PASSENGER_ID))
        .thenReturn(ResponseEntity.ok(passengerDto));

    assertThrows(NoSuchElementException.class, () -> rideService.order(rideDto));
  }

  @Test
  void orderRideIfPassengerHaveNotEnoughMoneyOnBankCard() {
    bankCardDto.setBalance(BigDecimal.ZERO);
    when(rideRepository.findByPassengerIdAndFinishTimeNotNull(EXIST_PASSENGER_ID))
        .thenReturn(Collections.emptyList());
    when(promoCodeService.getByName(EXIST_PROMO_CODE_NAME)).thenReturn(promoCode);
    when(passengerServiceWebClient.getPassengerDtoById(EXIST_PASSENGER_ID))
        .thenReturn(ResponseEntity.ok(passengerDto));

    assertThrows(PassengerBankCardNotEnoughMoneyException.class, () -> rideService.order(rideDto));
  }
}
