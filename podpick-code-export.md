# PodPick Code Export

요청한 필수 파일들의 현재 코드입니다.

---

## Frontend

### `app/page.tsx`
```tsx
"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { usePlayer } from "@/components/player/PlayerProvider";
import { Playlist } from "@/types/playlist";
import { useSession } from "next-auth/react";

const EMOTIONS = ["전체", "새벽감성", "행복", "설렘", "힐링", "집중", "우울함", "운동"] as const;
const SORT_OPTIONS = ["최신순", "오래된순", "제목순", "좋아요순", "저장순"] as const;

export default function HomePage() {
  const { data: session } = useSession();
  const { playlists, loading, error, playPlaylist, refreshPlaylists } = usePlayer();
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [title, setTitle] = useState("");
  const [emotion, setEmotion] = useState<(typeof EMOTIONS)[number]>("새벽감성");
  const [musicUrl, setMusicUrl] = useState("");
  const [keyword, setKeyword] = useState("");
  const [emotionFilter, setEmotionFilter] = useState<(typeof EMOTIONS)[number]>("전체");
  const [sortOption, setSortOption] = useState<(typeof SORT_OPTIONS)[number]>("최신순");
  const [submitting, setSubmitting] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);

  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(null), 1800);
    return () => window.clearTimeout(timer);
  }, [toast]);

  const topLiked = useMemo(
    () => [...playlists].sort((a, b) => (b.likeCount ?? 0) - (a.likeCount ?? 0)).slice(0, 6),
    [playlists]
  );

  const filteredPlaylists = useMemo(() => {
    const q = keyword.trim().toLowerCase();
    const filtered = playlists.filter((item) => {
      const byEmotion = emotionFilter === "전체" || item.emotion === emotionFilter;
      const byKeyword =
        q.length === 0 ||
        item.title.toLowerCase().includes(q) ||
        item.emotion.toLowerCase().includes(q);
      return byEmotion && byKeyword;
    });

    const sorted = [...filtered];
    if (sortOption === "최신순") sorted.sort((a, b) => b.id - a.id);
    if (sortOption === "오래된순") sorted.sort((a, b) => a.id - b.id);
    if (sortOption === "제목순") sorted.sort((a, b) => a.title.localeCompare(b.title, "ko"));
    if (sortOption === "좋아요순") sorted.sort((a, b) => (b.likeCount ?? 0) - (a.likeCount ?? 0));
    if (sortOption === "저장순") sorted.sort((a, b) => (b.savedCount ?? 0) - (a.savedCount ?? 0));
    return sorted;
  }, [playlists, keyword, emotionFilter, sortOption]);

  async function handleCreatePlaylist(event: FormEvent) {
    event.preventDefault();
    if (!session) {
      setToast("로그인이 필요한 기능이에요 😊");
      return;
    }
    if (!title.trim()) {
      setCreateError("플레이리스트 제목을 입력해 주세요.");
      return;
    }

    setCreateError(null);
    setSubmitting(true);
    try {
      const payload = {
        title: title.trim(),
        emotion,
        musicUrl: musicUrl.trim(),
      };
      const response = await fetch("/api/playlists", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (!response.ok) {
        const errorData = (await response.json().catch(() => null)) as { message?: string } | null;
        throw new Error(errorData?.message ?? "플레이리스트를 추가하지 못했습니다.");
      }

      setTitle("");
      setEmotion("새벽감성");
      setMusicUrl("");
      setShowCreateForm(false);
      await refreshPlaylists();
    } catch (e) {
      setCreateError(e instanceof Error ? e.message : "오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleLike(id: number) {
    if (!session) {
      setToast("로그인이 필요한 기능이에요 😊");
      return;
    }
    await fetch(`/api/playlists/${id}/like`, { method: "PATCH" });
    await refreshPlaylists();
  }

  async function handleSaveCount(id: number) {
    if (!session) {
      setToast("로그인이 필요한 기능이에요 😊");
      return;
    }
    await fetch(`/api/playlists/${id}/save`, { method: "PATCH" });
    await refreshPlaylists();
  }

  function formatDateFromId(id: number) {
    const d = new Date();
    d.setDate(d.getDate() - Math.max(0, 50 - id));
    return d.toLocaleDateString("ko-KR");
  }

  return (
    <div className="space-y-5">
      <section className="rounded-2xl border border-white/10 bg-white/[0.04] p-5">
        <h1 className="text-2xl font-black text-white md:text-3xl">
          지금 기분에 맞는{" "}
          <span className="bg-gradient-to-r from-violet-300 to-pink-300 bg-clip-text text-transparent">
            PodPick
          </span>
        </h1>
        <p className="mt-2 text-sm text-slate-300">감정으로 음악을 고르고, 사람들이 만든 플레이리스트를 발견하세요.</p>
      </section>

      <section className="rounded-2xl border border-white/10 bg-white/[0.04] p-5">
        <h2 className="text-lg font-bold text-white">인기 플레이리스트</h2>
        {loading ? (
          <p className="mt-3 text-sm text-slate-400">불러오는 중...</p>
        ) : error ? (
          <p className="mt-3 text-sm text-rose-300">{error}</p>
        ) : topLiked.length === 0 ? (
          <p className="mt-3 text-sm text-slate-400">아직 플레이리스트가 없어요 🎵</p>
        ) : (
          <div className="mt-4 grid gap-3 md:grid-cols-2 xl:grid-cols-3">
            {topLiked.map((item) => (
              <button
                key={item.id}
                type="button"
                onClick={() => playPlaylist(item)}
                className="rounded-xl border border-white/10 bg-[#16162a] p-4 text-left transition hover:border-violet-300/40 hover:bg-[#1a1a31]"
              >
                <p className="truncate text-sm font-semibold text-white">{item.title}</p>
                <p className="mt-1 text-xs text-slate-400">{item.emotion}</p>
                <p className="mt-2 text-xs text-slate-300">
                  좋아요 {item.likeCount ?? 0} · 저장 {item.savedCount ?? 0}
                </p>
              </button>
            ))}
          </div>
        )}
      </section>

      <section className="rounded-2xl border border-white/10 bg-white/[0.04] p-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h2 className="text-lg font-bold text-white">플레이리스트 관리</h2>
          <button
            type="button"
            onClick={() => {
              if (!session) {
                setToast("로그인이 필요한 기능이에요 😊");
                return;
              }
              setShowCreateForm((prev) => !prev);
            }}
            className="rounded-lg bg-gradient-to-r from-violet-500 to-pink-500 px-4 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60"
            disabled={!session}
          >
            {showCreateForm ? "폼 닫기" : "플레이리스트 추가"}
          </button>
        </div>

        {showCreateForm && (
          <form onSubmit={handleCreatePlaylist} className="mt-4 grid gap-2 rounded-xl border border-white/10 bg-[#16162a] p-4 md:grid-cols-4">
            <input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="제목"
              className="rounded-lg border border-white/10 bg-white/5 px-3 py-2 text-sm text-white outline-none placeholder:text-slate-500"
            />
            <select
              value={emotion}
              onChange={(e) => setEmotion(e.target.value as (typeof EMOTIONS)[number])}
              className="rounded-lg border border-white/10 bg-white/5 px-3 py-2 text-sm text-white outline-none"
            >
              {EMOTIONS.filter((item) => item !== "전체").map((item) => (
                <option key={item} value={item} className="bg-[#16162a]">
                  {item}
                </option>
              ))}
            </select>
            <input
              value={musicUrl}
              onChange={(e) => setMusicUrl(e.target.value)}
              placeholder="음악 URL (선택)"
              className="rounded-lg border border-white/10 bg-white/5 px-3 py-2 text-sm text-white outline-none placeholder:text-slate-500"
            />
            <button
              type="submit"
              disabled={submitting}
              className="rounded-lg border border-white/20 bg-white/10 px-3 py-2 text-sm font-semibold text-white disabled:opacity-60"
            >
              {submitting ? "추가 중..." : "추가하기"}
            </button>
            {createError && <p className="md:col-span-4 text-xs text-rose-300">{createError}</p>}
          </form>
        )}

        <div className="mt-4 flex flex-wrap items-center gap-2">
          {EMOTIONS.map((tab) => (
            <button
              key={tab}
              type="button"
              onClick={() => setEmotionFilter(tab)}
              className={`rounded-full border px-3 py-1 text-xs font-medium transition ${
                emotionFilter === tab
                  ? "border-violet-300/60 bg-violet-500/25 text-violet-100"
                  : "border-white/15 bg-white/5 text-slate-300 hover:bg-white/10"
              }`}
            >
              {tab}
            </button>
          ))}
        </div>

        <div className="mt-3 grid gap-2 md:grid-cols-[1fr_180px]">
          <input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="제목/감정 검색"
            className="rounded-lg border border-white/10 bg-white/5 px-3 py-2 text-sm text-white outline-none placeholder:text-slate-500"
          />
          <select
            value={sortOption}
            onChange={(e) => setSortOption(e.target.value as (typeof SORT_OPTIONS)[number])}
            className="rounded-lg border border-white/10 bg-white/5 px-3 py-2 text-sm text-white outline-none"
          >
            {SORT_OPTIONS.map((option) => (
              <option key={option} value={option} className="bg-[#16162a]">
                {option}
              </option>
            ))}
          </select>
        </div>

        <div className="mt-4 overflow-x-auto rounded-xl border border-white/10">
          <table className="w-full min-w-[720px] text-left text-sm">
            <thead className="bg-white/5 text-xs uppercase tracking-wide text-slate-400">
              <tr>
                <th className="px-3 py-2">제목</th>
                <th className="px-3 py-2">감정</th>
                <th className="px-3 py-2">좋아요</th>
                <th className="px-3 py-2">저장</th>
                <th className="px-3 py-2">등록일</th>
                <th className="px-3 py-2">액션</th>
              </tr>
            </thead>
            <tbody>
              {filteredPlaylists.map((item: Playlist) => (
                <tr key={item.id} className="border-t border-white/10">
                  <td className="px-3 py-2 font-medium text-white">{item.title}</td>
                  <td className="px-3 py-2 text-slate-300">{item.emotion}</td>
                  <td className="px-3 py-2 text-slate-300">{item.likeCount ?? 0}</td>
                  <td className="px-3 py-2 text-slate-300">{item.savedCount ?? 0}</td>
                  <td className="px-3 py-2 text-slate-400">{formatDateFromId(item.id)}</td>
                  <td className="px-3 py-2">
                    <div className="flex gap-1">
                      <button
                        type="button"
                        onClick={() => playPlaylist(item)}
                        className="rounded-md border border-violet-300/40 bg-violet-500/20 px-2 py-1 text-xs text-violet-100"
                      >
                        재생
                      </button>
                      <button
                        type="button"
                        onClick={() => handleLike(item.id)}
                        disabled={!session}
                        className="rounded-md border border-pink-300/40 bg-pink-500/20 px-2 py-1 text-xs text-pink-100 disabled:cursor-not-allowed disabled:opacity-60"
                      >
                        ❤️
                      </button>
                      <button
                        type="button"
                        onClick={() => handleSaveCount(item.id)}
                        disabled={!session}
                        className="rounded-md border border-emerald-300/40 bg-emerald-500/20 px-2 py-1 text-xs text-emerald-100 disabled:cursor-not-allowed disabled:opacity-60"
                      >
                        📌
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {!loading && filteredPlaylists.length === 0 && (
            <p className="px-3 py-5 text-sm text-slate-400">조건에 맞는 플레이리스트가 없습니다.</p>
          )}
        </div>
      </section>
      {toast && (
        <div className="fixed bottom-24 right-4 z-40 rounded-lg border border-white/15 bg-[#1b1b30] px-4 py-2 text-sm text-slate-100 shadow-xl">
          {toast}
        </div>
      )}
    </div>
  );
}
```

