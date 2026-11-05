# openlyrics

<p align="center">
  <img src="https://github.com/vanillaxtra/openlyrics/blob/main/showcase/mainshowcase.gif?raw=true" alt="openlyrics" width="100%">
</p>

![Skies](https://github.com/vanillaxtra/openlyrics/blob/main/showcase/skyshowcase.gif?raw=true)
![Floating lyrics](https://github.com/vanillaxtra/openlyrics/blob/main/showcase/anothershowcase.gif?raw=true)

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/vanillaxtra/openlyrics)
[![Loader](https://img.shields.io/badge/Loader-Fabric-dbb18c?style=flat-square)](https://fabricmc.net/)
[![Side](https://img.shields.io/badge/Side-Client-5aa9e6?style=flat-square)](https://github.com/vanillaxtra/openlyrics)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11%20%7C%2026.1--26.3-62b47a?style=flat-square)](https://github.com/vanillaxtra/openlyrics)
[![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)](LICENSE)

Client-side Fabric mod. Plays along with Spotify and puts synced lyrics in your world, with custom skies and some beat-reactive stuff. Server doesn't need it.

---

## Lyrics

Sky lines or floating words — change in menu (**J**).

- Karaoke-style highlight when the track has word timing
- Follow view / view lock / show through blocks
- Size, color, glow, fonts (Mojangles + a bunch of others)
- Offset + per-song timing if something's off
- **N** to save a line

---

## Skies

20 animated presets (aurora, nebula, synthwave, storm, matrix, etc.) plus a custom editor. Random sky or new sky per song if you want.

---

## Menu

**J** — sky, lyrics, style, spotify, display, performance, saved.

Phone menu in-world or normal screen menu. Top bar can show lyrics, cover, time, ping, fps. **F10** clip mode hides clutter for recordings.

---

## Extras

Trail, aura, beat glow on screen edges, glass hotbar, title intro. Toggle what you don't want. **F8** chill vs pvp profile, auto fps stuff in performance tab.

---

## Spotify

Lyrics from [lrclib.net](https://lrclib.net).

**Windows:** desktop Spotify app, just play. No setup.

**Elsewhere:** Client ID + connect in the mod (tutorial in menu). Premium needed for skip/pause over API.

**Demo** in menu if you want to try without Spotify.

---

## Keys

| Key | |
|---|---|
| **J** | menu |
| **F9** / **←** | next / prev |
| **P** | pause |
| **L** | lyrics on/off |
| **N** | save line |
| **F8** | profile |
| **F10** | clip mode |

Rebind in controls. Sync key exists but unbound by default.

---

## Install

1. Fabric + Fabric API for your version (1.21.11 or 26.1–26.3)
2. Drop the matching `openlyrics-*.jar` in `mods`
3. **J**, hook up Spotify or demo, play music

Config lives in `.minecraft/config/openlyrics/` — menu changes it for you.

---

## Notes

- Lyrics off? Timing tab, or song might not be on lrclib
- Only Spotify for now
- Sends title/artist to lrclib; translation sends lines if you turn it on
- [Issues](https://github.com/vanillaxtra/openlyrics/issues) · [source](https://github.com/vanillaxtra/openlyrics) · MIT · lyrics via lrclib · fonts licensed in `assets/openlyrics/font/licenses/`
