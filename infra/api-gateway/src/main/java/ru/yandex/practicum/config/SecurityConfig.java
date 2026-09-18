package ru.yandex.practicum.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import java.util.ArrayList;
import java.util.List;


@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .pathMatchers(HttpMethod.GET,"/api/categories/**").permitAll()
                        .pathMatchers(HttpMethod.GET,"/api/inventory/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/orders/**").hasRole("USER")
                        .pathMatchers(HttpMethod.GET,"/api/orders/by-email").hasRole("USER")
                        .pathMatchers(HttpMethod.GET,"/api/orders/*").hasRole("USER")
                        .pathMatchers(HttpMethod.GET,"/api/orders").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST,"/api/products/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH,"/api/products/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST,"/api/categories").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST,"/api/inventory/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT,"/api/inventory/**").hasRole("ADMIN")
                        .anyExchange().denyAll()
                )
                .httpBasic(Customizer.withDefaults())
                .securityContextRepository(
                        NoOpServerSecurityContextRepository.getInstance()
                )
                .cors(Customizer.withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    @Bean
    MapReactiveUserDetailsService userDetailsService(SecurityProperties properties,PasswordEncoder passwordEncoder) {
        List<UserDetails> users = new ArrayList<>();
        for(SecurityProperties.UserConfig userConfig: properties.getUsers()) {
            UserDetails user = User.builder()
                    .username(userConfig.getUsername())
                    .password(passwordEncoder.encode(userConfig.getPassword()))
                    .roles(userConfig.getRoles().toArray(String[]::new))
                    .build();
            users.add(user);
        }

        return new MapReactiveUserDetailsService(users);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
