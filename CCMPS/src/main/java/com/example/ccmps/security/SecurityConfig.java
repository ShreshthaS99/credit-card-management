package com.example.ccmps.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication
        .AuthenticationManager;

import org.springframework.security.authentication.dao
        .DaoAuthenticationProvider;

import org.springframework.security.config.annotation
        .authentication.configuration
        .AuthenticationConfiguration;

import org.springframework.security.config.annotation.method
        .configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web
        .builders.HttpSecurity;

import org.springframework.security.config.http
        .SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt
        .BCryptPasswordEncoder;

import org.springframework.security.crypto.password
        .PasswordEncoder;

import org.springframework.security.web
        .SecurityFilterChain;

import org.springframework.security.web.authentication
        .UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors
        .CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;

    private final CustomUserDetailsService
            userDetailsService;

    private final CorsConfigurationSource
            corsConfigurationSource;

    private final RestAuthenticationEntryPoint
            authenticationEntryPoint;

    private final RestAccessDeniedHandler
            accessDeniedHandler;

    public SecurityConfig(

            JwtAuthenticationFilter
                    jwtAuthenticationFilter,

            CustomUserDetailsService
                    userDetailsService,

            CorsConfigurationSource
                    corsConfigurationSource,

            RestAuthenticationEntryPoint
                    authenticationEntryPoint,

            RestAccessDeniedHandler
                    accessDeniedHandler) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;

        this.userDetailsService =
                userDetailsService;

        this.corsConfigurationSource =
                corsConfigurationSource;

        this.authenticationEntryPoint =
                authenticationEntryPoint;

        this.accessDeniedHandler =
                accessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider
    authenticationProvider(

            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder
        );

        return provider;
    }

    @Bean
    public AuthenticationManager
    authenticationManager(

            AuthenticationConfiguration
                    authenticationConfiguration)

            throws Exception {

        return authenticationConfiguration
                .getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain
    securityFilterChain(

            HttpSecurity http,

            DaoAuthenticationProvider
                    authenticationProvider)

            throws Exception {

        http
                .csrf(csrf ->
                        csrf.disable()
                )

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource
                        )
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authenticationProvider(
                        authenticationProvider
                )

                .exceptionHandling(exceptions ->
                        exceptions
                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler
                                )
                )

                .authorizeHttpRequests(authorization ->
                        authorization
                                .requestMatchers(
                                        "/api/auth/login",
                                        "/api/auth/register",
                                        "/error"
                                )
                                .permitAll()


                                .requestMatchers(
                                        "/api/admin/**"
                                )
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        "/api/**"
                                )
                                .authenticated()

                                .anyRequest()
                                .permitAll()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}