package dev.openlyrics.hud;

import dev.openlyrics.OpenLyrics;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterInit;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterRender;
import net.minecraft.client.gui.screen.TitleScreen;

public final class StartupIntro {
   private static long start = -1L;
   private static boolean done = false;

   private StartupIntro() {
   }

   public static void register() {
      ScreenEvents.AFTER_INIT.register((AfterInit)(client, screen, w, h) -> {
         if (!done && screen instanceof TitleScreen && OpenLyrics.cfg().fxStartup) {
            if (start < 0L) {
               start = System.currentTimeMillis();
            }

            ScreenEvents.afterRender(screen).register((AfterRender)(scr, g, mx, my, delta) -> {
               if (!done) {
                  float t = (float)(System.currentTimeMillis() - start) / 1000.0F;
                  if (t > 2.2F) {
                     done = true;
                  } else {
                     int sw = g.getScaledWindowWidth();
                     int sh = g.getScaledWindowHeight();
                     float old = Gfx.alpha;
                     float out = t > 1.5F ? 1.0F - (t - 1.5F) / 0.7F : 1.0F;
                     float in = Math.min(1.0F, t / 0.4F);
                     Gfx.alpha = 1.0F;
                     Gfx.rect(g, 0, 0, sw, sh, 328965, 0.94F * out);
                     Gfx.alpha = out * in;
                     String logo = "openlyrics";
                     int lw = Gfx.w(logo, Gfx.BOLD);
                     Gfx.textBold(g, logo, sw / 2 - lw / 2, sh / 2 - 8, 16777215, 1.0F, false);
                     float subA = out * Math.max(0.0F, Math.min(1.0F, (t - 0.2F) / 0.35F));
                     Gfx.alpha = subA;
                     Gfx.center(g, "notauthorised", sw / 2, sh / 2 + 8, 10326968, 1.0F, false);
                     Gfx.alpha = old;
                  }
               }
            });
         }
      });
   }
}
