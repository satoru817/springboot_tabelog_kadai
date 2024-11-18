package com.example.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables method-level security annotations like @PreAuthorize
public class WebSecurityConfig {
    private final CustomAuthenticationFailureHandler failureHandler;
    private final CustomAuthenticationSuccessHandler successHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Authorize requests based on the URL patterns
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/userRegister**","/userRegister/**","/signUp**","/css/**", "/images/**", "/js/**", "/storage/**", "/","/passwordChange","/test","/auth/login**","/forgot_password**","/forgot_password/**","/doChangePassword","/auth/blocked","/stripe/webhook")
                        .permitAll()  // Publicly accessible
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN") // Only users with "ADMIN" role can access
                        .requestMatchers("/paid/**")
                        .hasRole("PAID_USER")
                        .anyRequest()
                        .authenticated() // Any other request requires authentication
                )
                // Form login configuration
                .formLogin(login -> login
                        .loginPage("/auth/login")// Custom login page
                        .loginProcessingUrl("/auth/login") // Form action URL for login submission
                        .failureHandler(failureHandler)
                        .successHandler(successHandler)//Custom failure handler
                        .permitAll() // Allow everyone to see the login page
                )
                // Logout configuration
                .logout(logout -> logout
                        .logoutSuccessUrl("/auth/login") // Redirect after successful logout
                        .permitAll() // Allow logout for all authenticated users
                )
                .csrf().ignoringRequestMatchers("/stripe/webhook");



        return http.build(); // Build the security filter chain
    }

    // Password encoder bean for encoding passwords with BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

