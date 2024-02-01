package com.modsen.rideservice.integration.helper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "security.oauth2.keycloak")
@ConfigurationPropertiesScan
@Getter
@Setter
@Component
public class AccessTokenProperties {

  private String accessTokenPath;
  private String realmName;
  private String realmSecret;
  private String adminUsername;
  private String passengerUsername;
  private String driverUsername;
  private String passwordForAllRoles;
}
