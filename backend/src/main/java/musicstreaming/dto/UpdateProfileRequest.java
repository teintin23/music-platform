package musicstreaming.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @NotBlank @Size(min = 3, max = 50) String username,
    @NotBlank @Email @Size(max = 100) String email,
    @Size(min = 6, max = 72) String password // nullable — only update if provided
) {}