### `app/layout.tsx`
```tsx
import type { Metadata } from "next";
import "./globals.css";
import AppFrame from "@/components/app/AppFrame";
import Providers from "./providers";

export const metadata: Metadata = {
  title: "PodPick",
  description: "Emotional playlist curator service",
  manifest: "/manifest.json",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body
        className="min-h-screen bg-[#0f0f1a] text-white"
        style={{ background: "#0f0f1a", minHeight: "100vh", color: "white" }}
      >
        <Providers>
          <AppFrame>{children}</AppFrame>
        </Providers>
      </body>
    </html>
  );
}
```

### `app/globals.css`
```css
@import url("https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;600;700;800&display=swap");

@tailwind base;
@tailwind components;
@tailwind utilities;

body {
  min-height: 100vh;
  background:
    radial-gradient(circle at 10% 10%, rgba(244, 114, 182, 0.24), transparent 32%),
    radial-gradient(circle at 90% 15%, rgba(94, 234, 212, 0.18), transparent 35%),
    linear-gradient(145deg, #0f172a 0%, #111827 45%, #1e1b4b 100%);
  color: #f8fafc;
  font-family: "Noto Sans KR", sans-serif;
  letter-spacing: -0.01em;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in {
  animation: fadeInUp 0.45s ease-out both;
}

@keyframes toastSlideIn {
  from {
    opacity: 0;
    transform: translateX(24px) translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateX(0) translateY(0);
  }
}

.animate-toast-in {
  animation: toastSlideIn 0.25s ease-out both;
}

@keyframes equalize {
  0%,
  100% {
    transform: scaleY(0.4);
  }
  50% {
    transform: scaleY(1);
  }
}

.eq-bar {
  animation: equalize 0.8s ease-in-out infinite;
  transform-origin: bottom;
}

@keyframes waveformPulse {
  0%,
  100% {
    transform: scaleY(0.25);
  }
  50% {
    transform: scaleY(1);
  }
}

.waveform {
  display: flex;
  align-items: end;
  gap: 3px;
  height: 30px;
}

.waveform-bar {
  width: 4px;
  height: 100%;
  border-radius: 9999px;
  background: linear-gradient(180deg, #a855f7 0%, #ec4899 100%);
  transform-origin: bottom;
  animation: waveformPulse 0.9s ease-in-out infinite;
  animation-play-state: paused;
}

.waveform.running .waveform-bar {
  animation-play-state: running;
}

.waveform.paused .waveform-bar {
  animation-play-state: paused;
  transform: scaleY(0.22);
}

@keyframes marquee {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}

::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

::-webkit-scrollbar-track {
  background: #111225;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #6d28d9, #ec4899);
  border-radius: 9999px;
}

input[type="range"] {
  -webkit-appearance: none;
  appearance: none;
  background: transparent;
}

input[type="range"]::-webkit-slider-runnable-track {
  height: 6px;
  border-radius: 9999px;
  background: linear-gradient(90deg, #a855f7, #ec4899);
}

input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 14px;
  height: 14px;
  margin-top: -4px;
  border-radius: 9999px;
  background: #ec4899;
  box-shadow: 0 0 10px rgba(236, 72, 153, 0.5);
}

@keyframes introFadeOut {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

@keyframes introDot {
  0%,
  100% {
    opacity: 0.35;
    transform: translateY(0);
  }
  50% {
    opacity: 1;
    transform: translateY(-4px);
  }
}

.intro-fade {
  animation: introFadeOut 0.45s ease-out 1.05s both;
}

.intro-loading-dot {
  width: 8px;
  height: 8px;
  border-radius: 9999px;
  background: linear-gradient(180deg, #c4b5fd 0%, #f9a8d4 100%);
  animation: introDot 0.8s ease-in-out infinite;
}

.intro-fade-up {
  opacity: 0;
  transform: translateY(16px);
  animation: fadeInUp 0.55s ease-out forwards;
}

.intro-chip {
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.intro-chip:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 26px rgba(168, 85, 247, 0.25);
}

.intro-start-btn {
  transition: box-shadow 0.2s ease, transform 0.2s ease, filter 0.2s ease;
}

.intro-start-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 0 24px rgba(236, 72, 153, 0.45);
  filter: brightness(1.05);
}
```

