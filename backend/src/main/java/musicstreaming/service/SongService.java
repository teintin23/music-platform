package musicstreaming.service;

import lombok.RequiredArgsConstructor;
import musicstreaming.dto.SongResponse;
import musicstreaming.dto.UploadSongRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SongService {
    private final JdbcTemplate jdbcTemplate;
    private final Mp3DurationReader durationReader;

    private static final Path STORAGE_DIR = Paths.get("uploads", "songs").toAbsolutePath();

    @Transactional(rollbackFor = Exception.class) // Ensures 1 transaction, rolls back on ANY error
    public SongResponse uploadSong(Integer artistId, UploadSongRequest request, MultipartFile file) {
        
        Integer isArtist = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM artist WHERE artist_id = ?", Integer.class, artistId);
        if (isArtist == null || isArtist == 0) {
            throw new RuntimeException("You need an artist profile before uploading songs");
        }

        // Rule 5: At least one genre
        if (request.genreIds() == null || request.genreIds().isEmpty()) {
            throw new RuntimeException("At least one genre is required");
        }

        if (request.albumId() != null) {
            Integer owner = jdbcTemplate.query(
                    "SELECT artist_id FROM album WHERE album_id = ?",
                    (rs, i) -> rs.getInt(1), request.albumId())
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Album not found"));
            if (!owner.equals(artistId)) {
                throw new RuntimeException("You don't own this album");
            }
        }
        // 1. Persist file to disk FIRST (avoids MultipartFile stream invalidation bugs)
        String storedName = UUID.randomUUID() + ".mp3";
        Path storedPath = STORAGE_DIR.resolve(storedName);
        try {
            Files.createDirectories(STORAGE_DIR);
            Files.copy(file.getInputStream(), storedPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Cannot save file: " + e.getMessage());
        }

        try {
            // 2. Read duration from the SAVED file
            int duration = durationReader.readDurationSeconds(storedPath.toFile());
            if (duration < 15 || duration > 30) {
                throw new RuntimeException("Duration must be between 15 and 30 seconds");
            }

            // 3. Insert song row (Rule 10: uploaded_time = now())
            String songSql = """
                INSERT INTO song (title, duration, file_url, album_id, uploaded_time)
                VALUES (?, ?, ?, ?, now())
                RETURNING song_id
                """;
            
            Integer songId = jdbcTemplate.queryForObject(songSql, Integer.class,
                    request.title(), duration, storedName, request.albumId());

            // 4. Insert song_artist: main artist (Rule 8 & 9: exactly one main)
            jdbcTemplate.update(
                    "INSERT INTO song_artist (song_id, artist_id, credit_role) VALUES (?, ?, 'main')",
                    songId, artistId);

            // 5. Featured artists (Rule 9: optional)
            if (request.featuredArtistIds() != null && !request.featuredArtistIds().isEmpty()) {
                // Deduplicate + remove the main artist (in case they put themselves in featured too)
                LinkedHashSet<Integer> cleanFeatured = new LinkedHashSet<>();
                for (Integer id : request.featuredArtistIds()) {
                    if (!id.equals(artistId)) {
                        cleanFeatured.add(id);
                    }
                }

                for (Integer featuredId : cleanFeatured) {
                    jdbcTemplate.update(
                            "INSERT INTO song_artist (song_id, artist_id, credit_role) VALUES (?, ?, 'featured')",
                            songId, featuredId);
                }
            }

            // 6. Insert song_genre rows (Rule 5)
            for (Integer genreId : request.genreIds()) {
                jdbcTemplate.update(
                        "INSERT INTO song_genre (song_id, genre_id) VALUES (?, ?)",
                        songId, genreId);
            }

            return new SongResponse(songId, request.title(), duration, request.albumId(), storedName);

        } catch (RuntimeException e) {
            // Rollback happened automatically via @Transactional. 
            // Also remove the orphaned file to ensure "no garbage records" (Task 2.2 requirement).
            try { Files.deleteIfExists(storedPath); } catch (IOException ignored) {}
            throw e;
        }
    }
}