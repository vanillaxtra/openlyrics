package dev.openlyrics.config;

import dev.openlyrics.hud.Gfx;
import dev.openlyrics.sky.SkyPreset;
import dev.openlyrics.sky.WorldSky;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Config {
   public int accent = 2;
   public boolean rgbAccent = false;
   public boolean blur = true;
   public boolean particles = true;
   public boolean ripples = true;
   public boolean sounds = true;
   public int uiFont = 22;
   public int lyricsFont = 22;
   public boolean fxMenuAnim = true;
   public int menuMode = 0;
   public int menuModeVer = 0;
   public boolean menuCustom = true;
   public int menuTime = 0;
   public int menuCam = 0;
   public boolean menuOwnBg = false;
   public int sky = 0;
   public int lastSky = 0;
   public float skyStrength = 0.8F;
   public float weatherAmount = 1.0F;
   public boolean realWeather = true;
   public boolean hideClouds = true;
   public String cloudsBefore = "";
   public boolean skyDay = true;
   public boolean fxDayNight = true;
   public boolean fxConstellations = true;
   public boolean fxSkyMusic = true;
   public float skySpeed = 1.0F;
   public int skyPalette = -1;
   public int trail = 1;
   public boolean aura = true;
   public boolean beatGlow = true;
   public boolean skyShuffle = true;
   public float skyPattern = 1.0F;
   public float skyLively = 0.6F;
   public float skyBreath = 1.0F;
   public int customTop = 656920;
   public int customHorizon = 2758216;
   public int customAurora1 = 10719743;
   public int customAurora2 = 16777215;
   public int customWeather = 2;
   public boolean customAurora = true;
   public boolean customStars = true;
   public boolean customMoon = true;
   public boolean customShooting = true;
   public boolean lyricsSky = true;
   public boolean lyricsIsland = true;
   public boolean lyricsFollow = true;
   public int lyricsStyle = 1;
   public boolean lyricsViewLock = true;
   public float lyricsSize = 2.0F;
   public float lyricsOpacity = 0.9F;
   public boolean lyricsGlow = true;
   public float lyricsGlowStrength = 0.5F;
   public int lyricsGlowColor = 6;
   public int lyricsColor = 1;
   public int lyricsAnim = 2;
   public boolean lyricsUnderground = true;
   public int lyricsOffsetMs = 0;
   public int lyricsLeadMs = 150;
   public int lyricsChunkPreRollMs = 120;
   public int lyricsPacePct = 100;
   public boolean fxBeatPulse = false;
   public boolean fxSongIntro = true;
   public boolean translate = false;
   public String translateTo = "de";
   public boolean islandEnabled = true;
   public boolean showClock = true;
   public boolean showPing = true;
   public boolean showFps = true;
   public boolean albumCover = true;
   public boolean glassHotbar = true;
   public boolean fxStartup = true;
   public int quality = 2;
   public boolean fpsBoost = false;
   public String boostBefore = "";
   public boolean autoPerf = true;
   public int targetFps = 0;
   public boolean fxWeather = true;
   public boolean fxAurora = true;
   public boolean fxStars = true;
   public boolean fxBeams = true;
   public boolean fxSkyColor = true;
   public int profile = 0;
   public boolean demoLyrics = false;
   public String spotifyClientId = "";
   public int spotifyMode = 0;
   public boolean spotifyTutorialSeen = false;
   public int styleVersion = 10;

   public void clamp() {
      if (this.styleVersion < 2 && this.spotifyClientId != null && !this.spotifyClientId.isBlank()) {
         this.spotifyMode = 1;
      }

      if (this.styleVersion < 3 && System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
         this.spotifyMode = 0;
      }

      if (this.styleVersion < 7) {
         this.fxDayNight = false;
         this.skyDay = true;
      }

      if (this.styleVersion < 8) {
         this.styleVersion = 8;
      }

      if (this.styleVersion < 9) {
         this.autoPerf = true;
         this.targetFps = 0;
         this.styleVersion = 9;
      }

      if (this.styleVersion < 10) {
         int moj = 0;
         for (int i = 0; i < Gfx.FONT_IDS.length; i++) {
            if ("mojangles".equals(Gfx.FONT_IDS[i])) {
               moj = i;
               break;
            }
         }
         this.accent = 2;
         this.blur = true;
         this.particles = true;
         this.ripples = true;
         this.sounds = true;
         this.uiFont = moj;
         this.lyricsFont = moj;
         this.fxMenuAnim = true;
         if (this.sky < 0) {
            this.sky = Math.max(0, this.lastSky);
         }
         this.skyShuffle = true;
         this.fxDayNight = true;
         this.customMoon = true;
         this.customAurora = true;
         this.customStars = true;
         this.customShooting = true;
         this.fxConstellations = true;
         this.fxSkyMusic = true;
         this.fxWeather = true;
         this.fxAurora = true;
         this.fxStars = true;
         this.fxBeams = true;
         this.fxSkyColor = true;
         this.fxStartup = true;
         this.trail = 1;
         this.aura = true;
         this.beatGlow = true;
         this.lyricsSky = true;
         this.lyricsIsland = true;
         this.lyricsFollow = true;
         this.lyricsStyle = 1;
         this.lyricsViewLock = true;
         this.lyricsSize = 2.0F;
         this.lyricsOpacity = 0.9F;
         this.lyricsGlow = true;
         this.lyricsGlowStrength = 0.5F;
         this.lyricsGlowColor = 6;
         this.lyricsColor = 1;
         this.lyricsAnim = 2;
         this.lyricsUnderground = true;
         this.fxBeatPulse = false;
         this.fxSongIntro = true;
         this.islandEnabled = true;
         this.showClock = true;
         this.showFps = true;
         this.albumCover = true;
         this.quality = 2;
         this.styleVersion = 10;
      }

      this.spotifyMode = Math.max(0, Math.min(1, this.spotifyMode));
      this.rgbAccent = false;
      int fonts = Gfx.FONT_IDS.length;
      this.uiFont = Math.max(0, Math.min(fonts - 1, this.uiFont));
      this.lyricsFont = Math.max(0, Math.min(fonts - 1, this.lyricsFont));
      this.sky = Math.max(-1, Math.min(SkyPreset.values().length - 1, this.sky));
      this.skyStrength = Math.max(0.1F, Math.min(1.0F, this.skyStrength));
      this.weatherAmount = Math.max(0.0F, Math.min(2.0F, this.weatherAmount));
      this.lyricsSize = Math.max(0.6F, Math.min(4.0F, this.lyricsSize));
      this.lyricsOpacity = Math.max(0.2F, Math.min(1.0F, this.lyricsOpacity));
      this.lyricsGlowStrength = Math.max(0.2F, Math.min(2.5F, this.lyricsGlowStrength));
      this.lyricsAnim = Math.floorMod(this.lyricsAnim, 3);
      this.quality = Math.max(0, Math.min(2, this.quality));
      if (this.targetFps != 0) {
         this.targetFps = Math.max(30, Math.min(240, this.targetFps));
      }

      this.skySpeed = Math.max(0.25F, Math.min(2.0F, this.skySpeed));
      this.skyPattern = Math.max(0.2F, Math.min(1.5F, this.skyPattern));
      this.skyLively = Math.max(0.0F, Math.min(1.0F, this.skyLively));
      this.skyBreath = Math.max(0.0F, Math.min(2.0F, this.skyBreath));
      this.skyPalette = Math.max(-1, Math.min(WorldSky.PALETTES.length - 1, this.skyPalette));
      this.lyricsOffsetMs = Math.max(-5000, Math.min(5000, this.lyricsOffsetMs));
      this.lyricsLeadMs = Math.max(0, Math.min(1000, this.lyricsLeadMs));
      this.lyricsChunkPreRollMs = Math.max(0, Math.min(400, this.lyricsChunkPreRollMs));
      this.lyricsPacePct = Math.max(50, Math.min(200, this.lyricsPacePct));
      this.lyricsStyle = Math.floorMod(this.lyricsStyle, 2);
      if (this.spotifyClientId == null) {
         this.spotifyClientId = "";
      }

      if (this.cloudsBefore == null) {
         this.cloudsBefore = "";
      }

      if (this.boostBefore == null) {
         this.boostBefore = "";
      }

      if (this.translateTo == null) {
         this.translateTo = "de";
      }

      if (this.sky >= 20) {
         this.sky = SkyPreset.INFERNO.ordinal();
      }

      if (this.menuModeVer < 2) {
         this.menuMode = 0;
         this.menuModeVer = 2;
      }

      if (this.sky >= 0) {
         this.lastSky = this.sky;
      }
   }
}
