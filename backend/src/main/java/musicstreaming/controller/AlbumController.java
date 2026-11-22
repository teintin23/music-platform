package musicstreaming.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import musicstreaming.dto.AlbumResponse;
import musicstreaming.dto.CreateAlbumRequest;
import musicstreaming.service.AlbumService;
import musicstreaming.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AlbumController {
    private final AlbumService albumService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<AlbumResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateAlbumRequest request) {
        Integer artistId = userService.getMe(userDetails.getUsername()).userId();
        return ResponseEntity.ok(albumService.create(artistId, request));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<AlbumResponse>> listMine(@AuthenticationPrincipal UserDetails userDetails) {
        Integer artistId = userService.getMe(userDetails.getUsername()).userId();
        return ResponseEntity.ok(albumService.listMine(artistId));
    }

    @PutMapping("/{albumId}")
    public ResponseEntity<AlbumResponse> update(
            @PathVariable Integer albumId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateAlbumRequest request) {
        Integer artistId = userService.getMe(userDetails.getUsername()).userId();
        return ResponseEntity.ok(albumService.update(albumId, artistId, request));
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer albumId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer artistId = userService.getMe(userDetails.getUsername()).userId();
        albumService.delete(albumId, artistId);
        return ResponseEntity.noContent().build();
    }
}