package musicstreaming.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAlbumRequest(
    @NotBlank @Size(max = 150) String title
) {}
