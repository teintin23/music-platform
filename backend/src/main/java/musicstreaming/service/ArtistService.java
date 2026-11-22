package musicstreaming.service;

import lombok.RequiredArgsConstructor;
import musicstreaming.dto.ArtistResponse;
import musicstreaming.dto.CreateArtistRequest;
import musicstreaming.model.AppUser;
import musicstreaming.repository.ArtistRepository;
import musicstreaming.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArtistService {
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;

    @Transactional
    public ArtistResponse createArtistProfile(Integer userId, CreateArtistRequest request) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Rule 1: An artist cannot be an admin
        if (user.isAdmin()) {
            throw new RuntimeException("Admin account cannot create an artist profile");
        }

        // Rule 4: Each artist account has exactly one artist profile
        if (artistRepository.exists(userId)) {
            throw new RuntimeException("Artist profile already exists for this account");
        }

        return artistRepository.create(userId, request.artistName());
    }
}