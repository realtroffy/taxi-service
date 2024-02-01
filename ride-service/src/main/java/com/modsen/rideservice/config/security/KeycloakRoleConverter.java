package com.modsen.rideservice.config.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  public static final String PREFIX_REALM_ROLE = "ROLE_realm_";
  public static final String PREFIX_RESOURCE_ROLE = "ROLE_";
  private static final String CLAIM_REALM_ACCESS = "realm_access";
  private static final String CLAIM_RESOURCE_ACCESS = "resource_access";
  private static final String CLAIM_ROLES = "roles";

  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    Map<String, Collection<String>> realmAccess = jwt.getClaim(CLAIM_REALM_ACCESS);
    if (realmAccess != null && !realmAccess.isEmpty()) {
      Collection<String> roles = realmAccess.get(CLAIM_ROLES);
      if (roles != null && !roles.isEmpty()) {
        Collection<GrantedAuthority> realmRoles =
            roles.stream()
                .map(role -> new SimpleGrantedAuthority(PREFIX_REALM_ROLE + role))
                .collect(Collectors.toList());
        grantedAuthorities.addAll(realmRoles);
      }
    }
    Map<String, Map<String, Collection<String>>> resourceAccess =
        jwt.getClaim(CLAIM_RESOURCE_ACCESS);
    if (resourceAccess != null && !resourceAccess.isEmpty()) {
      resourceAccess.forEach(
          (resource, resourceClaims) ->
              resourceClaims
                  .get(CLAIM_ROLES)
                  .forEach(
                      role ->
                          grantedAuthorities.add(
                              new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + role))));
    }

    return grantedAuthorities;
  }
}
