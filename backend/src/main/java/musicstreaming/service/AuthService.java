package musicstreaming.service;

import lombok.RequiredArgsConstructor;
import musicstreaming.dto.AuthResponse;
import musicstreaming.dto.LoginRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        // 1. Authenticate username and password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        
        // 2. If successful, load user details and generate JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        return new AuthResponse(jwtService.generateToken(userDetails));
    }
}