package com.whatsappclone.configs.security;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakJwtAuthenticationConverter.class);

    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String DEFAULT_RESOURCE_ID = "account";

    private final JwtGrantedAuthoritiesConverter defaultAuthoritiesConverter;
    private final String resourceId;

    public KeycloakJwtAuthenticationConverter() {
        this(DEFAULT_RESOURCE_ID);
    }

    public KeycloakJwtAuthenticationConverter(String resourceId) {
        this.defaultAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        this.resourceId = StringUtils.hasText(resourceId) ? resourceId : DEFAULT_RESOURCE_ID;
    }

    @Override
    public JwtAuthenticationToken convert(@NotNull Jwt jwt) {

        try {
            Collection<GrantedAuthority> authorities = extractAllAuthorities(jwt);
            return new JwtAuthenticationToken(jwt, authorities);
        } catch (Exception ex) {
            logger.error("Error converting JWT to Authentication token", ex);
            return new JwtAuthenticationToken(jwt, getDefaultAuthorities(jwt));
        }
    }

    private Collection<GrantedAuthority> extractAllAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> defaultAuthorities = getDefaultAuthorities(jwt);
        Collection<GrantedAuthority> resourceRoles = extractResourceRoles(jwt);

        return Stream.concat(
                defaultAuthorities.stream(),
                resourceRoles.stream()
        ).collect(Collectors.toSet());
    }

    private Collection<GrantedAuthority> getDefaultAuthorities(Jwt jwt) {

        try {
            return defaultAuthoritiesConverter.convert(jwt);
        } catch (Exception ex) {
            logger.warn("Error extracting default authorities from JWT", ex);
            return Collections.emptySet();
        }
    }

    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {

        try {

            if (!jwt.hasClaim(RESOURCE_ACCESS_CLAIM)) {
                logger.debug("JWT does not contain '{}' claim", RESOURCE_ACCESS_CLAIM);
                return Collections.emptySet();
            }
            Object resourceAccessClaim = jwt.getClaim(RESOURCE_ACCESS_CLAIM);

            if (!(resourceAccessClaim instanceof Map)) {
                logger.warn("'{}' claim is not a Map: {}", RESOURCE_ACCESS_CLAIM,
                        resourceAccessClaim != null ? resourceAccessClaim.getClass() : "null");
                return Collections.emptySet();
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> resourceAccess = (Map<String, Object>) resourceAccessClaim;

            if (!resourceAccess.containsKey(resourceId)) {
                logger.debug("Resource '{}' not found in resource_access claim", resourceId);
                return Collections.emptySet();
            }
            Object resourceData = resourceAccess.get(resourceId);

            if (!(resourceData instanceof Map)) {
                logger.warn("Resource '{}' data is not a Map: {}", resourceId,
                        resourceData != null ? resourceData.getClass() : "null");
                return Collections.emptySet();
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> resource = (Map<String, Object>) resourceData;

            if (!resource.containsKey(ROLES_CLAIM)) {
                logger.debug("No '{}' claim found in resource '{}'", ROLES_CLAIM, resourceId);
                return Collections.emptySet();
            }
            Object rolesData = resource.get(ROLES_CLAIM);

            if (!(rolesData instanceof Collection)) {
                logger.warn("Roles data is not a Collection: {}",
                        rolesData != null ? rolesData.getClass() : "null");
                return Collections.emptySet();
            }
            @SuppressWarnings("unchecked")
            Collection<String> roles = (Collection<String>) rolesData;

            return roles.stream()
                    .filter(Objects::nonNull)
                    .filter(StringUtils::hasText)
                    .map(this::normalizeRoleName)
                    .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                    .collect(Collectors.toSet());

        } catch (Exception ex) {
            logger.error("Unexpected error extracting resource roles from JWT", ex);
            return Collections.emptySet();
        }
    }

    private String normalizeRoleName(String role) {

        if (!StringUtils.hasText(role)) {
            return "";
        }
        return role.replace("-", "_").toUpperCase();
    }
}
