package com.podpick.backend.playlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "PlaylistUpdateRequest", description = "플레이리스트 수정 요청")
public record PlaylistUpdateRequest(
        @NotBlank(message = "title은 비어 있을 수 없습니다.")
        @Size(max = 100, message = "title은 100자 이하여야 합니다.")
        @Schema(example = "퇴근 후 마음 정리 플레이리스트")
        String title,

        @NotBlank(message = "emotion은 비어 있을 수 없습니다.")
        @Size(max = 50, message = "emotion은 50자 이하여야 합니다.")
        @Schema(example = "편안함")
        String emotion,

        @Size(max = 500, message = "musicUrl은 500자 이하여야 합니다.")
        @Pattern(regexp = "^(|https?://.+)$", message = "musicUrl은 http:// 또는 https://로 시작해야 합니다.")
        @Schema(example = "https://music.youtube.com/watch?v=dQw4w9WgXcQ")
        String musicUrl
) {
}
