# openlyrics

<p align="center">
  <img src="./showcase/mainshowcase.gif" alt="openlyrics showcase" width="100%">
</p>

![Skies and lyrics](./showcase/skyshowcase.gif)
![Floating lyrics and effects](./showcase/anothershowcase.gif)

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/vanillaxtra/openlyrics)
[![Loader](https://img.shields.io/badge/Loader-Fabric-dbb18c?style=flat-square)](https://fabricmc.net/)
[![Side](https://img.shields.io/badge/Side-Client-5aa9e6?style=flat-square)](https://github.com/vanillaxtra/openlyrics)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11%20%7C%2026.1--26.3-62b47a?style=flat-square)](https://github.com/vanillaxtra/openlyrics)
[![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)](LICENSE)

A **client-side** Fabric mod that shows **synced lyrics inside your Minecraft world**, timed to the music, with **20 animated skies** and effects that react to the beat. Play a song in Spotify, look up, and the words appear around you—with karaoke-style highlighting on the vocal. No server install; works in singleplayer and on any multiplayer server.

---

# 🎵 Lyrics

Two display modes—**sky lines** or **floating words**—switch any time in the menu (**J**).

* **Word sync** – Karaoke highlight follows the singer line by line and word by word when lrclib has timed data.
* **Follow view** – New phrases spawn where you look.
* **View lock** – Lyrics stay fixed in world space instead of following the camera.
* **Underground / caves** – Optional look-through so words stay visible below the surface.
* **Style** – Size, opacity, text color, glow color and strength, entrance animation (fade, pop, slide).
* **Fonts** – **33** bundled faces, including **Mojangles** (vanilla Minecraft font) and pixel / display fonts.
* **Song title intro** – Big title in the sky when a new track starts.
* **Translation** – Optional subtitles (German, English, Spanish, French, Turkish, Polish, Italian, Russian).
* **Timing** – Global offset, look-ahead, chunk pre-roll, pace tuning, and **per-song offset memory**.
* **Favorites** – Save the current line with **N**; browse saved lines and recent tracks in the menu.

| Sky mode | Floating words |
|----------|----------------|
| Lines aligned across the sky | Short phrases drift in around you |

---

# 🌌 Skies

Every preset is animated and can react to your music. Tune **strength**, **speed**, **liveliness**, **weather amount**, and **palette** per sky.

* **20 presets** – Aurora, nebula, black hole, synthwave, night city, cherry blossom, thunderstorm, matrix, prism, pulsar, and more.
* **Custom sky** – Pick top / horizon / aurora colors, stars, moon, shooting stars, and weather in the editor.
* **Shuffle** – Random sky or a new sky per song.
* **Constellations** – Optional lines between stars.
* **Day & night** – Skies during daytime, optional day/night fade, real in-game rain on rainy presets, hide vanilla clouds.

<details>
<summary><b>All 20 sky presets</b></summary>

| Sky | What you get |
|-----|----------------|
| **Synthwave** | Retro sun, drifting stripes, pink horizon |
| **Aurora** | Flowing aurora curtains |
| **Nebula** | Colorful nebulae turning around you |
| **Black hole** | Accretion disk and dark core |
| **Cosmos** | Milky Way, bright core, distant planet |
| **Night city** | Horizon lights, searchlights, rain |
| **Golden hour** | Warm clouds and fireflies |
| **Cotton candy** | Pastel blue sky, pink clouds |
| **Cherry blossom** | Pink dusk, petals |
| **Frost night** | Ice bands, moon, snow |
| **Midnight** | Stars, moon, shooting stars |
| **Deep sea** | Underwater-style light web |
| **Blood moon** | Red moon, embers |
| **Thunderstorm** | Dark clouds, rain, lightning |
| **Inferno** | Lava veins, embers |
| **Matrix** | Green code columns |
| **Prism** | Rotating color beams |
| **Pulsar** | Sweeping beam, outward rings |
| **Watercolor** | Pastel blots, petals |
| **Custom** | Your colors, lights, weather |

</details>

---

# 📱 Menu & HUD

Press **J** to open the menu. Tabs: **Sky**, **Lyrics**, **Style**, **Spotify**, **Display**, **Performance**, **Saved**. Searchable command palette; optional blur, animations, and UI sounds.

* **Phone mode** – Character holds a phone; swipe up and aim with the crosshair.
* **Classic mode** – Full-screen overlay menu.
* **Island (top bar)** – Current lyric line, album art, clock, ping, FPS (each toggleable).
* **Glass hotbar** – Dark glass frame around the hotbar.
* **Clip mode** (**F10**) – Sky and lyrics only for clean recordings.

---

# ✨ Visual extras

* **Trail** – Glowing sparks behind you (off, accent, rainbow, or sky color).
* **Aura** – Glow ring under your feet, pulses with the music.
* **Beat edge** – Screen edges glow on the beat.
* **Beat pulse on lyrics** – Optional word pulse (off by default).
* **Title-screen intro** – Startup animation on the main menu.
* **Particles & ripples** – Menu / ambient polish (configurable).

---

# ⚡ Performance

* **Auto FPS mode** – Set a target FPS; the mod eases effects when frames drop.
* **Profiles** – **Chill** (full visuals) and **PvP** (lighter) on **F8**.
* **Quality** – Performance, Balanced, Pretty presets.
* **Per-effect toggles** – Weather, aurora, stars, beams, sky color, and more.
* **Cost readout** – See approximate ms/frame the mod adds in the Performance tab.

---

# 🎧 Music & Spotify

Lyrics are fetched from [lrclib.net](https://lrclib.net) using the track playing in **Spotify**.

### Windows (automatic)

Open the **Spotify desktop app** and play. No login or Client ID; Free and Premium. *(Windows 10/11 desktop app only—not the web player.)*

### Mac, Linux, or Web API

1. Create an app in the [Spotify developer dashboard](https://developer.spotify.com/dashboard).
2. Add the redirect URI shown in the mod’s Spotify page.
3. Paste your **Client ID** and click **Connect**.

In-game tutorial included. Skip / previous / pause over Web API require **Spotify Premium**.

### Demo mode

Built-in test lyrics so you can preview skies and styling without Spotify.

---

# ⌨️ Controls

Rebind everything in Minecraft **Controls → openlyrics**.

| Key | Action |
|-----|--------|
| **J** | Open menu |
| **F9** | Next song |
| **Left Arrow** | Previous song |
| **P** | Pause / resume |
| **L** | Toggle lyrics display |
| **N** | Save current line |
| **F8** | Chill / PvP profile |
| **F10** | Clip mode |
| *(unbound)* | Sync lyrics here (set offset at current playback position) |

---

# ❓ FAQ

**Lyrics are early or late.**  
Menu → **Lyrics** → **Timing**. Adjust offset or look-ahead; per-song offset is remembered.

**No lyrics for my song.**  
lrclib is community-sourced. Popular tracks usually have entries; use **Demo** to verify your setup.

**Servers?**  
Client-only; the server does not need the mod.

**Other music apps?**  
Spotify only for now.

**Privacy?**  
Title and artist go to lrclib for lookup. **Translation** (off by default) sends lines to a translation service. Web API login stays local with your own Client ID.

**Shaders / other mods?**  
Not formally tested—open an [issue](https://github.com/vanillaxtra/openlyrics/issues) with your setup.

**Low FPS?**  
Lower quality, enable auto FPS, switch to **PvP**, or disable heavy glow / weather.

---

## Configuration

Main config: `.minecraft/config/openlyrics/config.json`

Most options are in the in-game menu (**J**). The JSON holds the same settings for backup or manual edits. Per-song lyric offsets and favorites are stored alongside it in the `openlyrics` config folder.

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for **1.21.11** or **26.1 – 26.3**.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) for your Minecraft version in `mods`.
3. Download the jar matching your version (`openlyrics-1.0.0-<mc>.jar` from [Releases](https://github.com/vanillaxtra/openlyrics/releases)) into `mods`.
4. Launch the game, press **J**, connect Spotify or enable **Demo**, and play a track.

---

## Requirements

* **Minecraft:** 1.21.11 or 26.1 – 26.3
* **Loader:** Fabric (0.18+ on 1.21.11; 0.20+ on 26.x—use the jar built for your game)
* **Fabric API** for the same Minecraft version
* **Spotify** (desktop on Windows, or Web API elsewhere)—or **Demo** mode

---

## Links & credits

* **Source:** [github.com/vanillaxtra/openlyrics](https://github.com/vanillaxtra/openlyrics)
* **Issues:** [GitHub Issues](https://github.com/vanillaxtra/openlyrics/issues)
* **Lyrics:** [lrclib.net](https://lrclib.net)
* **Author:** notauthorised

---

## License

This project is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.

---

## Third-party fonts

Bundled under `assets/openlyrics/font/` (license texts in `assets/openlyrics/font/licenses/`):

* Press Start 2P, Pixelify Sans, Silkscreen, Lilita One, Bungee, Righteous, Playfair Display, Abril Fatface, Dancing Script — **SIL Open Font License**
* Permanent Marker — **Apache License 2.0**
* Other bundled families (Poppins, Nunito, Montserrat, …) — **SIL OFL** unless noted in that folder

<!-- Modrinth: use https://raw.githubusercontent.com/vanillaxtra/openlyrics/main/showcase/*.gif if relative paths fail -->
