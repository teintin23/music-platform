package musicstreaming.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UploadSongRequest(
    @NotBlank @Size(max = 150) String title,
    Integer albumId,                          // nullable — null means single (Rule 6)
    @NotEmpty List<Integer> genreIds,         // Rule 5: at least one genre
    List<Integer> featuredArtistIds           // Rule 9: optional featured artists
) {}