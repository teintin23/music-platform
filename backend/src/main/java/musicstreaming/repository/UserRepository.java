package musicstreaming.repository;

import lombok.RequiredArgsConstructor;
import musicstreaming.model.AppUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<AppUser> rowMapper = (rs, rowNum) -> AppUser.builder()
            .userId(rs.getInt("user_id"))
            .username(rs.getString("username"))
            .email(rs.getString("email"))
            .passwordHash(rs.getString("password_hash"))
            .isAdmin(rs.getBoolean("is_admin"))
            .createdTime(rs.getTimestamp("created_time").toLocalDateTime())
            .build();

    public AppUser save(AppUser user) {
        String sql = """
                INSERT INTO app_user (username, email, password_hash, is_admin)
                VALUES (?, ?, ?, ?)
                RETURNING user_id, created_time
                """;
        jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            user.setUserId(rs.getInt("user_id"));
            user.setCreatedTime(rs.getTimestamp("created_time").toLocalDateTime());
            return user;
        }, user.getUsername(), user.getEmail(), user.getPasswordHash(), user.isAdmin());
        return user;
    }

    public Optional<AppUser> findByUsername(String username) {
        return jdbcTemplate.query("SELECT * FROM app_user WHERE username = ?", rowMapper, username)
                .stream().findFirst();
    }

    public Optional<AppUser> findByEmail(String email) {
        return jdbcTemplate.query("SELECT * FROM app_user WHERE email = ?", rowMapper, email)
                .stream().findFirst();
    }

    public Optional<AppUser> findById(Integer userId) {
        return jdbcTemplate.query("SELECT * FROM app_user WHERE user_id = ?", rowMapper, userId)
                .stream().findFirst();
    }
}