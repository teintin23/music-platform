package musicstreaming.service;

import lombok.RequiredArgsConstructor;
import musicstreaming.dto.AlbumResponse;
import musicstreaming.dto.CreateAlbumRequest;
import musicstreaming.repository.AlbumRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public AlbumResponse create(Integer artistId, CreateAlbumRequest request) {
        // Rule 7: Album belongs to exactly one artist (set from JWT, not request body)
        return albumRepository.create(artistId, request.title());
    }

    public List<AlbumResponse> listMine(Integer artistId) {
        return albumRepository.findByArtist(artistId);
    }

    @Transactional
    public AlbumResponse update(Integer albumId, Integer artistId, CreateAlbumRequest request) {
        AlbumResponse album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found"));
        
        // Ownership check (Task 5.1 prep)
        if (!album.artistId().equals(artistId)) {
            throw new RuntimeException("You don't own this album");
        }
        
        albumRepository.updateTitle(albumId, request.title());
        return new AlbumResponse(albumId, request.title(), artistId);
    }

    @Transactional
    public void delete(Integer albumId, Integer artistId) {
        AlbumResponse album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found"));
        
        if (!album.artistId().equals(artistId)) {
            throw new RuntimeException("You don't own this album");
        }
        
        // Rule 6: Songs may reference this album. Safe default: detach songs (album_id = NULL = single)
        jdbcTemplate.update("UPDATE song SET album_id = NULL WHERE album_id = ?", albumId);
        albumRepository.delete(albumId);
    }
}