package in.krrishit.services;

import in.krrishit.model.User;
import in.krrishit.repository.UserRepository;
import in.krrishit.services.BlockedUserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BlockedUserService blockedUserService;

    public CustomUserDetailsService(UserRepository userRepository, BlockedUserService blockedUserService) {
        this.userRepository = userRepository;
        this.blockedUserService = blockedUserService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String lowerEmail = email.toLowerCase();
        
        // Check if the user is blocked by custom logic
        if (blockedUserService.isBlocked(lowerEmail)) {
            long remainingMillis = blockedUserService.getRemainingBlockTime(lowerEmail);
            long minutes = remainingMillis / 60000;
            long seconds = (remainingMillis % 60000) / 1000;
            throw new UsernameNotFoundException("Your account is blocked. Please try again after " 
                    + minutes + " minutes " + seconds + " seconds.");
        }

        // Retrieve the user from the repository
        Optional<User> userOptional = Optional.ofNullable(userRepository.findByEmail(lowerEmail));
        User user = userOptional.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        // Check if the user is disabled (blocked via the 'enabled' flag)
        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("Your account is disabled. Please contact the administrator.");
        }

        // Return the UserDetails with the user's role
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole() != null ? user.getRole() : "ROLE_USER"))
        );
    }

}
