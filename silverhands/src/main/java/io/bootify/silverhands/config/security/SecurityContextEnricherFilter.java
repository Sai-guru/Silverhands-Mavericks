package io.bootify.silverhands.config.security;

import io.bootify.silverhands.service.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


/**
 * Enriches the Spring Security Authentication with the user's database role as
 * a GrantedAuthority (ROLE_CUSTOMER / ROLE_SERVICE_PROVIDER), so that
 * hasRole(...) checks in SecurityConfig work with session-based OAuth2 login.
 */
@Component
public class SecurityContextEnricherFilter extends OncePerRequestFilter {

    private final UserService userService;

    public SecurityContextEnricherFilter(final UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
            final HttpServletResponse response, final FilterChain filterChain)
            throws ServletException, IOException {

        final Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            final var user = userService.ensureFromOAuth(oauthToken);
            final String dbRole = user.getRole();
            final String expectedAuthority = dbRole.startsWith("ROLE_") ? dbRole : "ROLE_" + dbRole;

            final boolean hasCurrentDbRole = oauthToken.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(authority -> authority.equals(expectedAuthority));

            if (!hasCurrentDbRole) {
                final List<GrantedAuthority> otherAuthorities = oauthToken.getAuthorities().stream()
                        .filter(a -> !a.getAuthority().equals("ROLE_CUSTOMER")
                                && !a.getAuthority().equals("ROLE_PROVIDER")
                                && !a.getAuthority().equals("ROLE_PENDING"))
                        .toList();

                final List<GrantedAuthority> enrichedAuthorities = new ArrayList<>(otherAuthorities);
                enrichedAuthorities.add(new SimpleGrantedAuthority(expectedAuthority));

                final var enrichedToken = new OAuth2AuthenticationToken(
                        oauthToken.getPrincipal(),
                        List.copyOf(enrichedAuthorities),
                        oauthToken.getAuthorizedClientRegistrationId());
                SecurityContextHolder.getContext().setAuthentication(enrichedToken);
            }
        }

        filterChain.doFilter(request, response);
    }

}
