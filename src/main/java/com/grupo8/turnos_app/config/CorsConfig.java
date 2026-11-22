package com.grupo8.turnos_app.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// allows the frontend (different origin) to call the api from the browser.
// used by SecurityConfig through http.cors()
@Configuration
public class CorsConfig {

  // comma separated list, e.g. http://localhost:4200,https://turnos.example.com
  @Value("${app.cors.allowed-origins:http://localhost:4200}")
  private List<String> allowedOrigins;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(allowedOrigins);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    // jwt goes in the Authorization header, no cookies needed
    config.setAllowCredentials(false);
    // browser caches the preflight response for 1 hour
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
