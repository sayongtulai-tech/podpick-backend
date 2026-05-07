package com.podpick.backend.playlist.controller;

import com.podpick.backend.playlist.domain.Playlist;
import com.podpick.backend.playlist.dto.PlaylistCreateRequest;
import com.podpick.backend.playlist.dto.PlaylistResponse;
import com.podpick.backend.playlist.dto.PlaylistUpdateRequest;
import com.podpick.backend.playlist.service.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "Playlists", description = "플레이리스트 API")
@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    // Service 계층에 비즈니스 로직을 위임하고, Controller는 HTTP 입출력만 담당합니다.
    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @Operation(summary = "플레이리스트 목록 조회")
    @GetMapping
    public List<PlaylistResponse> getPlaylists(
            @RequestHeader(value = "x-user-email", required = false) String userEmail
    ) {
        String ownerEmail = requireOwnerEmail(userEmail);
        return playlistService.getPlaylists(ownerEmail).stream()
                .map(PlaylistResponse::from)
                .toList();
    }

    @Operation(summary = "플레이리스트 생성")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public PlaylistResponse createPlaylist(
            @RequestHeader(value = "x-user-email", required = false) String userEmail,
            @Valid @RequestBody PlaylistCreateRequest request
    ) {
        String ownerEmail = requireOwnerEmail(userEmail);
        Playlist saved = playlistService.createPlaylist(request.title(), request.emotion(), request.musicUrl(), ownerEmail);
        return PlaylistResponse.from(saved);
    }

    @Operation(summary = "플레이리스트 수정")
    @PutMapping("/{id}")
    public PlaylistResponse updatePlaylist(
            @RequestHeader(value = "x-user-email", required = false) String userEmail,
            @PathVariable Long id,
            @Valid @RequestBody PlaylistUpdateRequest request
    ) {
        String ownerEmail = requireOwnerEmail(userEmail);
        Playlist updated = playlistService.updatePlaylist(id, request.title(), request.emotion(), request.musicUrl(), ownerEmail);
        return PlaylistResponse.from(updated);
    }

    @Operation(summary = "플레이리스트 삭제")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deletePlaylist(
            @RequestHeader(value = "x-user-email", required = false) String userEmail,
            @PathVariable Long id
    ) {
        String ownerEmail = requireOwnerEmail(userEmail);
        playlistService.deletePlaylist(id, ownerEmail);
    }

    @Operation(summary = "플레이리스트 좋아요 증가(PATCH)")
    @PatchMapping("/{id}/like")
    public PlaylistResponse increaseLikeCountByPatch(
            @RequestHeader(value = "x-user-email", required = false) String userEmail,
            @PathVariable Long id
    ) {
        String ownerEmail = requireOwnerEmail(userEmail);
        Playlist updated = playlistService.increaseLikeCount(id, ownerEmail);
        return PlaylistResponse.from(updated);
    }

    @Operation(summary = "플레이리스트 저장 수 증가(PATCH)")
    @PatchMapping("/{id}/save")
    public PlaylistResponse increaseSavedCountByPatch(
            @RequestHeader(value = "x-user-email", required = false) String userEmail,
            @PathVariable Long id
    ) {
        String ownerEmail = requireOwnerEmail(userEmail);
        Playlist updated = playlistService.increaseSavedCount(id, ownerEmail);
        return PlaylistResponse.from(updated);
    }

    private String requireOwnerEmail(String userEmail) {
        if (userEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        String normalized = userEmail.trim();
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "사용자 식별 정보(email)가 없습니다.");
        }
        return normalized;
    }
}

