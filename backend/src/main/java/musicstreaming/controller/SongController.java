package musicstreaming.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import musicstreaming.dto.SongResponse;
import musicstreaming.dto.UploadSongRequest;
import musicstreaming.service.SongService;
import musicstreaming.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SongController {
    private final SongService songService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SongResponse> uploadSong(
            @AuthenticationPrincipal UserDetails userDetails,
            
            // accept as String to bypass Swagger UI's bug of sending JSON parts as application/octet-stream.
            @Parameter(description = "Song metadata in JSON format", schema = @Schema(implementation = UploadSongRequest.class))
            @RequestPart("metadata") String metadataJson,
            
            @RequestPart("file") MultipartFile file) {
        
        Integer artistId = userService.getMe(userDetails.getUsername()).userId();
        
        try {
            UploadSongRequest metadata = objectMapper.readValue(metadataJson, UploadSongRequest.class);
            return ResponseEntity.ok(songService.uploadSong(artistId, metadata, file));
        } catch (Exception e) {
            throw new RuntimeException("Invalid metadata format: " + e.getMessage());
        }
    }
}