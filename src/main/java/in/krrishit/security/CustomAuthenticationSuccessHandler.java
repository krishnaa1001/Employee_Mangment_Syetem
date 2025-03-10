package in.krrishit.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Random;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String userEmail = authentication.getName();
        String otp = String.valueOf(new Random().nextInt(900000) + 100000); // six-digit OTP

        // Store OTP, email, and role in session for OTP verification
        HttpSession session = request.getSession();
        session.setAttribute("OTP", otp);
        session.setAttribute("otpGenerationTime", System.currentTimeMillis());
        session.setAttribute("otpAttempts", 0);
        session.setAttribute("userEmail", userEmail);
        // Store the first authority as userRole (adjust if needed for multiple roles)
        String userRole = authentication.getAuthorities().iterator().next().getAuthority();
        session.setAttribute("userRole", userRole);
        session.removeAttribute("otpBlockTime");

        // Build the verification link (adjust host/port as needed)
        String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
        String verifyLink = baseUrl + "/verify-otp";

        // Send OTP email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userEmail);
        message.setSubject("Your OTP Verification Link");
        message.setText("Dear User,\n\n"
                + "Please click the link below to verify your OTP and complete your login:\n"
                + verifyLink + "\n\n"
                + "Your OTP is: " + otp + "\n\n"
                + "Regards,\nKrrishIt Team");
        mailSender.send(message);

        // Determine target URL based on user role:
        if ("ROLE_EMPLOYEE".equals(userRole)) {
            session.setAttribute("targetUrl", "/employee_dashboard");
        } else if ("ROLE_ADMIN".equals(userRole)) {
            session.setAttribute("targetUrl", "/dashboard");
        } else {
            session.setAttribute("targetUrl", "/verify-otp");
        }

        // Redirect to OTP verification page
        response.sendRedirect("/verify-otp");
    }
}