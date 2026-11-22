package musicstreaming.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import musicstreaming.dto.ArtistResponse;
import musicstreaming.dto.CreateArtistRequest;
import musicstreaming.dto.UpdateProfileRequest;
import musicstreaming.dto.UserResponse;
import musicstreaming.service.ArtistService;
import musicstreaming.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ArtistController {
    private final ArtistService artistService;
    private final UserService userService;

    @PostMapping("/artists")
    public ResponseEntity<ArtistResponse> createArtistProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateArtistRequest request) {
        Integer userId = userService.getMe(userDetails.getUsername()).userId();
        return ResponseEntity.ok(artistService.createArtistProfile(userId, request));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request));
    }
}