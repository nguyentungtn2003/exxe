package com.example.exe2update.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

        @Autowired
        private CustomAuthenticationSuccessHandler successHandler;

        @Autowired
        private CustomAuthenticationFailureHandler failureHandler;

        @Autowired
        private CustomUserDetailsService userDetailsService;

        SecurityConfig(CustomOAuth2SuccessHandler customOAuth2SuccessHandler) {
                this.customOAuth2SuccessHandler = customOAuth2SuccessHandler;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
                StrictHttpFirewall firewall = new StrictHttpFirewall();
                firewall.setAllowUrlEncodedDoubleSlash(true); // Cho phép //
                firewall.setAllowBackSlash(true); // Tuỳ chọn: nếu bạn dùng \ trong URL
                firewall.setAllowSemicolon(true); // Tuỳ chọn
                return firewall;
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder());
                provider.setHideUserNotFoundExceptions(false);
                return provider;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // .csrf(csrf -> csrf
                                // .ignoringRequestMatchers(
                                // "/login", "/register", "/forgot-password", "/change-password",
                                // "/change-password/**", "/product-details/*/add-review"
                                // , "/cart/remove", "/cart/update","/checkout/pay,"/api/chat""
                                // )
                                // )
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers(
                                                                "/login", "/register", "/forgot-password",
                                                                "/change-password", "/change-password/**",
                                                                "/product-details/*/add-review",
                                                                "/cart/remove", "/cart/update", "/update",
                                                                "/post-comment", "/dbcategory/*",
                                                                "api/chat",
                                                                "/",
                                                                "/login/oauth2/**",
                                                                "/verify/**"))

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/assets/**", "/login", "/register",
                                                                "/forgot-password", "/change-password",
                                                                "/change-password/**",
                                                                "/api/chat",
                                                                "/",
                                                                "verify/**")
                                                .permitAll()

                                                // Allow public access to product details and reviews
                                                .requestMatchers(HttpMethod.GET, "/product-details/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/product-details/*/add-review")
                                                .hasRole("USER")

                                                // ArticlesDetailsController
                                                .requestMatchers(HttpMethod.POST, "/post-comment").hasRole("USER")
                                                .requestMatchers(HttpMethod.POST, "/post-reply").hasRole("USER")
                                                .requestMatchers(HttpMethod.POST, "/articles/add").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/articles/edit").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/post-comment").hasRole("USER")
                                                .requestMatchers(HttpMethod.POST, "/articles/delete").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/cart/update").hasRole("USER")
                                                .requestMatchers(HttpMethod.POST, "/api/chat").hasRole("USER")
                                                .requestMatchers(HttpMethod.POST, "/checkout/pay").hasRole("USER")

                                                .requestMatchers("/dbcategory/add").permitAll()

                                                // Require authentication for other requests
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .usernameParameter("email")
                                                .successHandler(successHandler)
                                                .failureHandler(failureHandler)
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login") // trang login tùy chỉnh
                                                .successHandler(customOAuth2SuccessHandler))
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll());

                return http.build();
        }

}