### `components/PlaylistCard.tsx`
```tsx
import { Playlist } from "@/types/playlist";

type PlaylistCardProps = {
  playlist: Playlist;
  deleting: boolean;
  editing: boolean;
  engaging: boolean;
  selected: boolean;
  playing: boolean;
  onDelete: (id: number) => void;
  onEdit: (playlist: Playlist) => void;
  onLike: (id: number) => void;
  onSave: (id: number) => void;
  onSelect: (playlist: Playlist) => void;
  onPlay: (playlist: Playlist) => void;
};

export default function PlaylistCard({
  playlist,
  deleting,
  editing,
  engaging,
  selected,
  playing,
  onDelete,
  onEdit,
  onLike,
  onSave,
  onSelect,
  onPlay,
}: PlaylistCardProps) {
  const hasMusicUrl =
    typeof playlist.musicUrl === "string" &&
    (playlist.musicUrl.startsWith("http://") || playlist.musicUrl.startsWith("https://"));

  return (
    <article
      onClick={() => onSelect(playlist)}
      className={`animate-fade-in rounded-2xl border bg-white/10 p-5 shadow-aurora backdrop-blur-sm transition duration-300 hover:-translate-y-1 hover:scale-[1.02] hover:bg-white/15 hover:shadow-[0_18px_45px_rgba(236,72,153,0.35)] ${
        playing
          ? "border-indigo-300/80 ring-2 ring-indigo-300/50 shadow-[0_0_35px_rgba(99,102,241,0.45)]"
          : selected
            ? "border-mintnote/60 ring-1 ring-mintnote/40"
            : "border-white/15"
      }`}
    >
      <div className="mb-3 flex items-center justify-between">
        <span className="rounded-full bg-mintnote/20 px-3 py-1 text-xs font-semibold text-mintnote">
          #{playlist.id}
        </span>
        <span className="rounded-full bg-roseglow/20 px-3 py-1 text-xs font-semibold text-roseglow">
          {playlist.emotion}
        </span>
      </div>
      <h3 className="line-clamp-2 text-lg font-semibold text-white">{playlist.title}</h3>
      {playing && (
        <p className="mt-2 text-xs font-semibold text-indigo-200">지금 미니 플레이어에서 재생 중</p>
      )}
      {hasMusicUrl && (
        <button
          type="button"
          onClick={() => onPlay(playlist)}
          className="mt-3 block rounded-lg border border-indigo-300/40 bg-indigo-400/10 px-3 py-2 text-center text-sm font-medium text-indigo-100 transition duration-200 hover:scale-[1.02] hover:border-indigo-200/70 hover:bg-indigo-400/30 hover:shadow-[0_8px_25px_rgba(99,102,241,0.35)] active:scale-95"
        >
          재생
        </button>
      )}
      <div className="mt-4 grid grid-cols-2 gap-2">
        <button
          type="button"
          onClick={() => onLike(playlist.id)}
          disabled={engaging}
          className="rounded-lg border border-yellow-300/50 bg-yellow-300/10 px-3 py-2 text-sm font-medium text-yellow-100 transition duration-200 hover:scale-[1.02] hover:bg-yellow-300/25 active:scale-95 disabled:cursor-not-allowed disabled:opacity-60"
        >
          좋아요 {playlist.likeCount ?? 0}
        </button>
        <button
          type="button"
          onClick={() => onSave(playlist.id)}
          disabled={engaging}
          className="rounded-lg border border-cyan-300/50 bg-cyan-300/10 px-3 py-2 text-sm font-medium text-cyan-100 transition duration-200 hover:scale-[1.02] hover:bg-cyan-300/25 active:scale-95 disabled:cursor-not-allowed disabled:opacity-60"
        >
          저장 {playlist.savedCount ?? 0}
        </button>
      </div>
      <div className="mt-2 grid grid-cols-2 gap-2">
        <button
          type="button"
          onClick={() => onEdit(playlist)}
          disabled={deleting || engaging}
          className="rounded-lg border border-mintnote/50 bg-mintnote/10 px-3 py-2 text-sm font-medium text-mintnote transition duration-200 hover:scale-[1.02] hover:bg-mintnote/25 active:scale-95 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {editing ? "수정 중" : "수정"}
        </button>
        <button
          type="button"
          onClick={() => onDelete(playlist.id)}
          disabled={deleting || engaging}
          className="rounded-lg border border-roseglow/50 bg-roseglow/10 px-3 py-2 text-sm font-medium text-rose-100 transition duration-200 hover:scale-[1.02] hover:bg-roseglow/25 active:scale-95 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {deleting ? "삭제 중..." : "삭제"}
        </button>
      </div>
    </article>
  );
}
```

