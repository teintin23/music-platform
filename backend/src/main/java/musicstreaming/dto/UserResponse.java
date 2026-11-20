package musicstreaming.dto;

public record UserResponse(Integer userId, String username, String email, boolean isAdmin) {}