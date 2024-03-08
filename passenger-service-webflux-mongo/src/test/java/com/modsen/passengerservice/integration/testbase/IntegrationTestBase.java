package com.modsen.passengerservice.integration.testbase;

import com.modsen.passengerservice.integration.annotation.IntegrationTests;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

@IntegrationTests
public abstract class IntegrationTestBase {

  public static final GenericContainer<?> mongoContainer =
      new GenericContainer<>("mongo:6.0.3").withExposedPorts(27017);

  @BeforeAll
  static void runContainer() {
    mongoContainer.start();
  }

  @DynamicPropertySource
  static void mongoProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.host", mongoContainer::getHost);
    registry.add("spring.data.mongodb.port", mongoContainer::getFirstMappedPort);
    registry.add("spring.data.mongodb.database", () -> "app_db");
  }
}
