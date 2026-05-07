package com.podpick.backend.playlist.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 50)
    private String emotion;

    @Column(length = 500)
    private String musicUrl;

    @Column(nullable = false, length = 320)
    private String ownerEmail;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long likeCount;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long savedCount;

    public Playlist(String title, String emotion, String musicUrl, String ownerEmail) {
        this.title = title;
        this.emotion = emotion;
        this.musicUrl = musicUrl;
        this.ownerEmail = ownerEmail;
        this.likeCount = 0L;
        this.savedCount = 0L;
    }

    @PrePersist
    public void prePersist() {
        // Defensive defaults for direct entity construction paths.
        if (this.likeCount < 0) {
            this.likeCount = 0L;
        }
        if (this.savedCount < 0) {
            this.savedCount = 0L;
        }
    }

    public void update(String title, String emotion, String musicUrl) {
        this.title = title;
        this.emotion = emotion;
        this.musicUrl = musicUrl;
    }

    public void increaseLikeCount() {
        this.likeCount += 1;
    }

    public void increaseSavedCount() {
        this.savedCount += 1;
    }
}

