package com.modsen.rideservice.integration.helper;

import io.restassured.http.ContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestConstructor;

import java.util.Base64;

import static com.modsen.rideservice.integration.testenvironment.IntegrationTestEnvironment.KEYCLOAK;
import static io.restassured.RestAssured.given;
import static org.springframework.test.context.TestConstructor.AutowireMode.ALL;

@Component
@TestConstructor(autowireMode = ALL)
@RequiredArgsConstructor
public class AccessTokenExtractor {

  private final AccessTokenProperties accessTokenProperties;

  public String getAdminAccessToken() {
    return getAccessTokenForRole(accessTokenProperties.getAdminUsername());
  }

  public String getPassengerAccessToken() {
    return getAccessTokenForRole(accessTokenProperties.getPassengerUsername());
  }

  public String getDriverAccessToken() {
    return getAccessTokenForRole(accessTokenProperties.getDriverUsername());
  }

  private String encode() {
    return new String(
        Base64.getEncoder()
            .encode(
                (accessTokenProperties.getRealmName()
                        + ":"
                        + accessTokenProperties.getRealmSecret())
                    .getBytes()));
  }

  private String getAccessTokenForRole(String username) {
    String authorization = encode();

    return given()
        .header("Authorization", "Basic " + authorization)
        .contentType(ContentType.URLENC)
        .formParam("grant_type", "password")
        .formParam("username", username)
        .formParam("password", accessTokenProperties.getPasswordForAllRoles())
        .post(KEYCLOAK.getAuthServerUrl() + accessTokenProperties.getAccessTokenPath())
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .response()
        .jsonPath()
        .getString("access_token");
  }
}
