package com.modsen.passengerservice.config.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  public static final String PREFIX_REALM_ROLE = "ROLE_realm_";
  public static final String PREFIX_RESOURCE_ROLE = "ROLE_";
  public static final String CLAIM_REALM_ACCESS = "realm_access";
  public static final String CLAIM_RESOURCE_ACCESS = "resource_access";
  public static final String RESOURCE_NAME = "taxi-service";
  public static final String CLAIM_ROLES = "roles";

  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    Map<String, Map<String, Collection<String>>> resourcesAccessClaim =
            jwt.getClaim(CLAIM_RESOURCE_ACCESS);

    Map<String, Collection<String>> realmAccessClaim = jwt.getClaim(CLAIM_REALM_ACCESS);
    Map<String, Collection<String>> taxiServiceResourceAccessClaim =
            getResourceClaimFromJwt(resourcesAccessClaim);

    Collection<String> realmRoles = getRolesFromJwtClaim(realmAccessClaim);
    Collection<String> resourceRoles = getRolesFromJwtClaim(taxiServiceResourceAccessClaim);

    Collection<GrantedAuthority> realmRolesGrantedAuthorities =
            convertClaimsRolesToGrantedAuthorityCollection(realmRoles, PREFIX_REALM_ROLE);
    Collection<GrantedAuthority> resourceRolesGrantedAuthorities =
            convertClaimsRolesToGrantedAuthorityCollection(resourceRoles, PREFIX_RESOURCE_ROLE);

    return Stream.of(realmRolesGrantedAuthorities, resourceRolesGrantedAuthorities)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
  }

  private Collection<String> getRolesFromJwtClaim(Map<String, Collection<String>> claims) {
    if (claims != null && !claims.isEmpty()) {
      return claims.get(CLAIM_ROLES);
    }
    return Collections.emptyList();
  }

  private Collection<GrantedAuthority> convertClaimsRolesToGrantedAuthorityCollection(
          Collection<String> roles, String rolePrefix) {
    if (roles != null && !roles.isEmpty()) {
      return roles.stream()
              .map(role -> new SimpleGrantedAuthority(rolePrefix + role))
              .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private Map<String, Collection<String>> getResourceClaimFromJwt(
          Map<String, Map<String, Collection<String>>> resourceClaims) {
    if (resourceClaims != null && !resourceClaims.isEmpty()) {
      return resourceClaims.get(RESOURCE_NAME);
    }
    return Collections.emptyMap();
  }
}