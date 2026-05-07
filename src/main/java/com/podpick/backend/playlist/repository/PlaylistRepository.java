package com.podpick.backend.playlist.repository;

import com.podpick.backend.playlist.domain.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByOwnerEmailOrderByIdDesc(String ownerEmail);

    Optional<Playlist> findByIdAndOwnerEmail(Long id, String ownerEmail);
}