### `components/app/AppFrame.tsx`
```tsx
"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { signIn, signOut, useSession } from "next-auth/react";
import PlayerProvider, { usePlayer } from "@/components/player/PlayerProvider";

function formatTime(sec: number) {
  if (!Number.isFinite(sec) || sec < 0) return "0:00";
  const m = Math.floor(sec / 60);
  const s = Math.floor(sec % 60);
  return `${m}:${String(s).padStart(2, "0")}`;
}

function Shell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const { data: session, status } = useSession();
  const {
    selectedPlaylist,
    isPlaying,
    volume,
    isMuted,
    currentTimeSec,
    durationSec,
    togglePlay,
    setVolumeLevel,
    toggleMute,
    seekTo,
  } = usePlayer();

  const menu = [
    { href: "/", label: "홈", icon: "🏠" },
    { href: "/explore", label: "탐색", icon: "🔎" },
    { href: "/bookmarks", label: "내 보관함", icon: "📁" },
  ];

  if (pathname.startsWith("/intro")) {
    return <>{children}</>;
  }

  return (
    <main className="min-h-screen bg-[#0f0f1a] pb-32 text-slate-100">
      <div className="mx-auto flex max-w-[1600px] items-center justify-end px-3 pt-4 md:px-6">
        {status === "authenticated" && session?.user ? (
          <div className="flex items-center gap-2 rounded-full border border-white/15 bg-white/5 px-3 py-1.5">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={session.user.image ?? "https://placehold.co/32x32/png"}
              alt="profile"
              className="h-7 w-7 rounded-full object-cover"
            />
            <span className="text-xs font-semibold text-slate-100">{session.user.name ?? "사용자"}</span>
          </div>
        ) : (
          <button
            type="button"
            onClick={() => signIn("google")}
            className="rounded-lg border border-white/15 bg-white/5 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-white/10"
          >
            로그인
          </button>
        )}
      </div>
      <div className="mx-auto flex max-w-[1600px] gap-4 px-3 py-4 md:gap-6 md:px-6">
        <aside className="hidden shrink-0 rounded-2xl border border-white/10 bg-white/[0.04] p-3 backdrop-blur-md md:block md:w-16 lg:w-60">
          <p className="text-lg font-bold tracking-wide text-violet-300">PODPICK</p>
          <nav className="mt-6 space-y-2">
            {menu.map((item) => {
              const active = pathname === item.href;
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`block w-full rounded-lg px-3 py-2 text-sm font-medium transition ${
                    active
                      ? "bg-gradient-to-r from-violet-500/30 to-pink-500/30 text-white"
                      : "text-slate-300 hover:bg-white/10"
                  }`}
                >
                  <span className="hidden lg:inline">{item.label}</span>
                  <span className="lg:hidden">{item.icon}</span>
                </Link>
              );
            })}
          </nav>

          <div className="mt-8 border-t border-white/10 pt-4">
            {status === "loading" ? (
              <div className="rounded-lg border border-white/10 bg-white/5 px-3 py-2 text-xs text-slate-300">
                로그인 상태 확인 중...
              </div>
            ) : session?.user ? (
              <div className="space-y-3">
                <Link
                  href="/profile"
                  className={`flex items-center gap-2 rounded-lg p-2 transition ${
                    pathname === "/profile" ? "bg-white/10" : "hover:bg-white/10"
                  }`}
                >
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src={session.user.image ?? "https://placehold.co/64x64/png"}
                    alt="profile"
                    className="h-8 w-8 rounded-full object-cover"
                  />
                  <div className="hidden min-w-0 lg:block">
                    <span className="block truncate text-sm font-medium text-slate-200">
                      {session.user.name ?? "사용자"}
                    </span>
                    <span className="block truncate text-[11px] text-slate-400">
                      {session.user.email ?? ""}
                    </span>
                  </div>
                </Link>
                <button
                  type="button"
                  onClick={() => signOut({ callbackUrl: "/" })}
                  className="w-full rounded-lg border border-rose-400/40 bg-rose-500/10 px-3 py-2 text-xs font-semibold text-rose-200"
                >
                  로그아웃
                </button>
              </div>
            ) : (
              <button
                type="button"
                onClick={() => signIn("google")}
                className="flex w-full items-center justify-center gap-2 rounded-lg border border-white/15 bg-white/5 px-3 py-2 text-xs font-bold text-white transition hover:bg-white/10"
              >
                <span className="inline-flex h-4 w-4 items-center justify-center rounded-full border border-white/50 text-[10px] text-white">
                  G
                </span>
                <span>Google로 로그인</span>
              </button>
            )}
          </div>
        </aside>

        <section className="min-w-0 flex-1">{children}</section>

        <aside className="hidden w-80 shrink-0 rounded-2xl border border-white/10 bg-white/[0.04] p-4 xl:block">
          <p className="text-xs font-semibold tracking-wide text-slate-400">현재 재생</p>
          <div className="mt-3 rounded-xl border border-white/10 bg-[#16162a] p-4">
            <p className="truncate text-sm font-semibold text-white">
              {selectedPlaylist?.title ?? "재생 중인 곡이 없습니다"}
            </p>
            <p className="mt-1 text-xs text-slate-400">
              {selectedPlaylist?.emotion ?? "탐색 페이지에서 곡을 선택해보세요"}
            </p>
          </div>
        </aside>
      </div>

      <footer className="fixed bottom-0 left-0 right-0 z-30 border-t border-white/10 bg-[#101022]/95 px-4 py-3 backdrop-blur-md">
        <div className="mx-auto grid max-w-[1600px] grid-cols-1 items-center gap-3 md:grid-cols-[1.2fr_1fr_1fr]">
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold text-slate-100">
              {selectedPlaylist?.title ?? "아직 선택된 곡이 없어요"}
            </p>
            <p className="truncate text-xs text-slate-400">{selectedPlaylist?.emotion ?? "PodPick"}</p>
          </div>

          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={togglePlay}
              className="rounded-full border border-white/20 bg-white/10 px-3 py-1 text-xs font-semibold"
            >
              {isPlaying ? "⏸" : "▶"}
            </button>
            <input
              type="range"
              min={0}
              max={Math.max(1, Math.floor(durationSec))}
              value={Math.min(currentTimeSec, durationSec || 1)}
              onChange={(e) => seekTo(Number(e.target.value))}
              onInput={(e) => seekTo(Number((e.target as HTMLInputElement).value))}
              className="w-full"
            />
            <span className="text-xs text-slate-300">
              {formatTime(currentTimeSec)} / {formatTime(durationSec)}
            </span>
          </div>

          <div className="flex items-center justify-start gap-2 md:justify-end">
            <button
              type="button"
              onClick={toggleMute}
              className="rounded-lg border border-white/15 px-2 py-1 text-xs"
            >
              {isMuted ? "🔇" : "🔊"}
            </button>
            <input
              type="range"
              min={0}
              max={100}
              value={isMuted ? 0 : volume}
              onChange={(e) => setVolumeLevel(Number(e.target.value))}
              onInput={(e) => setVolumeLevel(Number((e.target as HTMLInputElement).value))}
              className="w-28"
            />
          </div>
        </div>
      </footer>
    </main>
  );
}

export default function AppFrame({ children }: { children: React.ReactNode }) {
  return (
    <PlayerProvider>
      <Shell>{children}</Shell>
    </PlayerProvider>
  );
}
```

