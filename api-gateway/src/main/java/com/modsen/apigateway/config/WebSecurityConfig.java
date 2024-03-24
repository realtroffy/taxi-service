package com.modsen.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

@Configuration
public class WebSecurityConfig {

  public static final String LOGOUT_PAGE_PATH = "{baseUrl}/logout.html";

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(
      ServerHttpSecurity http, ServerLogoutSuccessHandler handler) {
    http.csrf()
        .disable()
        .authorizeExchange()
        .pathMatchers("/logout.html")
        .permitAll()
        .anyExchange()
        .authenticated()
        .and()
        .oauth2Login()
        .and()
        .logout()
        .logoutSuccessHandler(handler);

    return http.build();
  }

  @Bean
  public ServerLogoutSuccessHandler keycloakLogoutSuccessHandler(
      ReactiveClientRegistrationRepository repository) {

    OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutSuccessHandler =
        new OidcClientInitiatedServerLogoutSuccessHandler(repository);

    oidcLogoutSuccessHandler.setPostLogoutRedirectUri(LOGOUT_PAGE_PATH);

    return oidcLogoutSuccessHandler;
  }
}
