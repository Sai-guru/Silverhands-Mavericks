package io.bootify.silverhands.config.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final SecurityContextEnricherFilter securityContextEnricherFilter;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    // only providers can create/edit services and products
    private static final List<String> PROVIDER_WRITE_PATHS = List.of(
            "/api/services/**", "/api/products/**");

    // only providers can browse customer discovery
    private static final List<String> PROVIDER_READ_PATHS = List.of("/api/customers/**");

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/", "/error", "/swagger-ui/**", "/v3/api-docs/**",
                                "/actuator/health", "/oauth2/**", "/login/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, PROVIDER_WRITE_PATHS.toArray(String[]::new))
                        .hasRole("PROVIDER")
                        .requestMatchers(HttpMethod.PUT, PROVIDER_WRITE_PATHS.toArray(String[]::new))
                        .hasRole("PROVIDER")
                        .requestMatchers(HttpMethod.DELETE, PROVIDER_WRITE_PATHS.toArray(String[]::new))
                        .hasRole("PROVIDER")
                        .requestMatchers(PROVIDER_READ_PATHS.toArray(String[]::new))
                        .hasRole("PROVIDER")
                        // everything else (search, chat, conversations, AI) is
                        // available to any authenticated user
                        .anyRequest().authenticated())
                .addFilterBefore(securityContextEnricherFilter, AuthorizationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(
                                HttpStatus.UNAUTHORIZED),
                                request -> request.getRequestURI().startsWith("/api/")))
                .oauth2Login(oauth2 -> oauth2.successHandler(oauth2LoginSuccessHandler))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .forEach(configuration::addAllowedOrigin);
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
