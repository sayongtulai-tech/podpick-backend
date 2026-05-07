package com.podpick.backend.playlist.service;

import com.podpick.backend.playlist.domain.Playlist;
import com.podpick.backend.global.exception.PlaylistNotFoundException;
import com.podpick.backend.playlist.repository.PlaylistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    public PlaylistService(PlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
    }

    // 최근에 생성된 항목이 먼저 보이도록 id 내림차순으로 조회합니다.
    public List<Playlist> getPlaylists(String ownerEmail) {
        return playlistRepository.findByOwnerEmailOrderByIdDesc(ownerEmail);
    }

    @Transactional
    public Playlist createPlaylist(String title, String emotion, String musicUrl, String ownerEmail) {
        return playlistRepository.save(new Playlist(title, emotion, normalizeMusicUrl(musicUrl), ownerEmail));
    }

    @Transactional
    public Playlist updatePlaylist(Long id, String title, String emotion, String musicUrl, String ownerEmail) {
        Playlist playlist = playlistRepository.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new PlaylistNotFoundException(id));
        playlist.update(title, emotion, normalizeMusicUrl(musicUrl));
        return playlist;
    }

    private String normalizeMusicUrl(String musicUrl) {
        if (musicUrl == null) {
            return null;
        }
        String trimmed = musicUrl.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Transactional
    public void deletePlaylist(Long id, String ownerEmail) {
        Playlist playlist = playlistRepository.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new PlaylistNotFoundException(id));
        playlistRepository.delete(playlist);
    }

    @Transactional
    public Playlist increaseLikeCount(Long id, String ownerEmail) {
        Playlist playlist = playlistRepository.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new PlaylistNotFoundException(id));
        playlist.increaseLikeCount();
        return playlistRepository.save(playlist);
    }

    @Transactional
    public Playlist increaseSavedCount(Long id, String ownerEmail) {
        Playlist playlist = playlistRepository.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new PlaylistNotFoundException(id));
        playlist.increaseSavedCount();
        return playlistRepository.save(playlist);
    }
}

