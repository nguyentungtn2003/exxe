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

        public SecurityConfig(CustomOAuth2SuccessHandler customOAuth2SuccessHandler) {
                this.customOAuth2SuccessHandler = customOAuth2SuccessHandler;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
                StrictHttpFirewall firewall = new StrictHttpFirewall();
                firewall.setAllowUrlEncodedDoubleSlash(true);
                firewall.setAllowBackSlash(true);
                firewall.setAllowSemicolon(true);
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
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers(
                                                                "/login", "/register", "/forgot-password",
                                                                "/change-password", "/change-password/**",
                                                                "/product-details/*/add-review",
                                                                "/cart/remove", "/cart/update", "/update",
                                                                "/post-comment", "/dbcategory/*",
                                                                "/api/chat", "/", "/login/oauth2/**",
                                                                "/verify/**",
                                                                "/api/chat/**"))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/home", "/assets/**", "/login", "/register",
                                                                "/forgot-password", "/change-password",
                                                                "/change-password/**",
                                                                "/api/chat", "/verify/**", "/article/**")
                                                .permitAll()

                                                .requestMatchers(HttpMethod.GET, "/product-details/**").permitAll()

                                                // Yêu cầu đăng nhập cho checkout (bao gồm GET/POST)
                                                .requestMatchers("/checkout/**").authenticated()

                                                // Các POST cần role USER
                                                .requestMatchers(HttpMethod.POST, "/product-details/*/add-review")
                                                .hasRole("USER")
                                                .requestMatchers(HttpMethod.POST, "/post-comment").hasRole("USER")
                                                .requestMatchers(HttpMethod.POST, "/post-reply").hasRole("USER")
                                                .requestMatchers(HttpMethod.POST, "/cart/update").hasRole("USER")
                                                .requestMatchers(HttpMethod.POST, "/api/chat").hasRole("USER")

                                                // Admin quản lý bài viết
                                                .requestMatchers(HttpMethod.POST, "/articles/add").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/articles/edit").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/articles/delete").hasRole("ADMIN")

                                                // Các request khác phải login
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .usernameParameter("email")
                                                // Đây là key quan trọng để tự động redirect về trang truy cập trước
                                                // login
                                                .defaultSuccessUrl("/", false)
                                                .successHandler(successHandler)
                                                .failureHandler(failureHandler)
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .successHandler(customOAuth2SuccessHandler))
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll());

                return http.build();
        }

}
