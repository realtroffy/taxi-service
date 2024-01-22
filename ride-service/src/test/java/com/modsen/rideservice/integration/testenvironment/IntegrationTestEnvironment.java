package com.modsen.rideservice.integration.testenvironment;

import com.modsen.rideservice.integration.annotation.IntegrationTests;
import io.restassured.RestAssured;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@IntegrationTests
@Sql(
    scripts = {"classpath:db/delete-data.sql", "classpath:db/init-data.sql"},
    executionPhase = BEFORE_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class IntegrationTestEnvironment {

  public static final PostgreSQLContainer<?> container =
      new PostgreSQLContainer<>("postgres:13.1-alpine").withUrlParam("stringtype", "unspecified");
  public static final KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.3"));

  public static MockWebServer mockWebServer;

  @BeforeAll
  protected static void beforeAll() {
    container.start();
    kafka.start();
    mockWebServer = new MockWebServer();
  }

  @AfterAll
  protected static void afterAll() throws IOException {
    mockWebServer.close();
  }

  @LocalServerPort
  private int port;

  protected void setUp() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = this.port;
  }

  @DynamicPropertySource
  static void setDynamicProperty(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url ", container::getJdbcUrl);
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    registry.add("spring.kafka.bootstrap-address", kafka::getBootstrapServers);
  }

  @TestConfiguration
  public static class TestConfig {

    @Bean
    @Profile("test")
    public WebClient passengerWebClient() {
      return WebClient.builder().baseUrl(mockWebServer.url("/").url().toString()).build();
    }

    @Bean
    @Profile("test")
    public WebClient driverWebClient() {
      return WebClient.builder().baseUrl(mockWebServer.url("/").url().toString()).build();
    }

    @Bean
    @Profile("test")
    public Map<String, Object> consumerConfig() {
      Map<String, Object> props = new HashMap<>();
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
      props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-id");
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
      props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
      return props;
    }

    @Bean
    public Consumer<String, Object> testConsumerOrderNewRide() {
      return new KafkaConsumer<>(consumerConfig());
    }

    @Bean
    @Profile("test")
    public Map<String, Object> producerConfig() {
      return Map.of(
          ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
          ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
          ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    }
  }
}
