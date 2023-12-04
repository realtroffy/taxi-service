package com.modsen.driverservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.driverservice.dto.DriverDto;
import com.modsen.driverservice.dto.DriverRideDto;
import com.modsen.driverservice.dto.RideSearchDto;
import com.modsen.driverservice.exception.DriverWithoutCarAvailableException;
import com.modsen.driverservice.exception.RideSearchDtoMappingException;
import com.modsen.driverservice.mapper.BankCardMapper;
import com.modsen.driverservice.mapper.DriverDtoToDriverRideDtoMapper;
import com.modsen.driverservice.mapper.DriverMapper;
import com.modsen.driverservice.model.BankCard;
import com.modsen.driverservice.model.Driver;
import com.modsen.driverservice.repository.DriverRepository;
import com.modsen.driverservice.service.BankCardService;
import com.modsen.driverservice.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

  public static final String NO_SUCH_DRIVER_EXCEPTION_MESSAGE = "Driver was not found by id = ";
  public static final Double DEFAULT_RATING_NEW_DRIVER = 5.0;
  public static final boolean DEFAULT_AVAILABILITY_NEW_DRIVER = false;
  private final DriverRepository driverRepository;
  private final BankCardService bankCardService;
  private final BankCardMapper bankCardMapper;
  private final DriverMapper driverMapper;
  private final DriverDtoToDriverRideDtoMapper driverRideDtoMapper;
  private final ObjectMapper objectMapper;

  @Value(value = "${spring.kafka.topic.order.new.ride}")
  private String orderNewRideTopic;

  @Value(value = "${spring.kafka.topic.available.driver}")
  private String availableDriverTopic;

  @Value(value = "${spring.kafka.topic.not.found.driver}")
  private String notFoundDriverTopic;

  @Override
  @Transactional(readOnly = true)
  public DriverDto getById(long id) {
    Driver driver = getDriver(id);
    DriverDto driverDto = driverMapper.toDto(driver);
    driverDto.setRating(driver.getRating());
    return driverDto;
  }

  @Override
  @Transactional(readOnly = true)
  public Driver getDriverById(long id) {
    return getDriver(id);
  }

  private Driver getDriver(long id) {
    return driverRepository
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException(NO_SUCH_DRIVER_EXCEPTION_MESSAGE + id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<DriverDto> getAll(Pageable pageable) {
    Page<Long> ids = driverRepository.findAllIds(pageable);
    return getDriverDtos(pageable, ids);
  }

  @Override
  @Transactional
  public DriverDto save(DriverDto driverDto) {
    Driver driver = driverMapper.toEntity(driverDto);
    driver.setAvailable(DEFAULT_AVAILABILITY_NEW_DRIVER);
    driver.setRating(DEFAULT_RATING_NEW_DRIVER);
    Driver createdDriver = driverRepository.save(driver);
    return driverMapper.toDto(createdDriver);
  }

  @Override
  @Transactional
  public void deleteById(long id) {
    driverRepository.deleteById(id);
  }

  @Override
  @Transactional
  public void update(long id, DriverDto driverDto) {
    if (driverDto.isAvailable() && driverDto.getCarDto() == null) {
      throw new DriverWithoutCarAvailableException(
          "Driver could not switch status to available without car");
    }
    getDriver(id);
    driverDto.setId(id);
    Driver driver = driverMapper.toEntity(driverDto);
    driverRepository.save(driver);
  }

  @Override
  @Transactional
  public DriverDto updateRating(long id, double rating) {
    Driver driver = getDriver(id);
    driver.setRating(rating);
    DriverDto driverDto = driverMapper.toDto(driverRepository.save(driver));
    driverDto.setRating(rating);
    return driverDto;
  }

  @Override
  @Transactional
  public void addBankCardToDriver(long driverId, long bankCardId) {
    Driver driver = getDriver(driverId);
    BankCard bankCard = bankCardMapper.toEntity(bankCardService.getById(bankCardId));
    driver.addCard(bankCard);
  }

  @Override
  @Transactional
  public void removeBankCardToDriver(long driverId, long bankCardId) {
    Driver driver = getDriver(driverId);
    BankCard bankCard = bankCardMapper.toEntity(bankCardService.getById(bankCardId));
    driver.removeBankCard(bankCard);
  }

  private List<DriverDto> getDriverDtos(Pageable pageable, Page<Long> ids) {
    List<DriverDto> drivers = new ArrayList<>();
    List<Driver> byIdIn = driverRepository.findByIdIn(ids.toList(), pageable.getSort());
    byIdIn.forEach(
        driver -> {
          DriverDto driverDto = driverMapper.toDto(driver);
          drivers.add(driverDto);
        });

    return drivers;
  }

  @Transactional
  @Autowired
  public void getFreeRandomDriverIfExistAndChangeAvailabilityToFalse(
      StreamsBuilder kStreamBuilder) {
    KStream<String, String> stream =
        kStreamBuilder.stream(orderNewRideTopic, Consumed.with(Serdes.String(), Serdes.String()));

    KStream<String, RideSearchDto> rideSearchDtoKStream =
        stream.mapValues(this::getRideSearchDtoFromString);

    KStream<String, DriverRideDto> driverRideDtoKStream =
        rideSearchDtoKStream.mapValues(this::convertRideDtoToDriverRideDto);

    driverRideDtoKStream
        .split()
        .branch(
            (string, driverRideDto) -> driverRideDto.getId() != null,
            Branched.withConsumer(
                driverStream ->
                    driverStream.to(
                        availableDriverTopic,
                        Produced.with(Serdes.String(), driverRideDtoSerde()))))
        .branch(
            (string, driverRideDto) -> driverRideDto.getId() == null,
            Branched.withConsumer(
                driverStream ->
                    driverStream.to(
                        notFoundDriverTopic, Produced.with(Serdes.String(), driverRideDtoSerde()))))
        .noDefaultBranch();
  }

  private Serde<DriverRideDto> driverRideDtoSerde() {
    return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(DriverRideDto.class));
  }

  private DriverRideDto convertRideDtoToDriverRideDto(RideSearchDto rideSearchDto) {
    Optional<Driver> randomAvailableDriver = driverRepository.findRandomAvailable();
    if (randomAvailableDriver.isPresent()) {
      Driver driver = randomAvailableDriver.get();
      driver.setAvailable(false);
      DriverDto driverDto = driverMapper.toDto(driver);
      DriverRideDto driverRideDto = driverRideDtoMapper.toDriverRideDto(driverDto);
      driverRideDto.setRideId(rideSearchDto.getRideId());
      return driverRideDto;
    }
    return DriverRideDto.builder().rideId(rideSearchDto.getRideId()).build();
  }

  private RideSearchDto getRideSearchDtoFromString(String rideSearchDtoString) {
    try {
      return objectMapper.readValue(rideSearchDtoString, RideSearchDto.class);
    } catch (JsonProcessingException e) {
      throw new RideSearchDtoMappingException(
          "Exception occurred while RideSearchDto converting to Json");
    }
  }

  @Override
  @Transactional
  public void updateAvailabilityToTrueAfterFinishedRide(long driverId) {
    Driver driver = getDriver(driverId);
    driver.setAvailable(true);
    driverMapper.toDto(driver);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DriverDto> getDriversByIds(List<Long> listId) {
    List<Driver> byIdIn = driverRepository.findByIdIn(listId, null);
    return byIdIn.stream().map(driverMapper::toDto).collect(Collectors.toList());
  }
}
