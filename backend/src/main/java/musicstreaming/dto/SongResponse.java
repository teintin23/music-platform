package musicstreaming.dto;

public record SongResponse(
    Integer songId,
    String title,
    int duration,
    Integer albumId,
    String fileUrl
) {}