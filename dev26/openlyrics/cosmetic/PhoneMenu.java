package dev.openlyrics.cosmetic;

import dev.openlyrics.compat.Mc;
import dev.openlyrics.OpenLyrics;
import dev.openlyrics.gui.LyricsScreen;
import net.minecraft.client.Minecraft;
import dev.openlyrics.compat.DrawBuf;
import org.joml.Matrix4f;

public final class PhoneMenu {
   private static WorldPanel panel;

   private PhoneMenu() {
   }

   public static boolean interactive() {
      return OpenLyrics.cfg().menuMode == 1;
   }

   public static void open(Minecraft mc) {
      if (panel != null) {
         if (!panel.closing()) {
            panel.close();
         }
      } else if (interactive() && mc.level != null && mc.player != null) {
         panel = new WorldPanel(mc);
      } else {
         Mc.setScreen(mc, new LyricsScreen());
      }
   }

   public static void closeNow() {
      panel = null;
   }

   public static void menuClosed() {
   }

   public static boolean hidesHand() {
      return false;
   }

   public static void tick(Minecraft mc) {
      if (panel != null) {
         if (mc.level != null && mc.player != null && !panel.finished() && !(mc.player.position().distanceTo(panel.pos()) > 32.0)) {
            panel.tick(mc);
         } else {
            panel = null;
         }
      }
   }

   public static void startTick(Minecraft mc) {
      if (panel != null && Mc.screen(mc) == null && panel.hovered()) {
         while (mc.options.keyAttack.consumeClick()) {
            panel.click();
         }

         while (mc.options.keyUse.consumeClick()) {
            panel.click();
         }
      }
   }

   public static void render(Minecraft mc, DrawBuf buffers, Matrix4f base, float pt) {
      if (panel != null && mc.player != null) {
         panel.render(mc, buffers, base);
      }
   }
}