### `components/player/PlayerProvider.tsx`
```tsx
"use client";

import { createContext, useContext, useEffect, useMemo, useRef, useState } from "react";
import { Playlist } from "@/types/playlist";

type PlayerContextType = {
  playlists: Playlist[];
  loading: boolean;
  error: string | null;
  selectedPlaylist: Playlist | null;
  isPlaying: boolean;
  volume: number;
  isMuted: boolean;
  currentTimeSec: number;
  durationSec: number;
  refreshPlaylists: () => Promise<void>;
  playPlaylist: (playlist: Playlist) => void;
  togglePlay: () => void;
  setVolumeLevel: (value: number) => void;
  toggleMute: () => void;
  seekTo: (sec: number) => void;
};

const PlayerContext = createContext<PlayerContextType | null>(null);

declare global {
  interface Window {
    YT?: any;
    onYouTubeIframeAPIReady?: () => void;
  }
}

function getYouTubeVideoId(url: string | null) {
  if (!url) return null;
  try {
    const parsed = new URL(url);
    const host = parsed.hostname.replace("www.", "");
    if (host === "youtu.be") return parsed.pathname.split("/").filter(Boolean)[0] ?? null;
    if (host === "youtube.com" || host === "m.youtube.com" || host === "music.youtube.com") {
      if (parsed.pathname === "/watch") return parsed.searchParams.get("v");
      if (parsed.pathname.startsWith("/shorts/") || parsed.pathname.startsWith("/embed/")) {
        return parsed.pathname.split("/")[2] ?? null;
      }
    }
  } catch {
    return null;
  }
  return null;
}

type Props = { children: React.ReactNode };

const mockPlaylists: Playlist[] = [
  {
    id: 1,
    title: "새벽에 듣는 재즈",
    emotion: "새벽감성",
    musicUrl: "",
    likeCount: 5,
    savedCount: 3,
  },
  {
    id: 2,
    title: "기분 좋은 하루",
    emotion: "행복",
    musicUrl: "",
    likeCount: 8,
    savedCount: 6,
  },
];

export default function PlayerProvider({ children }: Props) {
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedPlaylist, setSelectedPlaylist] = useState<Playlist | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [ytApiReady, setYtApiReady] = useState(false);
  const [volume, setVolume] = useState(80);
  const [isMuted, setIsMuted] = useState(false);
  const [currentTimeSec, setCurrentTimeSec] = useState(0);
  const [durationSec, setDurationSec] = useState(0);
  const playerRef = useRef<any>(null);
  const playerHostRef = useRef<HTMLDivElement | null>(null);

  async function refreshPlaylists() {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch("/api/playlists", { cache: "no-store" });
      if (!response.ok) throw new Error("플레이리스트를 불러오지 못했습니다.");
      const data = (await response.json()) as Playlist[];
      setPlaylists(data);
      setSelectedPlaylist((prev) => {
        if (!prev) return data[0] ?? null;
        return data.find((p) => p.id === prev.id) ?? prev;
      });
    } catch (e) {
      setPlaylists(mockPlaylists);
      setSelectedPlaylist((prev) => prev ?? mockPlaylists[0] ?? null);
      setError("백엔드 연결 실패: 목업 데이터를 표시합니다.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    refreshPlaylists();
  }, []);

  useEffect(() => {
    if (window.YT?.Player) {
      setYtApiReady(true);
      return;
    }
    window.onYouTubeIframeAPIReady = () => setYtApiReady(true);
    const script = document.createElement("script");
    script.src = "https://www.youtube.com/iframe_api";
    script.async = true;
    document.body.appendChild(script);
  }, []);

  useEffect(() => {
    if (!ytApiReady || !playerHostRef.current || playerRef.current) return;
    playerRef.current = new window.YT.Player(playerHostRef.current, {
      videoId: "",
      playerVars: { autoplay: 1, mute: 0, playsinline: 1, rel: 0 },
      events: {
        onReady: (event: any) => {
          event.target.unMute?.();
          event.target.setVolume?.(80);
        },
        onStateChange: (event: any) => {
          if (event.data === window.YT.PlayerState.PLAYING) setIsPlaying(true);
          if (event.data === window.YT.PlayerState.PAUSED) setIsPlaying(false);
        },
      },
    });
  }, [ytApiReady]);

  useEffect(() => {
    const timer = window.setInterval(() => {
      const player = playerRef.current;
      if (!player?.getCurrentTime) return;
      const current = Number(player.getCurrentTime() ?? 0);
      const total = Number(player.getDuration?.() ?? 0);
      setCurrentTimeSec(current);
      setDurationSec(total);
    }, 500);
    return () => window.clearInterval(timer);
  }, []);

  function playPlaylist(playlist: Playlist) {
    setSelectedPlaylist(playlist);
    const player = playerRef.current;
    const videoId = getYouTubeVideoId(playlist.musicUrl);
    if (player && videoId) {
      player.loadVideoById(videoId);
      player.unMute?.();
      player.setVolume?.(volume);
      setIsPlaying(true);
      setIsMuted(false);
      return;
    }
    if (playlist.musicUrl) {
      window.open(playlist.musicUrl, "_blank", "noopener,noreferrer");
    }
  }

  function togglePlay() {
    const player = playerRef.current;
    if (!player?.getPlayerState) return;
    const state = player.getPlayerState();
    if (state === window.YT?.PlayerState?.PLAYING) {
      player.pauseVideo?.();
      setIsPlaying(false);
    } else {
      player.playVideo?.();
      setIsPlaying(true);
    }
  }

  function setVolumeLevel(value: number) {
    const clamped = Math.max(0, Math.min(100, value));
    setVolume(clamped);
    const player = playerRef.current;
    player?.setVolume?.(clamped);
    if (clamped === 0) {
      player?.mute?.();
      setIsMuted(true);
    } else {
      player?.unMute?.();
      setIsMuted(false);
    }
  }

  function toggleMute() {
    const player = playerRef.current;
    if (!player) return;
    if (isMuted) {
      player.unMute?.();
      player.setVolume?.(volume || 80);
      setIsMuted(false);
    } else {
      player.mute?.();
      setIsMuted(true);
    }
  }

  function seekTo(sec: number) {
    const player = playerRef.current;
    if (!player?.seekTo) return;
    player.seekTo(sec, true);
    setCurrentTimeSec(sec);
  }

  const value = useMemo<PlayerContextType>(
    () => ({
      playlists,
      loading,
      error,
      selectedPlaylist,
      isPlaying,
      volume,
      isMuted,
      currentTimeSec,
      durationSec,
      refreshPlaylists,
      playPlaylist,
      togglePlay,
      setVolumeLevel,
      toggleMute,
      seekTo,
    }),
    [playlists, loading, error, selectedPlaylist, isPlaying, volume, isMuted, currentTimeSec, durationSec]
  );

  return (
    <PlayerContext.Provider value={value}>
      <div
        ref={playerHostRef}
        className="pointer-events-none fixed left-[-9999px] top-[-9999px] h-px w-px opacity-0"
        aria-hidden="true"
      />
      {children}
    </PlayerContext.Provider>
  );
}

export function usePlayer() {
  const ctx = useContext(PlayerContext);
  if (!ctx) throw new Error("usePlayer must be used within PlayerProvider");
  return ctx;
}
```

