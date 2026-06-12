package com.grupo8.turnos_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.grupo8.turnos_app.auth.JwtAuthFilter;
import com.grupo8.turnos_app.modules.users.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/businesses/slug/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/business-types").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/businesses/{id}/appointments/public").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/businesses").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/businesses/{id}").hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/v1/admin/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/v1/business-types").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/v1/business-types/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/business-types/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/v1/businesses").hasRole("OWNER")
            .requestMatchers(HttpMethod.GET, "/api/v1/businesses/{id}").hasRole("OWNER")
            .requestMatchers(HttpMethod.GET, "/api/v1/businesses/mine").hasRole("OWNER")
            .requestMatchers(HttpMethod.PUT, "/api/v1/businesses/{id}").hasRole("OWNER")
            .requestMatchers(HttpMethod.POST, "/api/v1/businesses/{id}/types/**").hasRole("OWNER")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/businesses/{id}/types/**").hasRole("OWNER")
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/swagger-ui.html").permitAll()
            .requestMatchers("/v3/api-docs/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/businesses/{businessId}/employees").hasRole("OWNER")
            .requestMatchers(HttpMethod.POST, "/api/v1/businesses/{businessId}/employees").hasRole("OWNER")
            .requestMatchers(HttpMethod.GET, "/api/v1/employees/{id}").hasRole("OWNER")
            .requestMatchers(HttpMethod.PUT, "/api/v1/employees/{id}").hasRole("OWNER")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/employees/{id}").hasRole("OWNER")
            .requestMatchers(HttpMethod.GET, "/api/v1/businesses/*/appointments/public").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v1/appointments/*/book").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v1/appointments/*/pay-deposit").permitAll()
            .requestMatchers(HttpMethod.PUT, "/api/v1/appointments/*/cancel").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/v1/appointments/*/suspend").hasRole("OWNER")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/appointments/*").hasRole("OWNER")
            .requestMatchers(HttpMethod.GET, "/api/v1/businesses/*/appointments/today").hasRole("OWNER")
            .requestMatchers(HttpMethod.GET, "/api/v1/businesses/*/appointments").hasRole("OWNER")
            .requestMatchers(HttpMethod.GET, "/api/v1/users/me/appointments").authenticated()
            .anyRequest().authenticated()
        )
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
}