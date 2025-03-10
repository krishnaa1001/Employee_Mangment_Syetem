package in.krrishit.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.krrishit.services.BlockedUserService;

@Controller
public class OtpController {

    @Autowired
    private BlockedUserService blockedUserService;

    @GetMapping("/verify-otp")
    public String showOtpVerificationPage(Model model) {
        model.addAttribute("message", "Check your email to verify your OTP and access your dashboard.");
        return "verifyotp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") String otp, HttpSession session, Model model) {
        long currentTime = System.currentTimeMillis();

        // Get email from session (set in login success)
        String email = (String) session.getAttribute("userEmail");

        // Check if user is already blocked in this session
        Long blockTime = (Long) session.getAttribute("otpBlockTime");
        if (blockTime != null && currentTime < blockTime + 5 * 60 * 1000) {
            model.addAttribute("error", "Too many incorrect attempts. Your account is blocked. Please try again later.");
            return "verifyotp";
        } else if (blockTime != null) {
            // Unblock session if block period is over
            session.removeAttribute("otpBlockTime");
            session.setAttribute("otpAttempts", 0);
        }

        // Check OTP expiration (valid for 5 minutes)
        Long otpGenerationTime = (Long) session.getAttribute("otpGenerationTime");
        if (otpGenerationTime == null || currentTime > otpGenerationTime + 5 * 60 * 1000) {
            model.addAttribute("error", "OTP has expired. Please log in again to receive a new OTP.");
            return "verifyotp";
        }

        String sessionOtp = (String) session.getAttribute("OTP");
        Integer attempts = (Integer) session.getAttribute("otpAttempts");
        if (sessionOtp != null && sessionOtp.equals(otp)) {
            // Successful verification: clear OTP related session attributes
            session.removeAttribute("OTP");
            session.removeAttribute("otpGenerationTime");
            session.removeAttribute("otpAttempts");
            session.removeAttribute("otpBlockTime");

            // Retrieve user role from session (set during login)
            String role = (String) session.getAttribute("userRole");

            // Redirect based on role with dashboard pages inside respective controllers/mappings
            if ("ROLE_ADMIN".equals(role)) {
                return "redirect:/admin/dashboard";
            } else if ("ROLE_EMPLOYEE".equals(role)) {
                // Redirect to the employee dashboard mapping (e.g., /employee_dashboard)
                return "redirect:/employee_dashboard";
            } else if ("ROLE_CLIENT".equals(role)) {
                // Redirect to the client dashboard mapping (e.g., /client_dashboard)
                return "redirect:/client_dashboard";
            } else {
                // Default redirection if role not found or unrecognized
                return "redirect:/dashboard";
            }
        } else {
            // Increment incorrect attempts
            attempts = (attempts == null) ? 1 : attempts + 1;
            session.setAttribute("otpAttempts", attempts);
            if (attempts >= 5) {
                // Set session block for 5 minutes
                session.setAttribute("otpBlockTime", currentTime);
                model.addAttribute("error", "You have entered the incorrect OTP 5 times. Your account is blocked for 5 minutes.");
            } else {
                model.addAttribute("error", "Invalid OTP. Please try again.");
            }
            return "verifyotp";
        }
    }
}