---

## Backend

### `Playlist.java`
```java
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

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long likeCount;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long savedCount;

    public Playlist(String title, String emotion, String musicUrl) {
        this.title = title;
        this.emotion = emotion;
        this.musicUrl = musicUrl;
        this.likeCount = 0L;
        this.savedCount = 0L;
    }

    @PrePersist
    public void prePersist() {
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
```

### `PlaylistService.java`
```java
package com.podpick.backend.playlist.service;

import com.podpick.backend.playlist.domain.Playlist;
import com.podpick.backend.global.exception.PlaylistNotFoundException;
import com.podpick.backend.playlist.repository.PlaylistRepository;
import org.springframework.data.domain.Sort;
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

    public List<Playlist> getPlaylists() {
        return playlistRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Transactional
    public Playlist createPlaylist(String title, String emotion, String musicUrl) {
        return playlistRepository.save(new Playlist(title, emotion, normalizeMusicUrl(musicUrl)));
    }

    @Transactional
    public Playlist updatePlaylist(Long id, String title, String emotion, String musicUrl) {
        Playlist playlist = playlistRepository.findById(id)
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
    public void deletePlaylist(Long id) {
        if (!playlistRepository.existsById(id)) {
            throw new PlaylistNotFoundException(id);
        }
        playlistRepository.deleteById(id);
    }

    @Transactional
    public Playlist increaseLikeCount(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new PlaylistNotFoundException(id));
        playlist.increaseLikeCount();
        return playlistRepository.save(playlist);
    }

    @Transactional
    public Playlist increaseSavedCount(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new PlaylistNotFoundException(id));
        playlist.increaseSavedCount();
        return playlistRepository.save(playlist);
    }
}
```

