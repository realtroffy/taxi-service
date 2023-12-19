package com.modsen.driverservice.integration.testbase;

import com.modsen.driverservice.integration.annotation.IntegrationTests;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@IntegrationTests
@Sql(
    scripts = {"classpath:db/delete-data.sql", "classpath:db/init-data.sql"},
    executionPhase = BEFORE_TEST_METHOD)
public abstract class IntegrationTestBase {

  private static final PostgreSQLContainer<?> container =
      new PostgreSQLContainer<>("postgres:13.1-alpine");

  @BeforeAll
  protected static void runContainer() {
    container.start();
  }

  @LocalServerPort private int port;

  protected void setUp() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = this.port;
  }

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url ", container::getJdbcUrl);
  }
}
