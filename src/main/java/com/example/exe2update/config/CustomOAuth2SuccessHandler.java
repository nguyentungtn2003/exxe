package com.example.exe2update.config;

import com.example.exe2update.entity.Role;
import com.example.exe2update.entity.User;
import com.example.exe2update.repository.RoleRepository;
import com.example.exe2update.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomOAuth2SuccessHandler(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            response.sendRedirect("/login");
            return;
        }

        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
        String email = (String) attributes.get("email");

        if (email != null && !email.isEmpty()) {
            Optional<User> userOptional = userRepository.findByEmailNormalized(email);

            if (userOptional.isEmpty()) {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setFullName((String) attributes.getOrDefault("name", email));
                newUser.setUsername(email);
                newUser.setPasswordHash("N/A"); // OAuth user
                newUser.setCreatedAt(LocalDateTime.now());
                newUser.setStatus(true);

                Role role = roleRepository.findById(2) // ID 2 = USER
                        .orElseThrow(() -> new RuntimeException("Role with id=2 not found"));
                newUser.setRole(role);

                userRepository.save(newUser);
                System.out.println("New user saved: " + email);
            } else {
                System.out.println("User already exists: " + email);
            }
        } else {
            System.out.println("Email not found in Google attributes!");
        }

        response.sendRedirect("/home");
    }
}
