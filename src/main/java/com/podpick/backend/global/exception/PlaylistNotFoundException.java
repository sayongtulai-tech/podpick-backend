package com.podpick.backend.global.exception;

public class PlaylistNotFoundException extends RuntimeException {

    public PlaylistNotFoundException(Long id) {
        super("삭제할 플레이리스트를 찾을 수 없습니다. id=" + id);
    }
}