### `PlaylistController.java`
```java
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Playlists", description = "플레이리스트 API")
@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @Operation(summary = "플레이리스트 목록 조회")
    @GetMapping
    public List<PlaylistResponse> getPlaylists() {
        return playlistService.getPlaylists().stream()
                .map(PlaylistResponse::from)
                .toList();
    }

    @Operation(summary = "플레이리스트 생성")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public PlaylistResponse createPlaylist(@Valid @RequestBody PlaylistCreateRequest request) {
        Playlist saved = playlistService.createPlaylist(request.title(), request.emotion(), request.musicUrl());
        return PlaylistResponse.from(saved);
    }

    @Operation(summary = "플레이리스트 수정")
    @PutMapping("/{id}")
    public PlaylistResponse updatePlaylist(
            @PathVariable Long id,
            @Valid @RequestBody PlaylistUpdateRequest request
    ) {
        Playlist updated = playlistService.updatePlaylist(id, request.title(), request.emotion(), request.musicUrl());
        return PlaylistResponse.from(updated);
    }

    @Operation(summary = "플레이리스트 삭제")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
    }

    @Operation(summary = "플레이리스트 좋아요 증가")
    @PostMapping("/{id}/like")
    public PlaylistResponse increaseLikeCount(@PathVariable Long id) {
        Playlist updated = playlistService.increaseLikeCount(id);
        return PlaylistResponse.from(updated);
    }

    @Operation(summary = "플레이리스트 좋아요 증가(PATCH)")
    @PatchMapping("/{id}/like")
    public PlaylistResponse increaseLikeCountByPatch(@PathVariable Long id) {
        Playlist updated = playlistService.increaseLikeCount(id);
        return PlaylistResponse.from(updated);
    }

    @Operation(summary = "플레이리스트 저장 수 증가")
    @PostMapping("/{id}/save")
    public PlaylistResponse increaseSavedCount(@PathVariable Long id) {
        Playlist updated = playlistService.increaseSavedCount(id);
        return PlaylistResponse.from(updated);
    }

    @Operation(summary = "플레이리스트 저장 수 증가(PATCH)")
    @PatchMapping("/{id}/save")
    public PlaylistResponse increaseSavedCountByPatch(@PathVariable Long id) {
        Playlist updated = playlistService.increaseSavedCount(id);
        return PlaylistResponse.from(updated);
    }
}
```

### `application.properties`
```properties
spring.application.name=backend

# H2 database (in-memory mode)
# 기존 파일 DB 스키마 충돌로 부팅 실패하는 상황을 막기 위해 메모리 모드로 실행합니다.
spring.datasource.url=jdbc:h2:mem:podpick;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Swagger(OpenAPI)
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```
