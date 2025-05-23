package com.example.exe2update.config;

import com.example.exe2update.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserRepository userRepository; // hoặc service check email

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String email = request.getParameter("email");
        request.getSession().setAttribute("lastEmail", email);

        if (userRepository.findByEmailNormalized(email).isEmpty()) {
            request.getSession().setAttribute("message", "Email không tồn tại.");
            request.getSession().setAttribute("showForgotPassword", false);
        } else {
            request.getSession().setAttribute("message", "Sai mật khẩu.");
            request.getSession().setAttribute("showForgotPassword", true);
        }
        System.out.println("Login failed: " + exception.getMessage());

        super.setDefaultFailureUrl("/login?error");
        super.onAuthenticationFailure(request, response, exception);
    }
}
