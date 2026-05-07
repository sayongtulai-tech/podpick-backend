package com.podpick.backend.playlist.dto;

import com.podpick.backend.playlist.domain.Playlist;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PlaylistResponse", description = "플레이리스트 응답")
public record PlaylistResponse(
        Long id,
        String title,
        String emotion,
        String musicUrl,
        Long likeCount,
        Long savedCount
) {
    public static PlaylistResponse from(Playlist playlist) {
        return new PlaylistResponse(
                playlist.getId(),
                playlist.getTitle(),
                playlist.getEmotion(),
                playlist.getMusicUrl(),
                playlist.getLikeCount(),
                playlist.getSavedCount()
        );
    }
}

