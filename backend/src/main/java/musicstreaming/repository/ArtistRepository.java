package musicstreaming.repository;

import lombok.RequiredArgsConstructor;
import musicstreaming.dto.ArtistResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ArtistRepository {
    private final JdbcTemplate jdbcTemplate;

    public ArtistResponse create(Integer userId, String artistName) {
        String sql = """
                INSERT INTO artist (artist_id, artist_name)
                VALUES (?, ?)
                RETURNING artist_id, artist_name
                """;
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new ArtistResponse(rs.getInt("artist_id"), rs.getString("artist_name")),
                userId, artistName);
    }

    public Optional<ArtistResponse> findByUserId(Integer userId) {
        return jdbcTemplate.query(
                "SELECT artist_id, artist_name FROM artist WHERE artist_id = ?",
                (rs, rowNum) -> new ArtistResponse(rs.getInt("artist_id"), rs.getString("artist_name")),
                userId
        ).stream().findFirst();
    }

    public boolean exists(Integer userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM artist WHERE artist_id = ?", Integer.class, userId);
        return count != null && count > 0;
    }
}