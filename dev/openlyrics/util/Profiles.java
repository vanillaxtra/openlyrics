package dev.openlyrics.util;

import dev.openlyrics.OpenLyrics;
import dev.openlyrics.config.Config;
import dev.openlyrics.hud.Island;

public final class Profiles {
   private Profiles() {
   }

   public static void toggle() {
      set(OpenLyrics.cfg().profile == 0 ? 1 : 0);
   }

   public static void set(int profile) {
      Config c = OpenLyrics.cfg();
      boolean on = profile == 0;
      if (!on && c.sky >= 0) {
         c.lastSky = c.sky;
      }

      c.sky = on ? Math.max(0, c.lastSky) : -1;
      c.fxSkyColor = on;
      c.fxWeather = on;
      c.fxAurora = on;
      c.fxStars = on;
      c.fxBeams = on;
      c.lyricsGlow = on;
      c.glassHotbar = on;
      c.blur = on;
      c.fxMenuAnim = on;
      c.fxConstellations = on;
      c.fxSkyMusic = on;
      c.quality = on ? 1 : 0;
      c.profile = profile;
      if (!on && !c.fpsBoost) {
         FpsBoost.apply(true);
      }

      if (on && c.fpsBoost) {
         FpsBoost.apply(false);
      }

      OpenLyrics.saveConfig();
      Island.notify(Fx.t(on ? "openlyrics.profile.chill" : "openlyrics.profile.pvp"), true);
   }
}
