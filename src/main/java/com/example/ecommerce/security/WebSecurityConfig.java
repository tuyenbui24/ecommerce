package com.example.ecommerce.security;

import com.example.ecommerce.staff.StaffDtsService;
import com.example.ecommerce.user.UserDtsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final UserDtsService userDtsService;
    private final StaffDtsService staffDtsService;

    public WebSecurityConfig(UserDtsService userDtsService, StaffDtsService staffDtsService) {
        this.userDtsService = userDtsService;
        this.staffDtsService = staffDtsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider customAuthenticationProvider() {
        return new CustomAuthenticationProvider(userDtsService, staffDtsService, passwordEncoder());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(customAuthenticationProvider())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/register", "/process-register",
                                "/login", "/css/**", "/js/**", "/product-image/**",
                                "/img/**", "/bootstrap/**", "/fontawesome/**").permitAll()
                        .requestMatchers("/staffs/**", "/users/**", "/products/**").hasRole("ADMIN")
                        .requestMatchers("/cart/**", "/checkout/**", "/orders/**").authenticated()
                        .anyRequest().authenticated())

                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())

                .logout(lo -> lo
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }
}