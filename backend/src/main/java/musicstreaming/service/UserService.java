package musicstreaming.service;

import lombok.RequiredArgsConstructor;
import musicstreaming.dto.AuthResponse;
import musicstreaming.dto.RegisterRequest;
import musicstreaming.dto.UpdateProfileRequest;
import musicstreaming.dto.UserResponse;
import musicstreaming.model.AppUser;
import musicstreaming.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String role = user.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER";
        return new User(user.getUsername(), user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(role)));
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new RuntimeException("Username already exists"); // Rule 2
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already exists"); // Rule 2
        }

        AppUser newUser = AppUser.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password())) // Rule 3
                .isAdmin(false)                                          // Rule 1 / 4.1
                .build();

        userRepository.save(newUser);
        UserDetails userDetails = loadUserByUsername(newUser.getUsername());
        return new AuthResponse(jwtService.generateToken(userDetails));
    }

    public UserResponse getMe(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new UserResponse(user.getUserId(), user.getUsername(), user.getEmail(), user.isAdmin());
    }

    @Transactional
    public UserResponse updateProfile(String currentUsername, UpdateProfileRequest request) {
        AppUser user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Rule 3: Only hash when a new password is actually provided
        String newHash = user.getPasswordHash();
        if (request.password() != null && !request.password().isBlank()) {
            newHash = passwordEncoder.encode(request.password());
        }

        // Rule 2: Username/email UNIQUE constraints are enforced by DB.
        // DataIntegrityViolationException is mapped to 409 by GlobalExceptionHandler.
        userRepository.updateProfile(user.getUserId(), request.username(), request.email(), newHash);
        
        return new UserResponse(user.getUserId(), request.username(), request.email(), user.isAdmin());
    }
}