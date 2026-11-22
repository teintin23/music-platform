package musicstreaming.repository;

import lombok.RequiredArgsConstructor;
import musicstreaming.dto.AlbumResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AlbumRepository {
    private final JdbcTemplate jdbcTemplate;

    public AlbumResponse create(Integer artistId, String title) {
        String sql = "INSERT INTO album (artist_id, title) VALUES (?, ?) RETURNING album_id";
        Integer albumId = jdbcTemplate.queryForObject(sql, Integer.class, artistId, title);
        return new AlbumResponse(albumId, title, artistId);
    }

    public List<AlbumResponse> findByArtist(Integer artistId) {
        return jdbcTemplate.query(
                "SELECT album_id, title, artist_id FROM album WHERE artist_id = ? ORDER BY album_id DESC",
                (rs, rowNum) -> new AlbumResponse(
                        rs.getInt("album_id"), rs.getString("title"), rs.getInt("artist_id")),
                artistId);
    }

    public Optional<AlbumResponse> findById(Integer albumId) {
        return jdbcTemplate.query(
                "SELECT album_id, title, artist_id FROM album WHERE album_id = ?",
                (rs, rowNum) -> new AlbumResponse(
                        rs.getInt("album_id"), rs.getString("title"), rs.getInt("artist_id")),
                albumId
        ).stream().findFirst();
    }

    public void updateTitle(Integer albumId, String title) {
        jdbcTemplate.update("UPDATE album SET title = ? WHERE album_id = ?", title, albumId);
    }

    public void delete(Integer albumId) {
        jdbcTemplate.update("DELETE FROM album WHERE album_id = ?", albumId);
    }
}
