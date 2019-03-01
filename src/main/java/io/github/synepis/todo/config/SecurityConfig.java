package io.github.synepis.todo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.synepis.todo.exception.ApiError;
import io.github.synepis.todo.exception.GlobalExceptionHandler;
import io.github.synepis.todo.security.AuthProvider;
import io.github.synepis.todo.security.AuthTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.time.Instant;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private AuthProvider authProvider;

    private GlobalExceptionHandler globalExceptionHandler;

    private ObjectMapper objectMapper;

    public SecurityConfig(AuthProvider authProvider, GlobalExceptionHandler globalExceptionHandler, ObjectMapper objectMapper) {
        this.authProvider = authProvider;
        this.globalExceptionHandler = globalExceptionHandler;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers(HttpMethod.POST, "/users").permitAll()
                .antMatchers(HttpMethod.POST, "/logins").permitAll()
                .anyRequest().fullyAuthenticated();
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler())
                .authenticationEntryPoint(unauthorizedEntryPoint());
        http
                .addFilterBefore(new AuthTokenFilter(), BasicAuthenticationFilter.class);
    }


    @Override
    protected void configure(AuthenticationManagerBuilder authBuilder) {
        authBuilder.authenticationProvider(authProvider);
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            ApiError.builder()
                    .status(HttpStatus.FORBIDDEN.name())
                    .userMessages(List.of("Access is denied"))
                    .debugMessages(List.of(accessDeniedException.getMessage()))
                    .createdOn(Instant.now())
                    .build();
        };
    }

    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            var apiError = ApiError.builder()
                    .status(HttpStatus.UNAUTHORIZED.name())
                    .userMessages(List.of("Authentication is required"))
                    .debugMessages(List.of(authException.getMessage()))
                    .createdOn(Instant.now())
                    .build();
            var errorJson = objectMapper.writeValueAsString(apiError);

            response.setContentType("application/json");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getOutputStream().println(errorJson);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public SecurityService securityService() {
//        return new SecurityService();
//    }
}