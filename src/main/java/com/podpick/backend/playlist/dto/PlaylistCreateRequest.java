package com.podpick.backend.playlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PlaylistCreateRequest", description = "플레이리스트 생성 요청")
public record PlaylistCreateRequest(
        @NotBlank(message = "title은 비어 있을 수 없습니다.")
        @Size(max = 100, message = "title은 100자 이하여야 합니다.")
        @Schema(example = "비 오는 날 혼자 듣는 노래")
        String title,

        @NotBlank(message = "emotion은 비어 있을 수 없습니다.")
        @Size(max = 50, message = "emotion은 50자 이하여야 합니다.")
        @Schema(example = "우울함")
        String emotion,

        @Size(max = 500, message = "musicUrl은 500자 이하여야 합니다.")
        @Pattern(regexp = "^(|https?://.+)$", message = "musicUrl은 http:// 또는 https://로 시작해야 합니다.")
        @Schema(example = "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        String musicUrl
) {
}

